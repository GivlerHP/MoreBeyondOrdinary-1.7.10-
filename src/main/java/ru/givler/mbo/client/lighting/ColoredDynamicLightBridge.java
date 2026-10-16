package ru.givler.mbo.client.lighting;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import ru.givler.mbo.config.ColoredLightConfig;

/** Optional adapter to the existing FalseTweaks/OptiFine source manager. No second entity scan. */
public final class ColoredDynamicLightBridge {
  private static final String BASE = "com.falsepattern.falsetweaks.modules.dynlights.base.";
  private static Access access;
  private static boolean checked;
  private static boolean pending;
  private static long lastUpdate = Long.MIN_VALUE, lastGeneration = Long.MIN_VALUE;
  private static Object lastWorld;
  private ColoredDynamicLightBridge() {}

  public static boolean available() {
    if (!checked) {
      checked = true;
      try { access = new Access(); }
      catch (ClassNotFoundException absent) { /* Standalone fallback. */ }
      catch (ReflectiveOperationException incompatible) { System.err.println("[MBO] Dynamic light adapter unavailable: " + incompatible); }
    }
    return access != null;
  }
  public static boolean enabled() {
    if (!available()) return false;
    return flag(access.enabled);
  }
  private static boolean flag(Method method) {
    try { return (Boolean) method.invoke(null); }
    catch (ReflectiveOperationException error) { throw new IllegalStateException("FalseTweaks dynamic-light settings", error); }
  }
  public static int level(int original, Entity entity) {
    if (!available() || !ColoredLightConfig.enabled || !ColoredLightConfig.dynamicLights) return original;
    boolean hand = entity == Minecraft.getMinecraft().renderViewEntity;
    if (!flag(hand ? access.hand : access.entities)) return original;
    return ColoredLightAuto.optifineLevel(original, entity);
  }
  /** Called only after the backend released its write lock, before any new terrain work is queued. */
  public static void changed() { pending=true; sync(); }
  public static void sync() {
    if (!available()) return;
    Minecraft mc = Minecraft.getMinecraft();
    if (mc == null || mc.renderViewEntity == null) return;
    try {
      long update = access.time.getLong(null), generation = ColoredDynamicLights.INSTANCE.generation();
      if (!pending && update == lastUpdate && generation == lastGeneration && mc.theWorld == lastWorld) return;
      lastUpdate=update; lastWorld=mc.theWorld;
    } catch(IllegalAccessException error) { throw new IllegalStateException(error); }
    List<ColoredDynamicLights.Source> next = new ArrayList<>();
    try {
      ReentrantReadWriteLock lock = (ReentrantReadWriteLock) access.lock.get(null);
      lock.readLock().lock();
      try {
        Map<?, ?> lights = (Map<?, ?>) access.lights.get(null);
        if (ColoredLightConfig.enabled && ColoredLightConfig.dynamicLights && enabled()) for (Object light : lights.values()) {
          Entity entity = (Entity) access.entity.get(light);
          if (entity.isDead || entity.worldObj != mc.theWorld || entity.getDistanceSqToEntity(mc.renderViewEntity)>32*32) continue;
          if (!flag(entity == mc.renderViewEntity ? access.hand : access.entities)) continue;
          int level = access.lightLevel.getInt(light);
          if (level <= 0) continue;
          int packed = ColoredDynamicLights.emission(entity);
          if ((packed >>> 24) == 0 || (packed & 0xFFFFFF) == 0) continue;
          // FT measures block corners; RGB samples block centres.
          next.add(new ColoredDynamicLights.Source(entity, packed,
              access.x.getDouble(light) + .5, access.y.getDouble(light) + .5, access.z.getDouble(light) + .5));
        }
      } finally { lock.readLock().unlock(); }
    } catch (IllegalAccessException error) { throw new IllegalStateException("FalseTweaks dynamic-light sources", error); }
    ColoredDynamicLights.INSTANCE.fromBackend(mc, next);
    ColoredLightClient.INSTANCE.flushDynamicRebuilds(mc);
    pending=false; lastGeneration=ColoredDynamicLights.INSTANCE.generation();
  }
  /** Deduplicate backend and RGB invalidations, including cancellation of an in-flight old mesh. */
  public static void mark(RenderGlobal renderer, int x, int y, int z, int x2, int y2, int z2) {
    if (!available() || !ColoredLightConfig.enabled) {
      renderer.markBlockRangeForRenderUpdate(x,y,z,x2,y2,z2);
      return;
    }
    if (y2 < 0 || y > 255) return; // Backend's uninitialised previous position is a sentinel.
    ColoredLightClient.INSTANCE.markRange(x-2,y-2,z-2,x2+2,y2+2,z2+2);
    pending=true;
  }
  private static Field field(Class<?> type, String name) throws NoSuchFieldException {
    Field result = type.getDeclaredField(name); result.setAccessible(true); return result;
  }
  private static final class Access {
    final Field lights, lock, time, entity, lightLevel, x, y, z;
    final Method enabled, hand, entities;
    Access() throws ReflectiveOperationException {
      ClassLoader loader = ColoredDynamicLightBridge.class.getClassLoader();
      Class<?> manager = Class.forName(BASE + "DynamicLights", false, loader);
      Class<?> source = Class.forName(BASE + "DynamicLight", false, loader);
      manager.getDeclaredMethod("mbo$unifiedDynamicLights");
      source.getDeclaredMethod("mbo$unifiedDynamicLights");
      lights=field(manager,"mapDynamicLights"); lock=field(manager,"rwLock");
      time=field(manager,"timeUpdateMs");
      entity=field(source,"entity"); lightLevel=field(source,"lastLightLevel");
      x=field(source,"lastPosX"); y=field(source,"lastPosY"); z=field(source,"lastPosZ");
      Class<?> drivers=Class.forName("com.falsepattern.falsetweaks.modules.dynlights.DynamicLightsDrivers",false,loader);
      enabled=drivers.getMethod("isDynamicLights"); hand=drivers.getMethod("isDynamicHandLight"); entities=drivers.getMethod("isDynamicEntityLight");
      System.out.println("[MBO] RGB uses the FalseTweaks/OptiFine dynamic-light manager");
    }
  }
}
