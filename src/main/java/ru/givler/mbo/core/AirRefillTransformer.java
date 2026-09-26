package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Replaces vanilla's instant air reset after leaving water. */
public final class AirRefillTransformer implements IClassTransformer, Opcodes {
  private static final String ENTITY = "net.minecraft.entity.EntityLivingBase";
  private static final String HOOKS = "ru/givler/mbo/core/AirRefillHooks";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null || !ENTITY.equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    int patched = 0;
    for (MethodNode method : node.methods) {
      String mappedName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
          node.name, method.name, method.desc);
      if (!"onEntityUpdate".equals(method.name) && !"func_70030_z".equals(method.name)
          && !"onEntityUpdate".equals(mappedName) && !"func_70030_z".equals(mappedName)) {
        continue;
      }
      for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null;
          insn = insn.getNext()) {
        if (!(insn instanceof IntInsnNode) || insn.getOpcode() != SIPUSH
            || ((IntInsnNode) insn).operand != 300) continue;
        AbstractInsnNode next = insn.getNext();
        while (next != null && next.getOpcode() < 0) next = next.getNext();
        if (!(next instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) next;
        if (call.getOpcode() != INVOKEVIRTUAL || !"(I)V".equals(call.desc)) continue;
        InsnList replacement = new InsnList();
        replacement.add(new VarInsnNode(ALOAD, 0));
        replacement.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "refill",
            "(Lnet/minecraft/entity/EntityLivingBase;)I", false));
        method.instructions.insertBefore(insn, replacement);
        method.instructions.remove(insn);
        patched++;
      }
    }
    if (patched != 1) {
      System.err.println("[MBO ASM] Air refill reset not found: " + patched);
      return bytes;
    }
    ClassWriter writer = SafeClassWriter.create();
    node.accept(writer);
    return writer.toByteArray();
  }
}
