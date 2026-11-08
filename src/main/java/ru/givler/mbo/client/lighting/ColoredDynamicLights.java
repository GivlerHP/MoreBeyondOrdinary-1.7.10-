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
      // Worker-owned cache contents are reset lazily on the next published snapshot.
    }
  }

  public void tick(Minecraft mc) {
    if (AngelicaDynamicLightBridge.available()) {
      if (!AngelicaDynamicLightBridge.enabled() || !ColoredLightConfig.dynamicLights) {
        if (!sources.isEmpty()) clear(mc);
      }
      return;
    }
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
    publish(mc, next, .1);
  }
  private void publish(Minecraft mc, List<Source> next, double movementThreshold) {
    boolean force=invalidated;
    boolean changed = force || next.size() != sources.size(); invalidated = false;
    // Distance sorting can change when the camera moves. Match source identity,
    // retain its last accepted position, and rebuild only sources that changed.
    List<Source> dirtySources=new ArrayList<>();
    for(int i=0;i<next.size();i++) {
      Source incoming=next.get(i),previous=find(sources,incoming.entity);
      if(previous!=null && previous.packed==incoming.packed &&
          !DynamicLightGeometry.moved(incoming.x,incoming.y,incoming.z,previous.x,previous.y,previous.z,movementThreshold)) {
        next.set(i,previous);
        if(force)dirtySources.add(previous);
      } else {
        changed=true;
        if(previous!=null)dirtySources.add(previous);
        dirtySources.add(incoming);
      }
    }
    for(Source previous:sources)if(find(next,previous.entity)==null){changed=true;dirtySources.add(previous);}
    if (changed) {
      ColoredLightDiagnostics.dynamicUpdate();
      sources = Collections.unmodifiableList(next); generation++;
      published = new Published(world, sources);
      // This fallback rebuilds only nearby sections, at a configurable cadence.
      if (!ColoredLightShaderBridge.active) {
        for (Source source : dirtySources) mark(mc, source);
      }
    }
  }
  private static Source find(List<Source> list,Entity entity) {
    for(Source source:list)if(source.entity==entity)return source;
    return null;
  }
  private void mark(Minecraft mc, Source source) {
    if (mc.renderGlobal == null) return;
    double r=source.radius+1;
    for(int sy=((int)Math.floor(source.y-r))>>4;sy<=((int)Math.floor(source.y+r))>>4;sy++)
      for(int sz=((int)Math.floor(source.z-r))>>4;sz<=((int)Math.floor(source.z+r))>>4;sz++)
        for(int sx=((int)Math.floor(source.x-r))>>4;sx<=((int)Math.floor(source.x+r))>>4;sx++)
          if(DynamicLightGeometry.affects(source.x,source.y,source.z,source.radius,sx,sy,sz))
            ColoredLightClient.INSTANCE.markRange(sx*16,sy*16,sz*16,sx*16,sy*16,sz*16);
  }
  public int sample(int x, int y, int z) {
    Published snapshot = published;
    World sampledWorld = snapshot.world;
    if (snapshot.sources.isEmpty() || sampledWorld == null || !ColoredLightConfig.enabled || !ColoredLightConfig.dynamicLights) return 0;
    // Most terrain samples are nowhere near a moving light. Do not allocate cache
    // keys, touch ThreadLocal state or evaluate visibility for those cells.
    double px=x+.5,py=y+.5,pz=z+.5;
    if(px<snapshot.minX || px>snapshot.maxX || py<snapshot.minY || py>snapshot.maxY || pz<snapshot.minZ || pz>snapshot.maxZ)return 0;
    Query query = queries.get();
    if (query.snapshot != snapshot) { query.snapshot = snapshot; query.values.clear(); }
    DynamicLightSampleCache values = query.values;
    int cached = values.get(x,y,z); if (cached >= 0) return cached;
    int rgb = 0;
    for (Source source : snapshot.sources) {
      double dx = x + 0.5 - source.x, dy = y + 0.5 - source.y, dz = z + 0.5 - source.z;
      double distanceSquared=dx*dx+dy*dy+dz*dz;
      if(distanceSquared>=source.radius*source.radius)continue;
      double distance = Math.sqrt(distanceSquared);
      if (!visible(sampledWorld, source, x + 0.5, y + 0.5, z + 0.5, distance)) continue;
      int base = ColoredLightApi.rgb(source.packed); float scale = (float) (1 - distance / source.radius);
      int value = Math.round((base >>> 16) * scale) << 16 | Math.round((base >> 8 & 255) * scale) << 8 | Math.round((base & 255) * scale);
      rgb = ru.givler.mbo.lighting.ColoredLightVolume.merge(rgb, value);
    }
    if (!ColoredLightConfig.enabled || published != snapshot) return 0;
    values.put(x,y,z,rgb);
    return rgb;
  }
  public void invalidateGeometry() {
    if(!ColoredLightShaderBridge.active)for(Source source:sources)mark(Minecraft.getMinecraft(),source);
  }
  public void invalidate() { generation++; published = new Published(world,sources); invalidated = true; }
  private static final class Published {
    final World world; final List<Source> sources;
    final double minX,minY,minZ,maxX,maxY,maxZ;
    Published(World world,List<Source> sources){
      this.world=world;this.sources=sources;
      double ax=Double.POSITIVE_INFINITY,ay=ax,az=ax,bx=Double.NEGATIVE_INFINITY,by=bx,bz=bx;
      for(Source s:sources){ax=Math.min(ax,s.x-s.radius);ay=Math.min(ay,s.y-s.radius);az=Math.min(az,s.z-s.radius);
        bx=Math.max(bx,s.x+s.radius);by=Math.max(by,s.y+s.radius);bz=Math.max(bz,s.z+s.radius);}
      minX=ax;minY=ay;minZ=az;maxX=bx;maxY=by;maxZ=bz;
    }
  }
  private static final class Query { Published snapshot; final DynamicLightSampleCache values = new DynamicLightSampleCache(); }
  private boolean visible(World sampledWorld, Source source, double x, double y, double z, double distance) {
    int steps = Math.max(1, (int) Math.ceil(distance * 2));
    int previousX=0,previousY=0,previousZ=0;boolean previous=false;
    for (int i = 1; i < steps; i++) {
      double t = i / (double) steps;
      int bx = (int) Math.floor(source.x + (x - source.x) * t), by = (int) Math.floor(source.y + (y - source.y) * t), bz = (int) Math.floor(source.z + (z - source.z) * t);
      // The half-block ray step often visits the same voxel more than once.
      // Preserve the exact original ray, but query each consecutive voxel once.
      if(previous && bx==previousX && by==previousY && bz==previousZ)continue;
      previous=true;previousX=bx;previousY=by;previousZ=bz;
      if (!sampledWorld.blockExists(bx, by,bz) || sampledWorld.getBlock(bx,by,bz).getLightOpacity(sampledWorld,bx,by,bz) >= 15) return false;
    }
    return true;
  }
}
