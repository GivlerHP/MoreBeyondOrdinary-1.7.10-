package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Let vanilla body/head rotation see passenger motion rather than lift transport. */
public final class PlatformPassengerBodyTransformer implements IClassTransformer, Opcodes {
  @Override public byte[] transform(String name,String transformedName,byte[] bytes) {
    if (bytes==null || !"net.minecraft.entity.EntityBodyHelper".equals(transformedName)) return bytes;
    ClassNode node=new ClassNode(); new ClassReader(bytes).accept(node,0);
    int patched=0;
    for (MethodNode method:node.methods) {
      String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
      if (!"()V".equals(method.desc) || !("func_75664_a".equals(method.name) || "func_75664_a".equals(mapped))) continue;
      for (AbstractInsnNode instruction=method.instructions.getFirst();instruction!=null;) {
        AbstractInsnNode next=instruction.getNext();
        if (instruction instanceof FieldInsnNode && instruction.getOpcode()==GETFIELD) {
          FieldInsnNode field=(FieldInsnNode)instruction;
          String mappedField=FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(field.owner,field.name,field.desc);
          String hook=null;
          if ("prevPosX".equals(field.name) || "field_70169_q".equals(field.name) || "field_70169_q".equals(mappedField)) hook="animationPreviousX";
          if ("prevPosZ".equals(field.name) || "field_70166_s".equals(field.name) || "field_70166_s".equals(mappedField)) hook="animationPreviousZ";
          if (hook!=null && "D".equals(field.desc)) {
            method.instructions.set(field,new MethodInsnNode(INVOKESTATIC,
                "ru/givler/mbo/movingplatform/MovingPlatformTickHandler",hook,
                "(Lnet/minecraft/entity/EntityLivingBase;)D",false));
            ++patched;
          }
        }
        instruction=next;
      }
    }
    if (patched!=2) { System.err.println("[MBO ASM] Passenger body rotation anchors not found"); return bytes; }
    ClassWriter writer=new ClassWriter(0); node.accept(writer);
    return writer.toByteArray();
  }
}
