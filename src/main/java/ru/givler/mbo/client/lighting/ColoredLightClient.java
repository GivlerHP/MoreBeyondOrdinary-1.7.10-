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
  private final ArrayDeque<Section> scans = new ArrayDeque<>();
  private final Set<Section> changedCells = new LinkedHashSet<>();
  private final ColoredLightWorldAccess observer = new ColoredLightWorldAccess(this);
  private final Section probe = new Section(0, 0, 0);
  private Section scanning;
  private byte[] scanOpacity;
  private int[] scanEmission;
  private int cursor, cx = Integer.MIN_VALUE, cy, cz, discoveryDistance = -1;
  private boolean disabled, urgent;
  private long geometryVersion;
  public long geometryVersion() { return geometryVersion; }
  public int opacity(int x, int y, int z) { return lighting.opacity(x, y, z); }
  public String statistics() { return "sections=" + lighting.sectionCount() + ", pending=" + scans.size() + ", ready=" + cacheActive; }
  public void applyDynamicOptions() {
    Minecraft mc=Minecraft.getMinecraft();ColoredDynamicLights.INSTANCE.clear(mc);rebuild(mc);
  }
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
    long diagnosticTick=ColoredLightDiagnostics.start();
    long allocationTick=ColoredLightAllocationDiagnostics.start();
    try {
      int darkness = world.calculateSkylightSubtracted(1F);
      if (skyDarkness != darkness) { skyDarkness = darkness; CACHE.invalidateAll(this::mark); ColoredDynamicLights.INSTANCE.invalidateGeometry(); }
      int sx = (int) Math.floor(mc.renderViewEntity.posX) >> 4;
      int sy = (int) Math.floor(mc.renderViewEntity.posY) >> 4;
      int sz = (int) Math.floor(mc.renderViewEntity.posZ) >> 4;
      int distance = mc.gameSettings == null ? 8 : mc.gameSettings.renderDistanceChunks;
      if (sx != cx || sy != cy || sz != cz || distance != discoveryDistance) {
        discoveryDistance = distance;
        cx = sx; cy = sy; cz = sz; requestArea();
        // Discover the whole loaded view, including sources never approached by the player.
        // Chunk unload notifications, world changes and full disable own their removal.
      }
      long diagnosticScan=ColoredLightDiagnostics.start();
      long allocationScan=ColoredLightAllocationDiagnostics.start();
      drainChanges();
      scanInputs();
      ColoredLightAllocationDiagnostics.end(ColoredLightAllocationDiagnostics.SCAN,allocationScan);
      ColoredLightDiagnostics.scan(diagnosticScan);
      ColoredLightShaderBridge.tick(mc);
      long diagnosticDynamic=ColoredLightDiagnostics.start();
      long allocationDynamic=ColoredLightAllocationDiagnostics.start();
      ColoredDynamicLights.INSTANCE.tick(mc);
      ColoredLightAllocationDiagnostics.end(ColoredLightAllocationDiagnostics.DYNAMIC,allocationDynamic);
      ColoredLightDiagnostics.dynamic(diagnosticDynamic);
      long diagnosticPropagation=ColoredLightDiagnostics.start();
      long allocationPropagation=ColoredLightAllocationDiagnostics.start();
      long processedBefore=lighting.processedCells();
      if (lighting.process(ColoredLightConfig.propagationWorkBudget(urgent))) {
        lighting.publish(CACHE, this::mark); cacheActive = true; urgent = false;
      }
      ColoredLightDiagnostics.propagation(diagnosticPropagation);
      ColoredLightAllocationDiagnostics.end(ColoredLightAllocationDiagnostics.PROPAGATION,allocationPropagation);
      ColoredLightAllocationDiagnostics.work(ColoredLightAllocationDiagnostics.PROPAGATION,lighting.processedCells()-processedBefore);
    } catch (RuntimeException error) {
      disabled = true; CACHE.clear(this::mark); cacheActive = false;
      System.err.println("[MBO] Colored lighting disabled for this world: " + error);
    }
    int diagnosticDirty=dirty.size();
    rebuild(mc);
    ColoredLightDiagnostics.end(diagnosticTick,diagnosticDirty);
    ColoredLightAllocationDiagnostics.end(ColoredLightAllocationDiagnostics.TICK,allocationTick);
  }

  @SubscribeEvent public void renderTick(TickEvent.RenderTickEvent event) {
    if (event.phase == TickEvent.Phase.START) ColoredLightShaderBridge.renderStart(event.renderTickTime);
  }

  private void reset() {
    volume = null; cacheActive = false; CACHE.clear((x, y, z) -> {});
    lighting = new ColoredLightSections(); dirty.clear(); waiting.clear(); known.clear(); scans.clear(); changedCells.clear();
    scanning = null; cx = Integer.MIN_VALUE; discoveryDistance = -1; disabled = false; urgent = true;
    scanOpacity = null; scanEmission = null; cursor = 0;
  }

  private void requestArea() {
    int radius = Math.max(1, discoveryDistance) + 1; ArrayList<Section> requests = new ArrayList<>();
    for (int y = ColoredLightApi.minimumY(world) >> 4; y < 16; y++)
      for (int z = cz - radius; z <= cz + radius; z++) for (int x = cx - radius; x <= cx + radius; x++) {
        Section s = new Section(x, y, z);
        if (!lighting.contains(x, y, z) && !waiting.contains(s) && world.blockExists(x * 16, y * 16, z * 16)) requests.add(s);
      }
    requests.sort(Comparator.comparingInt(s -> s.squaredDistance(cx, cy, cz)));
    for (Section s : requests) { waiting.add(s); scans.addLast(s); }
  }

  private void scanInputs() {
    int budget = ColoredLightConfig.scanWorkBudget(!scans.isEmpty() || (scanning != null && !known.contains(scanning)));
    while (budget > 0) {
      if (scanning == null) {
        while (!scans.isEmpty()) {
          Section candidate = scans.removeFirst();
          if (world.blockExists(candidate.x * 16, candidate.y * 16, candidate.z * 16)) { scanning = candidate; break; }
          waiting.remove(candidate);
        }
        if (scanning == null) return;
        cursor = 0;
        // install() copies values into the persistent section; scratch arrays can serve the next scan.
        if(scanOpacity==null)scanOpacity=new byte[4096];
        if(scanEmission==null)scanEmission=new int[4096];
        // Absent vanilla storage is all air. Keep its propagation path without 4096 world reads.
        if(scanning.y>=0 && scanning.y<16 && world.getChunkProvider()!=null && world.getChunkFromChunkCoords(scanning.x,scanning.z)
            .getBlockStorageArray()[scanning.y]==null) {
          Arrays.fill(scanOpacity,(byte)1);Arrays.fill(scanEmission,0);cursor=4096;
          budget-=4096;
        }
      }
      int end = Math.min(4096, cursor + budget);
      ColoredLightAllocationDiagnostics.work(ColoredLightAllocationDiagnostics.SCAN,end-cursor);
      for (; cursor < end; cursor++, budget--) {
        int x = scanning.x * 16 + (cursor & 15), y = scanning.y * 16 + (cursor >> 8), z = scanning.z * 16 + (cursor >> 4 & 15);
        scanEmission[cursor]=0;
        if (!world.blockExists(x, y, z)) { scanOpacity[cursor] = 15; continue; }
        Block block = world.getBlock(x, y, z);
        scanOpacity[cursor] = (byte) Math.max(1, Math.min(15, block.getLightOpacity(world, x, y, z)));
        scanEmission[cursor] = ColoredLightApi.sample(block, world, x, y, z);
      }
      if (cursor < 4096) return;
      if (!known.contains(scanning)) urgent = true;
      if (lighting.install(scanning.x, scanning.y, scanning.z, scanOpacity, scanEmission)) { geometryVersion++; ColoredDynamicLights.INSTANCE.invalidate(); }
      known.add(scanning); waiting.remove(scanning); scanning = null;
    }
  }

  void changed(int x, int y, int z) {
    if (world == null || disabled || !ColoredLightConfig.enabled || y < ColoredLightApi.minimumY(world) || y >= 256) return;
    changedCells.add(new Section(x,y,z));
  }

  void changedRange(int x,int y,int z,int x2,int y2,int z2) {
    if(world==null || disabled || !ColoredLightConfig.enabled)return;
    int minimumY=ColoredLightApi.minimumY(world);
    long cells=((long)x2-x+1)*((long)Math.min(255,y2)-Math.max(minimumY,y)+1)*((long)z2-z+1);
    if(cells>0 && cells<=512) {
      for(int by=Math.max(minimumY,y);by<=Math.min(255,y2);by++)for(int bz=z;bz<=z2;bz++)for(int bx=x;bx<=x2;bx++)changed(bx,by,bz);
      return;
    }
    if(scanning!=null && scanning.x>=(x>>4) && scanning.x<=(x2>>4)
        && scanning.y>=(y>>4) && scanning.y<=(y2>>4) && scanning.z>=(z>>4) && scanning.z<=(z2>>4))cursor=0;
    // Bulk chunk/light notifications share the same bounded section discovery queue.
    for(int sy=Math.max(minimumY>>4,y>>4);sy<=Math.min(15,y2>>4);sy++)
      for(int sz=z>>4;sz<=z2>>4;sz++)for(int sx=x>>4;sx<=x2>>4;sx++) {
        queueSection(sx,sy,sz);
      }
  }

  private void queueSection(int sx,int sy,int sz) {
    probe.x=sx;probe.y=sy;probe.z=sz;
    if(sy<(ColoredLightApi.minimumY(world)>>4) || sy>=16)return;
    if(waiting.contains(probe))return;
    Section section=new Section(sx,sy,sz);waiting.add(section);scans.addLast(section);
  }

  private void drainChanges() {
    for(Section cell:changedCells)applyChange(cell.x,cell.y,cell.z);
    changedCells.clear();
  }

  private void applyChange(int x,int y,int z) {
    if(!world.blockExists(x,y,z)) {
      if(lighting.contains(x>>4,y>>4,z>>4)) { lighting.update(x,y,z,15,0);urgent=true;geometryVersion++;ColoredDynamicLights.INSTANCE.invalidate(); }
      return;
    }
    if(!lighting.contains(x>>4,y>>4,z>>4)) {
      queueSection(x>>4,y>>4,z>>4);return;
    }
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
    for (int y = ColoredLightApi.minimumY(world)>>4; y < 16; y++) { lighting.remove(x, y, z); probe.x = x; probe.y = y; probe.z = z; known.remove(probe); waiting.remove(probe); }
    scans.removeIf(s -> s.x == x && s.z == z);
    changedCells.removeIf(s -> (s.x >> 4) == x && (s.z >> 4) == z);
    if (scanning != null && scanning.x == x && scanning.z == z) scanning = null;
    urgent = true;
    geometryVersion++;
    if (event instanceof ChunkEvent.Load) for(int y=ColoredLightApi.minimumY(world)>>4;y<16;y++)queueSection(x,y,z);
  }

  void markRange(int x,int y,int z,int x2,int y2,int z2) {
    for(int sy=Math.max(ColoredLightApi.minimumY(world)>>4,y>>4);sy<=Math.min(15,y2>>4);sy++)
      for(int sz=z>>4;sz<=z2>>4;sz++)for(int sx=x>>4;sx<=x2>>4;sx++)mark(sx*16+8,sy*16+8,sz*16+8);
  }
  private void mark(int x, int y, int z) {
    if (y < ColoredLightApi.minimumY(world) || y >= 256) return;
    probe.x = x >> 4; probe.y = y >> 4; probe.z = z >> 4;
    if (!dirty.contains(probe)) dirty.add(new Section(probe.x, probe.y, probe.z));
  }
  void flushDynamicRebuilds(Minecraft mc) { rebuild(mc); }
  private void rebuild(Minecraft mc) {
    for (Section s : dirty) if (mc.renderGlobal != null) mc.renderGlobal.markBlockRangeForRenderUpdate(s.x * 16 + 1, s.y * 16 + 1, s.z * 16 + 1, s.x * 16 + 14, s.y * 16 + 14, s.z * 16 + 14);
    // Vanilla skips markDirty for a renderer already queued. Explicitly notify it again so
    // FalseTweaks/Neodymium discard an in-flight build made with the previous RGB field.
    if(mc.renderGlobal!=null && !dirty.isEmpty()) {
      try {
        net.minecraft.client.renderer.WorldRenderer[] renderers=(net.minecraft.client.renderer.WorldRenderer[])RendererFields.ALL.get(mc.renderGlobal);
        if(renderers!=null)for(net.minecraft.client.renderer.WorldRenderer renderer:renderers) {
          if(renderer==null)continue;
          probe.x=renderer.posX>>4;probe.y=renderer.posY>>4;probe.z=renderer.posZ>>4;
          if(dirty.contains(probe))renderer.markDirty();
        }
      } catch(IllegalAccessException e) { throw new IllegalStateException("Cannot invalidate RGB terrain builds",e); }
    }
    dirty.clear();
  }
  private static final class RendererFields {
    static final java.lang.reflect.Field ALL=cpw.mods.fml.relauncher.ReflectionHelper.findField(
        net.minecraft.client.renderer.RenderGlobal.class,"worldRenderers","field_72765_l");
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
