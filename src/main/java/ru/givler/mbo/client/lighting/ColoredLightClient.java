package ru.givler.mbo.client.lighting;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkEvent;
import ru.givler.mbo.config.ColoredLightConfig;
import ru.givler.mbo.lighting.*;

/** Persistent section inputs and budgeted incremental RGB updates on the client thread. */
public final class ColoredLightClient {
  public static final ColoredLightClient INSTANCE = new ColoredLightClient();
  public static final ColoredLightCache CACHE = new ColoredLightCache();
  public static volatile boolean cacheActive;
  public static volatile int skyDarkness;
  /** Legacy volume fallback for isolated renderer tests; unused by the running client. */
  public static volatile ColoredLightVolume volume;
  private World world;
  private ColoredLightSections lighting = new ColoredLightSections();
  private final Set<Section> dirty = new LinkedHashSet<>(), waiting = new LinkedHashSet<>(), known = new LinkedHashSet<>();
  private final ArrayDeque<Section> scans = new ArrayDeque<>(), polls = new ArrayDeque<>();
  private final ColoredLightWorldAccess observer = new ColoredLightWorldAccess(this);
  private final Section probe = new Section(0, 0, 0);
  private Section scanning;
  private byte[] scanOpacity;
  private int[] scanEmission;
  private int cursor, cx = Integer.MIN_VALUE, cy, cz, pause;
  private boolean disabled, urgent;
  private long geometryVersion;
  public long geometryVersion() { return geometryVersion; }
  public int opacity(int x, int y, int z) { return lighting.opacity(x, y, z); }
  public String statistics() { return "sections=" + lighting.sectionCount() + ", pending=" + scans.size() + ", ready=" + cacheActive; }
  public void applyOptions() {
    if(!ColoredLightConfig.entityLighting)ColoredEntityLightHooks.release();
    Minecraft mc = Minecraft.getMinecraft();
    if (!ColoredLightConfig.enabled) release(mc);
    else { CACHE.invalidateAll(this::mark); ColoredDynamicLights.INSTANCE.invalidate(); rebuild(mc); }
  }
  private void release(Minecraft mc) {
    CACHE.invalidateAll(this::mark);
    Set<Section> previous = new LinkedHashSet<>(dirty);
    if (world != null) world.removeWorldAccess(observer);
    world = null; reset(); dirty.addAll(previous);
    ColoredDynamicLights.INSTANCE.clear(mc);
    ColoredLightShaderBridge.release();
    ColoredEntityLightHooks.release();
    geometryVersion++; dirty.clear();
    // Clear all baked vertex colors, including diagonal sampling and previously lit moving sources.
    if (mc.theWorld != null && mc.renderGlobal != null) mc.renderGlobal.loadRenderers();
  }
  public void reloadInputs() {
    CACHE.invalidateAll(this::mark);
    Set<Section> previous = new LinkedHashSet<>(dirty);
    reset(); dirty.addAll(previous); geometryVersion++;
  }
  private ColoredLightClient() {}

