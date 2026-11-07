package ru.givler.mbo.core;

import java.io.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

/** Checks the actual vanilla and MF2 entry points, both transformer orders and repeat application. */
public final class PortableCarryTransformerSmoke implements Opcodes {
  private static byte[] bytes(String name)throws Exception {
    try(InputStream in=PortableCarryTransformerSmoke.class.getResourceAsStream("/"+name.replace('.','/')+".class")) {
      if(in==null)throw new AssertionError("Missing fixture "+name);
      ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int count;
      while((count=in.read(buffer))!=-1)out.write(buffer,0,count);return out.toByteArray();
    }
  }
  public static void main(String[] args)throws Exception {
    PortableCarryTransformer transformer=new PortableCarryTransformer();
    for(String name:new String[]{"net.minecraft.client.renderer.ItemRenderer","mods.battlegear2.client.utils.BattlegearRenderHelper"}) {
      byte[] original=bytes(name),patched=transformer.transform(name,name,original);
      if(patched==original)throw new AssertionError("Carry entry point missing: "+name);
      if(transformer.transform(name,name,patched)!=patched)throw new AssertionError("Duplicate carry hooks");
      ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);int hooks=0;
      for(MethodNode method:node.methods) {
        boolean carried=false;
        for(AbstractInsnNode insn:method.instructions.toArray())if(insn instanceof MethodInsnNode && ((MethodInsnNode)insn).owner.equals("ru/givler/mbo/client/render/light/PortableCarry")){hooks++;carried=true;}
        if(carried)new Analyzer(new BasicVerifier()).analyze(node.name,method);
      }
      if(hooks!=(name.endsWith("ItemRenderer")?1:2))throw new AssertionError("Wrong hand hook count "+name+": "+hooks);
    }
    String vanilla="net.minecraft.client.renderer.ItemRenderer";
    for(boolean mboFirst:new boolean[]{false,true}) {
      byte[] input=bytes(vanilla);
      if(mboFirst)input=transformer.transform(vanilla,vanilla,input);
      input=new mods.battlegear2.coremod.transformers.ItemRendererTransformer().transform(vanilla,vanilla,input);
      if(!mboFirst)input=transformer.transform(vanilla,vanilla,input);
      ClassNode node=new ClassNode();new ClassReader(input).accept(node,0);
      for(MethodNode method:node.methods)if(method.name.equals("renderItemInFirstPerson"))new Analyzer(new BasicVerifier()).analyze(node.name,method);
    }
    System.out.println("PORTABLE_CARRY_HOOKS_OK: vanilla and MF2 hand passes, both transformer orders");
  }
}
