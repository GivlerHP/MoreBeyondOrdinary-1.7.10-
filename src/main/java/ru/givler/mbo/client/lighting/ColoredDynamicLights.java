package ru.givler.mbo.client.lighting;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.World;
import ru.givler.mbo.config.ColoredLightConfig;
import ru.givler.mbo.lighting.ColoredLightApi;

/** Small separate moving-light layer. Never inserts moving sources into the persistent flood fill. */
public final class ColoredDynamicLights {
  public static final ColoredDynamicLights INSTANCE = new ColoredDynamicLights();
  public static final class Source {
    public final Entity entity;
    public final int packed;
    public final double x, y, z, radius;
    Source(Entity entity, int packed) {
      this(entity, packed, entity.posX, entity.posY + (entity instanceof EntityItem ? 0.2 : entity.getEyeHeight()), entity.posZ);
    }
    Source(Entity entity, int packed, double x, double y, double z) {
      this.entity = entity; this.packed = packed;
      this.x = x; this.y = y; this.z = z;
      radius = 7.5; // OptiFine E7 range; intensity is supplied separately by the source level.
    }
    public double x(float partial) { return entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial; }
    public double y(float partial) { return entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partial + (entity instanceof EntityItem ? 0.2 : entity.getEyeHeight()); }
    public double z(float partial) { return entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial; }
  }
  private volatile List<Source> sources = Collections.emptyList();
  private volatile World world;
  private volatile Published published = new Published(null, Collections.emptyList());
  private int ticks;
  private volatile long generation;
  private boolean invalidated;
  private final java.util.concurrent.ConcurrentLinkedQueue<java.lang.ref.WeakReference<Query>> allocatedQueries = new java.util.concurrent.ConcurrentLinkedQueue<>();
  private final ThreadLocal<Query> queries = new ThreadLocal<Query>() {
    @Override protected Query initialValue() { Query query = new Query(); allocatedQueries.add(new java.lang.ref.WeakReference<>(query)); return query; }
  };
  private ColoredDynamicLights() {}
  public List<Source> sources() { return sources; }
  public long generation() {return generation;}
  public int cachedCells() {
    int cells = 0;
    for (java.lang.ref.WeakReference<Query> reference : allocatedQueries) { Query query = reference.get(); if (query != null) cells += query.values.size(); }
    return cells;
  }
  public void clear(Minecraft mc) {
    if (!ColoredLightShaderBridge.active) for (Source source : sources) mark(mc,source);
    sources = Collections.emptyList(); world = null; generation++; ticks = 0; invalidated = false;
    published = new Published(null, sources);
    for (java.lang.ref.WeakReference<Query> reference : allocatedQueries) {
      Query query = reference.get();
      if (query == null) allocatedQueries.remove(reference);
      else { query.values = new HashMap<>(); query.snapshot = null; }
    }
  }

