package ru.givler.mbo.core;

import java.nio.file.*;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.tree.*;

/** Uses classes extracted with Crucible's own ClassPatchManager, never vanilla substitutes. */
public final class CrucibleCompatibilitySmoke {
  public static void main(String[] args) throws Exception {
    ProductionTransformersSmoke.Mapping mapping = new ProductionTransformersSmoke.Mapping(
        Paths.get(args[0], "notch-mcp.srg"));
    ProductionTransformersSmoke.Mapping srg = new ProductionTransformersSmoke.Mapping(
        Paths.get(args[0], "notch-srg.srg"));
    // EntityLivingBase invokes inherited Entity methods with its own owner in the constant pool.
    for(java.util.Map.Entry<String,String> entry:new java.util.HashMap<>(mapping.methods).entrySet())
      if(entry.getKey().startsWith("sa/"))mapping.methods.put("sv/"+entry.getKey().substring(3),entry.getValue());
    ProductionTransformersSmoke.CachedMaps methodMaps=new ProductionTransformersSmoke.CachedMaps();
    for(java.util.Map.Entry<String,String> entry:srg.methods.entrySet()) {
      int split=entry.getKey().lastIndexOf('/');
      String owner=entry.getKey().substring(0,split),member=entry.getKey().substring(split+1);
      methodMaps.computeIfAbsent(owner,key->new java.util.HashMap<>()).put(member,entry.getValue());
    }
    methodMaps.computeIfAbsent("sv",key->new java.util.HashMap<>()).putAll(methodMaps.get("sa"));
    Object remapper=cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper.INSTANCE;
    String[] fields={"classNameBiMap","methodNameMaps","fieldNameMaps"};
    Object[] values={com.google.common.collect.HashBiMap.create(mapping.classes),
        methodMaps,new ProductionTransformersSmoke.CachedMaps()};
    for(int i=0;i<fields.length;i++) {
      java.lang.reflect.Field field=remapper.getClass().getDeclaredField(fields[i]);
      field.setAccessible(true);field.set(remapper,values[i]);
    }
    verify(args[1], mapping, "net.minecraft.server.MinecraftServer", new TickRateTransformer(),
        "ru/givler/mbo/core/TickRateHooks", "crucibleTickNanos", 1);
    IClassTransformer piston = (IClassTransformer)Class.forName(
        "ru.givler.caveabyss.core.PistonHeightTransformer").newInstance();
    verify(args[1], mapping, "net.minecraft.block.BlockPistonBase", piston,
        "ru/givler/caveabyss/core/MinusOneHooks", "minimumY", 2);
    verify(args[1], mapping, "net.minecraft.entity.EntityLivingBase", new AirRefillTransformer(),
        "ru/givler/mbo/core/AirRefillHooks", "refillToLimit", 1);
    java.lang.reflect.Field side=cpw.mods.fml.relauncher.FMLLaunchHandler.class.getDeclaredField("side");
    side.setAccessible(true);Object previousSide=side.get(null);
    try {
      side.set(null,cpw.mods.fml.relauncher.Side.SERVER);
      IClassTransformer cache=(IClassTransformer)Class.forName(
          "ru.givler.caveabyss.core.NegativeRenderTransformer").newInstance();
      verify(args[1],mapping,"net.minecraft.world.ChunkCache",cache,
          "ru/givler/caveabyss/core/MinusOneHooks","getBlock",1);
      byte[] entity=ProductionTransformersSmoke.resource("net/minecraft/entity/Entity");
      if(new BoatRiderRenderTransformer().transform("sa","net.minecraft.entity.Entity",entity)!=entity)
        throw new AssertionError("Client camera must not patch dedicated-server Entity");
    } finally {side.set(null,previousSide);}
    for (int rate : new int[]{1,20,60,200,1000}) {
      TickRateHooks.setServerRate(rate);
      if (TickRateHooks.crucibleTickNanos(50000000) != (int)(1000000000L/rate))
        throw new AssertionError("Crucible interval at " + rate);
    }
    TickRateHooks.setServerRate(20);
    if (TickRateHooks.crucibleTickNanos(40000000) != 40000000)
      throw new AssertionError("Crucible configured pacing must survive at default rate");
    TickRateSmoke.check();
    System.out.println("CRUCIBLE_COMPAT_OK: actual patched classes, both piston scans, nanosecond pacing and Forge regression");
  }
  private static void verify(String root, ProductionTransformersSmoke.Mapping mapping,
      String name, IClassTransformer transformer, String owner, String hook, int expected) throws Exception {
    byte[] raw=Files.readAllBytes(Paths.get(root,name.replace('.','/')+".class"));
    if(name.equals("net.minecraft.world.ChunkCache")) {
      java.lang.reflect.Field side=cpw.mods.fml.common.asm.transformers.SideTransformer.class.getDeclaredField("SIDE");
      side.setAccessible(true);Object previous=side.get(null);
      try {
        side.set(null,"SERVER");
        raw=new cpw.mods.fml.common.asm.transformers.SideTransformer().transform(name,name,raw);
      } finally {side.set(null,previous);}
    }
    byte[] patchedRaw=transformer.transform(new org.objectweb.asm.ClassReader(raw).getClassName(),name,raw);
    ProductionTransformersSmoke.verify(patchedRaw);
    byte[] input=ProductionTransformersSmoke.remap(raw,mapping);
    byte[] output=transformer.transform(name,name,input);
    ProductionTransformersSmoke.verify(output);
    int count=0;
    for(MethodNode method:ProductionTransformersSmoke.read(output).methods)
      for(AbstractInsnNode insn:method.instructions.toArray())if(insn instanceof MethodInsnNode) {
        MethodInsnNode call=(MethodInsnNode)insn;
        if(call.owner.equals(owner)&&call.name.equals(hook))count++;
        if(name.endsWith("MinecraftServer")&&call.owner.equals(owner)
            &&(call.name.equals("sleepServer")||call.name.equals("serverTimeMillis")))
          throw new AssertionError("Crucible real clock must stay unscaled");
      }
    if(count!=expected)throw new AssertionError(name+" hook count "+count);
  }
}
