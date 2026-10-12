package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Low decorative beds cannot use vanilla's 1.5-block sleeping name-label drop. */
public final class DecorSleepRenderTransformer implements IClassTransformer,Opcodes {
 public byte[] transform(String name,String transformedName,byte[] bytes) {
  if(bytes==null||!"net.minecraft.client.renderer.entity.RendererLivingEntity".equals(transformedName))return bytes;
  ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int patched=0;
  for(MethodNode method:node.methods) {
   String mapped=cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
   if(!(method.name.equals("func_96449_a")||mapped.equals("func_96449_a"))||!method.desc.contains("DDDLjava/lang/String;FD"))continue;
   for(AbstractInsnNode ins:method.instructions.toArray())if(ins instanceof LdcInsnNode&&Double.valueOf(1.5).equals(((LdcInsnNode)ins).cst)) {
    InsnList code=new InsnList();code.add(new VarInsnNode(ALOAD,1));
    code.add(new MethodInsnNode(INVOKESTATIC,"ru/givler/mbo/core/DecorSleepHooks","sleepingLabelDrop",
        "(DLnet/minecraft/entity/EntityLivingBase;)D",false));method.instructions.insert(ins,code);patched++;
   }
  }
  if(patched!=1)throw new IllegalStateException("Decor sleeping name label hook missing: "+patched);
  ClassWriter writer=SafeClassWriter.create();node.accept(writer);return writer.toByteArray();
 }
}