  @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
    if (event.phase != TickEvent.Phase.END) return;
    Minecraft mc = Minecraft.getMinecraft();
    if (!ColoredLightConfig.enabled) {
      if (world != null || cacheActive || volume != null) release(mc);
      return;
    }
    if (world != mc.theWorld) {
      if (world != null) world.removeWorldAccess(observer);
      world = mc.theWorld; reset(); ColoredDynamicLights.INSTANCE.clear(mc); ColoredLightShaderBridge.release();
      if (world != null) world.addWorldAccess(observer);
    }
    if (world == null || mc.renderViewEntity == null || mc.isGamePaused()) return;
    if (!ColoredLightConfig.enabled || disabled) {
      if (cacheActive) { CACHE.clear(this::mark); cacheActive = false; }
      rebuild(mc); return;
    }
    try {
      int darkness = world.calculateSkylightSubtracted(1F);
      if (skyDarkness != darkness) { skyDarkness = darkness; CACHE.invalidateAll(this::mark); }
      int sx = (int) Math.floor(mc.renderViewEntity.posX) >> 4;
      int sy = (int) Math.floor(mc.renderViewEntity.posY) >> 4;
      int sz = (int) Math.floor(mc.renderViewEntity.posZ) >> 4;
      if (sx != cx || sy != cy || sz != cz) {
        cx = sx; cy = sy; cz = sz; requestArea();
        int retain = Math.max(4, inputRadius() + 1);
        lighting.retain(cx, cy, cz, retain);
        if (scanning != null && scanning.distance(cx, cy, cz) > retain) { waiting.remove(scanning); scanning = null; }
        Iterator<Section> iterator = known.iterator();
        while (iterator.hasNext()) { Section s = iterator.next(); if (s.distance(cx, cy, cz) > retain) iterator.remove(); }
      }
      scanInputs();
      ColoredLightShaderBridge.tick(mc);
      ColoredDynamicLights.INSTANCE.tick(mc);
      if (lighting.process(urgent ? ColoredLightConfig.urgentPropagationBudget : ColoredLightConfig.propagationBudget)) {
        lighting.publish(CACHE, this::mark); cacheActive = true; urgent = false;
      }
    } catch (RuntimeException error) {
      disabled = true; CACHE.clear(this::mark); cacheActive = false;
      System.err.println("[MBO] Colored lighting disabled for this world: " + error);
    }
    rebuild(mc);
  }

  @SubscribeEvent public void renderTick(TickEvent.RenderTickEvent event) {
    if (event.phase == TickEvent.Phase.START) ColoredLightShaderBridge.renderStart(event.renderTickTime);
  }

  private void reset() {
    volume = null; cacheActive = false; CACHE.clear((x, y, z) -> {});
    lighting = new ColoredLightSections(); dirty.clear(); waiting.clear(); known.clear(); scans.clear(); polls.clear();
    scanning = null; cx = Integer.MIN_VALUE; disabled = false; urgent = true; pause = 0;
    scanOpacity = null; scanEmission = null; cursor = 0;
  }

  private int inputRadius() { return (ColoredLightConfig.radius + 30) / 16; }
  private void requestArea() {
    int radius = inputRadius(); ArrayList<Section> requests = new ArrayList<>();
    for (int y = Math.max(0, cy - radius); y <= Math.min(15, cy + radius); y++)
      for (int z = cz - radius; z <= cz + radius; z++) for (int x = cx - radius; x <= cx + radius; x++) {
        Section s = new Section(x, y, z);
        if (!lighting.contains(x, y, z) && !waiting.contains(s) && world.blockExists(x * 16, y * 16, z * 16)) requests.add(s);
      }
    requests.sort(Comparator.comparingInt(s -> s.squaredDistance(cx, cy, cz)));
    for (Section s : requests) { waiting.add(s); scans.addLast(s); }
  }

  private void scanInputs() {
    int budget = (!scans.isEmpty() || (scanning != null && !known.contains(scanning)))
        ? ColoredLightConfig.urgentScanBudget : ColoredLightConfig.scanBudget;
    while (budget > 0) {
      if (scanning == null) {
        while (!scans.isEmpty()) {
          Section candidate = scans.removeFirst();
          if (candidate.distance(cx, cy, cz) <= inputRadius() && world.blockExists(candidate.x * 16, candidate.y * 16, candidate.z * 16)) { scanning = candidate; break; }
          waiting.remove(candidate);
        }
        if (scanning == null) {
          if (pause-- > 0) return;
          if (polls.isEmpty()) { polls.addAll(known); pause = ColoredLightConfig.refreshTicks; }
          if (polls.isEmpty()) return;
          scanning = polls.removeFirst();
          if (!known.contains(scanning)) { scanning = null; continue; }
        }
        cursor = 0; scanOpacity = new byte[4096]; scanEmission = new int[4096];
      }
      int end = Math.min(4096, cursor + budget);
      for (; cursor < end; cursor++, budget--) {
        int x = scanning.x * 16 + (cursor & 15), y = scanning.y * 16 + (cursor >> 8), z = scanning.z * 16 + (cursor >> 4 & 15);
        if (!world.blockExists(x, y, z)) { scanOpacity[cursor] = 15; continue; }
        Block block = world.getBlock(x, y, z);
        scanOpacity[cursor] = (byte) Math.max(1, Math.min(15, block.getLightOpacity(world, x, y, z)));
        scanEmission[cursor] = ColoredLightApi.sample(block, world, x, y, z);
      }
      if (cursor < 4096) return;
      if (!known.contains(scanning)) urgent = true;
      if (lighting.install(scanning.x, scanning.y, scanning.z, scanOpacity, scanEmission)) geometryVersion++;
      known.add(scanning); waiting.remove(scanning); scanning = null;
    }
  }

  void changed(int x, int y, int z) {
    if (world == null || disabled || !ColoredLightConfig.enabled || !world.blockExists(x, y, z)) return;
    Block block = world.getBlock(x, y, z);
    int opacity = block.getLightOpacity(world, x, y, z), emission = ColoredLightApi.sample(block, world, x, y, z);
    if (!lighting.matches(x, y, z, opacity, emission)) {
      lighting.update(x, y, z, opacity, emission); urgent = true; geometryVersion++;
      ColoredDynamicLights.INSTANCE.invalidate();
    }
    if (scanning != null && scanning.x == (x >> 4) && scanning.y == (y >> 4) && scanning.z == (z >> 4)) {
      int i = ((y & 15) * 16 + (z & 15)) * 16 + (x & 15);
      if (i < cursor) { scanOpacity[i] = (byte) Math.max(1, Math.min(15, opacity)); scanEmission[i] = emission; }
    }
  }

  @SubscribeEvent public void chunkLoaded(ChunkEvent.Load event) { chunkChanged(event); }
  @SubscribeEvent public void chunkUnloaded(ChunkEvent.Unload event) { chunkChanged(event); }
  private void chunkChanged(ChunkEvent event) {
    if (!event.world.isRemote || event.world != world) return;
    int x = event.getChunk().xPosition, z = event.getChunk().zPosition;
    for (int y = 0; y < 16; y++) { lighting.remove(x, y, z); probe.x = x; probe.y = y; probe.z = z; known.remove(probe); waiting.remove(probe); }
    scans.removeIf(s -> s.x == x && s.z == z);
    if (scanning != null && scanning.x == x && scanning.z == z) scanning = null;
    urgent = true;
    if (event instanceof ChunkEvent.Load && cx != Integer.MIN_VALUE) requestArea();
  }

  private void mark(int x, int y, int z) {
    if (y < 0 || y >= 256) return;
    probe.x = x >> 4; probe.y = y >> 4; probe.z = z >> 4;
    if (!dirty.contains(probe)) dirty.add(new Section(probe.x, probe.y, probe.z));
  }
  private void rebuild(Minecraft mc) {
    for (Section s : dirty) if (mc.renderGlobal != null) mc.renderGlobal.markBlockRangeForRenderUpdate(s.x * 16 + 1, s.y * 16 + 1, s.z * 16 + 1, s.x * 16 + 14, s.y * 16 + 14, s.z * 16 + 14);
    dirty.clear();
  }

  private static final class Section {
    int x, y, z;
    Section(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
    int distance(int sx, int sy, int sz) { return Math.max(Math.abs(x - sx), Math.max(Math.abs(y - sy), Math.abs(z - sz))); }
    int squaredDistance(int sx, int sy, int sz) { int dx = x - sx, dy = y - sy, dz = z - sz; return dx * dx + dy * dy + dz * dz; }
    @Override public int hashCode() { return (x * 31 + y) * 31 + z; }
    @Override public boolean equals(Object other) { if (!(other instanceof Section)) return false; Section s = (Section) other; return x == s.x && y == s.y && z == s.z; }
  }
}
