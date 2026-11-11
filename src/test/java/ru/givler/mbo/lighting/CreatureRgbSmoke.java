package ru.givler.mbo.lighting;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import thaumcraft.common.entities.monster.EntityWisp;
import thaumcraft.api.aspects.Aspect;
import ru.givler.mbo.client.lighting.*;
import ru.givler.mbo.config.*;
import ru.givler.mbo.integration.thaumcraft.client.ThaumcraftRgbSources;

/** Actual wisp watcher/aspect changes, native mob states and override precedence. */
public final class CreatureRgbSmoke {
  static class Blaze extends EntityBlaze {
    boolean burning;
    Blaze(){super(null);}
    public boolean isBurning(){return burning;}
  }
  static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
  public static void main(String[] args)throws Exception {
    Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)uf.get(null);
    NativeRgbSources.register();ThaumcraftRgbSources.register();
    Blaze blaze=(Blaze)u.allocateInstance(Blaze.class);
    check(ColoredLightApi.entityEmission(blaze)==ColoredLightApi.emission(0xFFD080,10),"idle blaze");
    blaze.burning=true;check(ColoredLightApi.entityEmission(blaze)==ColoredLightApi.emission(0xFFD080,15),"charged blaze");
    blaze.isDead=true;check(ColoredLightApi.entityEmission(blaze)==0,"dead blaze");
    EntityMagmaCube magma=(EntityMagmaCube)u.allocateInstance(EntityMagmaCube.class);
    check(ColoredLightApi.entityEmission(magma)==ColoredLightApi.emission(0xFF6828,13),"magma cube");
    EntityWisp wisp=(EntityWisp)u.allocateInstance(EntityWisp.class);
    DataWatcher watcher=new DataWatcher(wisp);watcher.addObject(22,"");
    Field data=Entity.class.getDeclaredField("dataWatcher");data.setAccessible(true);data.set(wisp,watcher);
    int count=0;
    for(Aspect aspect:Aspect.aspects.values()){
      wisp.setType(aspect.getTag());
      check(ColoredLightApi.entityEmission(wisp)==ColoredLightApi.emission(aspect.getColor(),10),"wisp aspect "+aspect.getTag());count++;
    }
    wisp.setType("unknown_mbo_test");check(ColoredLightApi.entityEmission(wisp)==ColoredLightApi.emission(0xFFFFFF,10),"unknown wisp aspect");
    wisp.isDead=true;check(ColoredLightApi.entityEmission(wisp)==0,"dead wisp");wisp.isDead=false;
    boolean enabled=ColoredLightConfig.enabled,dynamic=ColoredLightConfig.dynamicLights;
    try {
      ColoredLightConfig.enabled=false;ColoredLightConfig.dynamicLights=false;
      check(ColoredLightAuto.entityLevel(0,wisp)==10,"ordinary source must remain when RGB is off");
      check(ColoredLightAuto.optifineLevel(15,wisp)==15,"stronger native light must remain");
    } finally {ColoredLightConfig.enabled=enabled;ColoredLightConfig.dynamicLights=dynamic;}
    Field rules=ColoredLightSources.class.getDeclaredField("entities");rules.setAccessible(true);Object previous=rules.get(null);
    Field names=EntityList.class.getDeclaredField("classToStringMapping");names.setAccessible(true);
    ((Map)names.get(null)).put(EntityWisp.class,"Thaumcraft.Wisp");
    String id=ColoredLightApi.entityId(wisp);check("thaumcraft:wisp".equals(id),"Thaumcraft wisp id normalization");
    Map<String,ColoredLightRule> overrides=new HashMap<>();
    com.google.gson.JsonObject rule=new com.google.gson.JsonObject();rule.addProperty("color","#123456");rule.addProperty("brightness",4);
    overrides.put(id,ColoredLightRule.parse(id,rule,false));
    try {rules.set(null,overrides);check(ColoredLightApi.entityEmission(wisp)==ColoredLightApi.emission(0x123456,4),"explicit wisp override must win");}
    finally {rules.set(null,previous);}
    System.out.println("PASS creature RGB: blaze states, magma, "+count+" live wisp aspects, unknown/dead sources, white-light fallback and user override");
  }
}
