package ru.givler.mbo.integration.angelica.client;

import ru.givler.mbo.client.lighting.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import ru.givler.mbo.config.ColoredLightConfig;

/** Optional linkage: share Angelica's tracked sources and update cadence. */
public final class AngelicaDynamicLightBridge {
  private static Access access;
  private static boolean checked;
  private static long lastUpdate=Long.MIN_VALUE;
  private static Object lastWorld;
  private AngelicaDynamicLightBridge() {}
  public static boolean available() {
    if(!checked) {
      checked=true;
      if(!cpw.mods.fml.common.Loader.isModLoaded("angelica"))return false;
      try { access=new Access(); }
      catch(ClassNotFoundException absent) { }
      catch(ReflectiveOperationException error) { throw new IllegalStateException("Unsupported Angelica dynamic-light API",error); }
    }
    return access!=null;
  }
  public static boolean enabled() {
    if(!available())return false;
    try{return (Boolean)access.enabled.invoke(null);}
    catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
  }
  /** Use the backend's section queue so RGB and monochrome requests share its deduplication. */
  public static boolean schedule(int x,int y,int z) {
    if(!available())return false;
    try {
      Object renderer=access.renderer.get(null);
      if(renderer==null || !access.frustum.getBoolean(null))return false;
      access.schedule.invoke(null,renderer,x,y,z);
      return true;
    }catch(ReflectiveOperationException e){throw new IllegalStateException("Angelica RGB rebuild queue",e);}
  }
  /** Invoked after updateAll released the source lock, before terrain jobs are submitted. */
  public static void sync() {
    if(!available())return;
    Minecraft mc=Minecraft.getMinecraft();
    if(mc.theWorld==null || mc.renderViewEntity==null)return;
    try {
      Object manager=access.get.invoke(null);
      long update=access.update.getLong(manager);
      if(update==lastUpdate && lastWorld==mc.theWorld)return;
      lastUpdate=update;lastWorld=mc.theWorld;
      List<ColoredDynamicLights.Source> next=new ArrayList<>();
      if(ColoredLightConfig.enabled && ColoredLightConfig.dynamicLights && enabled()) {
        ReentrantReadWriteLock lock=(ReentrantReadWriteLock)access.lock.get(manager);
        lock.readLock().lock();
        try {
          for(Object source:(Object[])access.sources.get(manager)) {
            if(!(source instanceof Entity))continue;
            Entity entity=(Entity)source;
            if(entity.isDead || entity.worldObj!=mc.theWorld || entity.getDistanceSqToEntity(mc.renderViewEntity)>32*32
                || (Integer)access.luminance.invoke(source)<=0)continue;
            int packed=ColoredDynamicLights.emission(entity);
            if((packed>>>24)==0 || (packed&0xffffff)==0)continue;
            // Use the position accepted by Angelica's movement threshold, rather than
            // creating a second moving field between its actual source updates.
            next.add(new ColoredDynamicLights.Source(entity,packed,access.previousX.getDouble(source),
                access.previousY.getDouble(source)+((Double)access.y.invoke(source)-entity.posY),access.previousZ.getDouble(source)));
          }
        }finally{lock.readLock().unlock();}
      }
      ColoredDynamicLights.INSTANCE.fromBackend(mc,next);
      ColoredLightClient.INSTANCE.flushDynamicRebuilds(mc);
    }catch(ReflectiveOperationException e){throw new IllegalStateException("Angelica RGB source snapshot",e);}
  }
  private static Field field(Class<?> c,String name)throws ReflectiveOperationException {
    Field f=c.getDeclaredField(name);f.setAccessible(true);return f;
  }
  private static final class Access {
    final Method get,enabled,x,y,z,luminance,schedule;
    final Field sources,lock,update,renderer,frustum,previousX,previousY,previousZ;
    Access()throws ReflectiveOperationException {
      ClassLoader loader=AngelicaDynamicLightBridge.class.getClassLoader();
      Class<?> c=Class.forName("com.gtnewhorizons.angelica.dynamiclights.DynamicLights",false,loader);
      c.getDeclaredMethod("mbo$unifiedDynamicLights");
      Class<?> s=Class.forName("com.gtnewhorizons.angelica.dynamiclights.IDynamicLightSource",false,loader);
      get=c.getMethod("get");enabled=c.getMethod("isEnabled");
      renderer=field(c,"activeRenderer");frustum=field(c,"FrustumCullingEnabled");
      schedule=c.getMethod("scheduleChunkRebuild",Class.forName("com.gtnewhorizons.angelica.dynamiclights.IDynamicLightWorldRenderer",false,loader),int.class,int.class,int.class);
      sources=field(c,"lightSourceArray");lock=field(c,"lightSourcesLock");update=field(c,"lastUpdate");
      x=s.getMethod("angelica$getDynamicLightX");y=s.getMethod("angelica$getDynamicLightY");z=s.getMethod("angelica$getDynamicLightZ");
      luminance=s.getMethod("angelica$getLuminance");
      previousX=field(Entity.class,"angelica$prevX");previousY=field(Entity.class,"angelica$prevY");previousZ=field(Entity.class,"angelica$prevZ");
      System.out.println("[MBO] RGB backend: Angelica tracked dynamic lights");
    }
  }
}
