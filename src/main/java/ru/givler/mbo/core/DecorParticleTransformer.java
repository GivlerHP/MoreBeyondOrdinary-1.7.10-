package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class DecorParticleTransformer implements IClassTransformer,Opcodes {
 public byte[] transform(String name,String transformedName,byte[] bytes) {
  if(bytes==null||!"net.minecraft.client.renderer.RenderGlobal".equals(transformedName))return bytes;
  ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);boolean patched=false;
  for(MethodNode method:node.methods) {
   String mapped=cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
   if(!(method.name.equals("doSpawnParticle")||method.name.equals("func_147974_a")||mapped.equals("func_147974_a"))||!method.desc.startsWith("(Ljava/lang/String;DDDDDD)"))continue;
   for(AbstractInsnNode ins:method.instructions.toArray())if(ins.getOpcode()==ARETURN) {
    InsnList code=new InsnList();code.add(new VarInsnNode(ALOAD,1));
    code.add(new VarInsnNode(DLOAD,2));code.add(new VarInsnNode(DLOAD,4));code.add(new VarInsnNode(DLOAD,6));
    code.add(new MethodInsnNode(INVOKESTATIC,"ru/givler/mbo/core/DecorParticleHooks","resolve",
        "(Lnet/minecraft/client/particle/EntityFX;Ljava/lang/String;DDD)Lnet/minecraft/client/particle/EntityFX;",false));
    method.instructions.insertBefore(ins,code);patched=true;
   }
  }
  if(!patched)throw new IllegalStateException("Decor landing particle hook missing");
  ClassWriter writer=SafeClassWriter.create();node.accept(writer);return writer.toByteArray();
 }
}
