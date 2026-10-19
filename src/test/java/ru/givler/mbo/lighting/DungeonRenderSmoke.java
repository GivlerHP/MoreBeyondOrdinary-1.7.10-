package ru.givler.mbo.lighting;

import java.io.*;
import java.lang.reflect.*;
import java.util.Arrays;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import ru.givler.mbo.core.DungeonAreaRenderTransformer;
import ru.givler.mbo.dungeon.DungeonAreaRecord;

public final class DungeonRenderSmoke {
  static class ClockWorld extends net.minecraft.client.multiplayer.WorldClient {
    long time;
    ClockWorld(){super(null,null,0,net.minecraft.world.EnumDifficulty.NORMAL,null);}
    @Override public long getTotalWorldTime(){return time;}
  }
  public static void run()throws Exception {
    String target="net.minecraft.client.renderer.EntityRenderer";
    ByteArrayOutputStream output=new ByteArrayOutputStream();
    try(InputStream in=DungeonRenderSmoke.class.getClassLoader().getResourceAsStream(target.replace('.','/')+".class")) {
      byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)output.write(buffer,0,n);
    }
    DungeonAreaRenderTransformer transformer=new DungeonAreaRenderTransformer();
    byte[] patched=transformer.transform(target,target,output.toByteArray());
    if(!Arrays.equals(patched,transformer.transform(target,target,patched)))throw new AssertionError("Duplicate dungeon hook");
    ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);int calls=0;
    for(MethodNode method:node.methods) {
      boolean hooked=false;
      for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.equals("ru/givler/mbo/client/render/DungeonAreaWorldRenderer")) {
        calls++;hooked=true;
        boolean terrain=false;
        for(AbstractInsnNode next=i.getNext();next!=null;next=next.getNext())if(next instanceof MethodInsnNode){terrain=((MethodInsnNode)next).owner.endsWith("RenderGlobal");break;}
        if(!terrain)throw new AssertionError("Wall hook must precede terrain invocation");
      }
      if(hooked)new Analyzer<BasicValue>(new BasicVerifier()).analyze(node.name,method);
    }
    if(calls==0)throw new AssertionError("No dungeon caller hooks");
    Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);
    ClockWorld world=(ClockWorld)((sun.misc.Unsafe)uf.get(null)).allocateInstance(ClockWorld.class);
    DungeonAreaRecord area=new DungeonAreaRecord();set(area,"type",DungeonAreaRecord.ILLUSORY);set(area,"activated",true);set(area,"activationTime",100L);
    world.time=100;if(!area.shouldRender() || area.fadeAlpha(world,0)!=1F)throw new AssertionError("Illusory wall disappeared at activation");
    world.time=110;if(area.fadeAlpha(world,0)!=.5F)throw new AssertionError("Illusory wall lost fade");
    world.time=120;if(area.fadeAlpha(world,0)!=0F)throw new AssertionError("Fade did not finish");
    set(area,"type",DungeonAreaRecord.PASSABLE);
    if(!area.shouldRender() || area.fadeAlpha(world,.5F)!=1F)throw new AssertionError("Passable wall must remain opaque outside lens");
    System.out.println("Dungeon walls: actual caller bytecode verified, before-terrain order, idempotence, one-second fade and opaque passable wall passed");
  }
  private static void set(Object value,String name,Object fieldValue)throws Exception {
    Field f=DungeonAreaRecord.class.getDeclaredField(name);f.setAccessible(true);f.set(value,fieldValue);
  }
}