  public void tick(Minecraft mc) {
    if (ColoredDynamicLightBridge.available()) {
      if (!ColoredDynamicLightBridge.enabled() || !ColoredLightConfig.dynamicLights) {
        if (!sources.isEmpty()) clear(mc);
      }
      return; // FalseTweaks owns discovery, movement cadence and removal.
    }
    if (world != mc.theWorld) { world = mc.theWorld; sources = Collections.emptyList(); published = new Published(world,sources); generation++; ticks = 0; }
    if (world == null || mc.renderViewEntity == null) return;
    if(!ColoredLightConfig.dynamicLights) { if(!sources.isEmpty())clear(mc);return; }
    if (++ticks % Math.max(1, ColoredLightConfig.dynamicInterval) != 0) return;
    List<Source> next = new ArrayList<>();
    if (ColoredLightConfig.dynamicLights && ColoredLightConfig.dynamicLimit > 0) {
      for (Object object : world.loadedEntityList == null ? Collections.emptyList() : world.loadedEntityList) {
        if (!(object instanceof Entity)) continue;
        Entity entity = (Entity) object;
        if (entity.isDead || entity.getDistanceSqToEntity(mc.renderViewEntity) > 32 * 32) continue;
        int packed = ColoredLightApi.entityEmission(entity);
        if (packed == 0 && entity instanceof EntityItem) packed = ColoredLightApi.itemEmission(((EntityItem) entity).getEntityItem(), entity);
        if (entity instanceof EntityLivingBase) packed = ColoredLightAuto.merge(packed, ColoredLightAuto.heldEmission((EntityLivingBase) entity));
        if ((packed >>> 24) != 0 && (packed & 0xFFFFFF) != 0) next.add(new Source(entity, packed));
      }
      next.sort(Comparator.comparingDouble(s -> s.entity.getDistanceSqToEntity(mc.renderViewEntity)));
      if (next.size() > ColoredLightConfig.dynamicLimit) next = new ArrayList<>(next.subList(0, ColoredLightConfig.dynamicLimit));
    }
    publish(mc, next, .25);
  }
  static int emission(Entity entity) {
    int packed = ColoredLightApi.entityEmission(entity);
    if (entity instanceof EntityItem) packed = ColoredLightAuto.merge(packed, ColoredLightApi.itemEmission(((EntityItem) entity).getEntityItem(), entity));
    if (entity instanceof EntityLivingBase) packed = ColoredLightAuto.merge(packed, ColoredLightAuto.heldEmission((EntityLivingBase) entity));
    return packed;
  }
  void fromBackend(Minecraft mc, List<Source> next) {
    if (world != mc.theWorld) { clear(mc); world = mc.theWorld; }
    if (!ColoredLightConfig.enabled || !ColoredLightConfig.dynamicLights) next = new ArrayList<>();
    next.sort(Comparator.comparingDouble(s -> s.entity.getDistanceSqToEntity(mc.renderViewEntity)));
    if (next.size() > ColoredLightConfig.dynamicLimit) next = new ArrayList<>(next.subList(0, Math.max(0, ColoredLightConfig.dynamicLimit)));
    publish(mc, next, .000001);
  }
  private void publish(Minecraft mc, List<Source> next, double movementThreshold) {
    boolean changed = invalidated || next.size() != sources.size(); invalidated = false;
    if (!changed) for (int i = 0; i < next.size(); i++) {
      Source a = next.get(i), b = sources.get(i);
      if (a.entity != b.entity || a.packed != b.packed || Math.abs(a.x - b.x) + Math.abs(a.y - b.y) + Math.abs(a.z - b.z) > movementThreshold) { changed = true; break; }
    }
    if (changed) {
      ColoredLightDiagnostics.dynamicUpdate();
      List<Source> previous = sources;
      sources = Collections.unmodifiableList(next); generation++;
      published = new Published(world, sources);
      // This fallback rebuilds only nearby sections, at a configurable cadence.
      if (!ColoredLightShaderBridge.active) {
        for (Source source : previous) mark(mc, source);
        for (Source source : next) mark(mc, source);
      }
    }
  }
  private void mark(Minecraft mc, Source source) {
    if (mc.renderGlobal == null) return;
    int r = (int) Math.ceil(source.radius) + 2; // Include face/interpolation samples across section edges.
    int x = (int) Math.floor(source.x), y = (int) Math.floor(source.y), z = (int) Math.floor(source.z);
    ColoredLightClient.INSTANCE.markRange(x-r,y-r,z-r,x+r,y+r,z+r);
  }
  public int sample(int x, int y, int z) {
    Published snapshot = published;
    World sampledWorld = snapshot.world;
    if (snapshot.sources.isEmpty() || sampledWorld == null || !ColoredLightConfig.enabled || !ColoredLightConfig.dynamicLights) return 0;
    Query query = queries.get();
    if (query.snapshot != snapshot) { query.snapshot = snapshot; query.values.clear(); }
    Map<Cell,Integer> values = query.values;
    query.probe.x = x; query.probe.y = y; query.probe.z = z;
    Integer cached = values.get(query.probe); if (cached != null) return cached;
    int rgb = 0;
    for (Source source : snapshot.sources) {
      double dx = x + 0.5 - source.x, dy = y + 0.5 - source.y, dz = z + 0.5 - source.z;
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (distance >= source.radius || !visible(sampledWorld, source, x + 0.5, y + 0.5, z + 0.5, distance)) continue;
      int base = ColoredLightApi.rgb(source.packed); float scale = (float) (1 - distance / source.radius);
      int value = Math.round((base >>> 16) * scale) << 16 | Math.round((base >> 8 & 255) * scale) << 8 | Math.round((base & 255) * scale);
      rgb = ru.givler.mbo.lighting.ColoredLightVolume.merge(rgb, value);
    }
    if (!ColoredLightConfig.enabled || published != snapshot) return 0;
    if (values.size() < 32768) values.put(new Cell(x, y, z), rgb);
    return rgb;
  }
  public void invalidateGeometry() {
    if(!ColoredLightShaderBridge.active)for(Source source:sources)mark(Minecraft.getMinecraft(),source);
  }
  public void invalidate() { generation++; published = new Published(world,sources); invalidated = true; }
  private static final class Published {
    final World world; final List<Source> sources;
    Published(World world,List<Source> sources){this.world=world;this.sources=sources;}
  }
  private static final class Query { Published snapshot; final Cell probe = new Cell(0, 0, 0); volatile Map<Cell, Integer> values = new HashMap<>(); }
  private boolean visible(World sampledWorld, Source source, double x, double y, double z, double distance) {
    int steps = Math.max(1, (int) Math.ceil(distance * 2));
    for (int i = 1; i < steps; i++) {
      double t = i / (double) steps;
      int bx = (int) Math.floor(source.x + (x - source.x) * t), by = (int) Math.floor(source.y + (y - source.y) * t), bz = (int) Math.floor(source.z + (z - source.z) * t);
      if (!sampledWorld.blockExists(bx, by,bz) || sampledWorld.getBlock(bx,by,bz).getLightOpacity(sampledWorld,bx,by,bz) >= 15) return false;
    }
    return true;
  }
  private static final class Cell {
    int x, y, z;
    Cell(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
    @Override public int hashCode() { return (x * 31 + y) * 31 + z; }
    @Override public boolean equals(Object o) { if (!(o instanceof Cell)) return false; Cell c = (Cell) o; return x == c.x && y == c.y && z == c.z; }
  }
}
