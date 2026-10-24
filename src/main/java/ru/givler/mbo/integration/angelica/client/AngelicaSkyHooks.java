package ru.givler.mbo.integration.angelica.client;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import ru.givler.mbo.client.texture.CustomSkyRule;

/** Keeps Angelica's texture/reload/GL lifecycle, supplies OptiFine E7 layer semantics. */
public final class AngelicaSkyHooks {
  public static final AngelicaSkyHooks INSTANCE=new AngelicaSkyHooks();
  private static final Map<Object,State> rules=Collections.synchronizedMap(new WeakHashMap<Object,State>());
  private static World world;
  private static float partial;
  private static Field properties,brightness,speed;
  private static Method values,errorMethod;
  private static Field skyboxEnabled,skyEnabled,packEnabled;
  private static boolean enabled() {
    try {
      if(skyboxEnabled==null) {
        Class<?> type=Class.forName("jss.notfine.config.MCPatcherForgeConfig$BetterSkies");
        skyboxEnabled=type.getField("skybox");skyEnabled=type.getField("enabled");
        packEnabled=Class.forName("com.gtnewhorizons.angelica.config.AngelicaConfig").getField("enableMCPatcherForgeFeatures");
      }
      return skyboxEnabled.getBoolean(null)&&skyEnabled.getBoolean(null)&&packEnabled.getBoolean(null);
    }catch(ReflectiveOperationException error){throw new IllegalStateException("Cannot read sky settings",error);}
  }
  private static final class State {
    CustomSkyRule rule;World lastWorld;float position=Float.NaN;long time;
  }
  private AngelicaSkyHooks() {}
  private static void bind(Object layer)throws ReflectiveOperationException {
    if(properties!=null)return;
    Class<?> type=layer.getClass();
    properties=type.getDeclaredField("properties");properties.setAccessible(true);
    brightness=type.getDeclaredField("brightness");brightness.setAccessible(true);
    speed=type.getDeclaredField("speed");speed.setAccessible(true);
    values=properties.getType().getMethod("getProperties");
    errorMethod=properties.getType().getMethod("error",String.class,Object[].class);
  }
  /** -1 lets celestial-object properties keep their original non-fading behavior. */
  public static int validate(Object layer) {
    try {
      bind(layer);Properties p=(Properties)values.invoke(properties.get(layer));
      if("false".equalsIgnoreCase(p.getProperty("fade")))return -1;
      State state=new State();
      try {state.rule=new CustomSkyRule(p);}
      catch(IllegalArgumentException bad){errorMethod.invoke(properties.get(layer),"MBO custom sky: %s",new Object[]{bad.getMessage()});return 0;}
      rules.put(layer,state);System.out.println("[MBO] Custom sky E7 rules: "+properties.get(layer));return 1;
    }catch(ReflectiveOperationException error){throw new IllegalStateException("Cannot bind Angelica sky layer",error);}
  }
  public static void setup(World current,float partialTicks,float angle){world=current;partial=partialTicks;}
  @cpw.mods.fml.common.eventhandler.SubscribeEvent public void unload(net.minecraftforge.event.world.WorldEvent.Unload event) {
    if(!event.world.isRemote)return;
    world=null;
    synchronized(rules){for(State state:rules.values()){state.lastWorld=null;state.position=Float.NaN;}}
  }
  public static int prepare(Object layer) {
    if(!enabled())return 0;
    State state=rules.get(layer);if(state==null)return -1;
    World current=world;if(current==null)return 0;
    Entity eye=Minecraft.getMinecraft().renderViewEntity;if(eye==null)return 0;
    float position=1;
    if(state.rule.biomes!=null||state.rule.heights!=null) {
      BiomeGenBase biome=current.getBiomeGenForCoords((int)Math.floor(eye.posX),(int)Math.floor(eye.posZ));
      float target=biome!=null&&state.rule.position(biome.biomeID,biome.biomeName,(int)Math.floor(eye.posY))?1:0;
      long now=System.nanoTime();
      if(state.lastWorld!=current||Float.isNaN(state.position))state.position=target;
      else state.position=CustomSkyRule.smooth(state.position,target,(now-state.time)/1e9f,state.rule.transition);
      state.time=now;state.lastWorld=current;position=state.position;
    }
    float rain=current.getRainStrength(partial),thunder=current.getWeightedThunderStrength(partial);
    if(rain>0)thunder/=rain;
    float value=position*state.rule.brightness(current.getWorldTime(),rain,thunder);
    try {brightness.setFloat(layer,value);}
    catch(IllegalAccessException error){throw new IllegalStateException(error);}
    return value>1e-4f?1:0;
  }
  public static void rotate(float angle,float x,float y,float z,Object layer) {
    try {
      float rate=speed.getFloat(layer);
      if(world!=null&&rate!=Math.round(rate)) {
        long day=Math.floorDiv(world.getWorldTime()+18000,24000);
        angle+=(float)((day*(double)(rate%1))%1)*360;
      }
      org.lwjgl.opengl.GL11.glRotatef(angle,x,y,z);
    }catch(IllegalAccessException error){throw new IllegalStateException(error);}
  }
}
