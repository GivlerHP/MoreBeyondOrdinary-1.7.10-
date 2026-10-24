package ru.givler.mbo.integration.angelica.core;

import net.minecraft.launchwrapper.IClassTransformer;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class AngelicaSkyTransformer implements IClassTransformer,Opcodes {
  private static final String HOOK="ru/givler/mbo/integration/angelica/client/AngelicaSkyHooks";
  public byte[] transform(String name,String target,byte[] bytes) {
    boolean layer="com.prupe.mcpatcher.sky.SkyRenderer$Layer".equals(target),sky="com.prupe.mcpatcher.sky.SkyRenderer".equals(target);
    if(bytes==null||!(layer||sky))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0;
    for(MethodNode method:node.methods)for(AbstractInsnNode i:method.instructions.toArray())
      if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(HOOK))return bytes;
    for(MethodNode m:node.methods) {
      if(sky&&m.name.equals("setup")&&FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(m.desc).equals("(Lnet/minecraft/world/World;FF)V")) {
        InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,0));c.add(new VarInsnNode(FLOAD,1));c.add(new VarInsnNode(FLOAD,2));
        c.add(new MethodInsnNode(INVOKESTATIC,HOOK,"setup","(Lnet/minecraft/world/World;FF)V",false));m.instructions.insert(c);count++;
      }
      if(layer&&m.desc.equals("()Z")&&(m.name.equals("readFadeTimers")||m.name.equals("prepare"))) {
        LabelNode original=new LabelNode();InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,0));
        c.add(new MethodInsnNode(INVOKESTATIC,HOOK,m.name.equals("prepare")?"prepare":"validate","(Ljava/lang/Object;)I",false));
        c.add(new InsnNode(DUP));c.add(new JumpInsnNode(IFLT,original));c.add(new InsnNode(IRETURN));c.add(original);c.add(new InsnNode(POP));
        m.instructions.insert(c);count++;
      }
      if(layer&&m.name.equals("render")&&m.desc.equals("()Z")) {
        boolean replaced=false;
        for(AbstractInsnNode i:m.instructions.toArray())if(!replaced&&i instanceof MethodInsnNode) {
          MethodInsnNode call=(MethodInsnNode)i;
          if(call.name.equals("glRotatef")&&call.desc.equals("(FFFF)V")) {
            m.instructions.insertBefore(call,new VarInsnNode(ALOAD,0));call.owner=HOOK;call.name="rotate";call.desc="(FFFFLjava/lang/Object;)V";
            replaced=true;count++;
          }
        }
      }
    }
    if(count!=(layer?3:1))throw new IllegalStateException("Angelica sky anchors: "+target+" "+count);
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS){
      protected String getCommonSuperClass(String a,String b){return "java/lang/Object";}
    };node.accept(writer);return writer.toByteArray();
  }
}
