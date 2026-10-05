package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class TickRateTransformer implements IClassTransformer, Opcodes {
  private static final String HOOKS = "ru/givler/mbo/core/TickRateHooks";

  @Override public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null) return null;
    boolean server = "net.minecraft.server.MinecraftServer".equals(transformedName);
    boolean timer = "net.minecraft.util.Timer".equals(transformedName);
    if (!server && !timer) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    int clocks = 0, sleeps = 0, rates = 0, limits = 0;
    for (MethodNode method : node.methods) {
      String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      if (!method.desc.equals("()V")
          || (server ? !method.name.equals("run")
              : !(mapped.equals("updateTimer") || mapped.equals("func_74275_a")))) continue;
      for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null;) {
        AbstractInsnNode next = insn.getNext();
        if (server && insn instanceof MethodInsnNode) {
          MethodInsnNode call = (MethodInsnNode) insn;
          String called = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner, call.name, call.desc);
          if (call.getOpcode() == INVOKESTATIC && call.desc.equals("()J")
              && (called.equals("getSystemTimeMillis") || called.equals("func_130071_aq"))) {
            method.instructions.set(call, new MethodInsnNode(INVOKESTATIC, HOOKS, "serverTimeMillis", "()J", false));
            ++clocks;
          } else if (call.owner.equals("java/lang/Thread") && call.name.equals("sleep") && call.desc.equals("(J)V")) {
            method.instructions.set(call, new MethodInsnNode(INVOKESTATIC, HOOKS, "sleepServer", "(J)V", false));
            ++sleeps;
          }
        } else if (timer && insn instanceof FieldInsnNode && insn.getOpcode() == GETFIELD) {
          FieldInsnNode field = (FieldInsnNode) insn;
          String fieldName = FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(field.owner, field.name, field.desc);
          if (field.desc.equals("F") && (fieldName.equals("ticksPerSecond") || fieldName.equals("field_74282_a"))) {
            method.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, HOOKS, "clientTicksPerSecond", "(F)F", false));
            ++rates;
          }
        } else if (timer && insn instanceof IntInsnNode && ((IntInsnNode) insn).operand == 10) {
          method.instructions.set(insn, new MethodInsnNode(INVOKESTATIC, HOOKS, "clientTickLimit", "()I", false));
          ++limits;
        }
        insn = next;
      }
    }
    if (server ? clocks != 2 || sleeps != 1 : rates != 1 || limits != 2)
      throw new IllegalStateException("MBO tick rate anchors missing in " + transformedName);
    ClassWriter writer = SafeClassWriter.create();
    node.accept(writer);
    System.out.println("[MBO ASM] Patched adjustable tick rate in " + transformedName);
    return writer.toByteArray();
  }
}
