package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/** Keeps the registered vanilla skeleton and only replaces its two default combat goals. */
public final class SkeletonCombatTransformer implements IClassTransformer, Opcodes {
  private static final String BOW = "ru/givler/mbo/entity/ai/EntityAISkeletonBowAttack";
  private static final String MELEE = "ru/givler/mbo/entity/ai/EntityAISkeletonMeleeAttack";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null || !"net.minecraft.entity.monster.EntitySkeleton".equals(transformedName))
      return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    int constructors = 0, equipmentChecks = 0;
    for (MethodNode method : node.methods) {
      if ("<init>".equals(method.name)) {
        for (AbstractInsnNode insn = method.instructions.getFirst();
            insn != null;
            insn = insn.getNext()) {
          if (insn instanceof TypeInsnNode && insn.getOpcode() == NEW) {
            TypeInsnNode allocation = (TypeInsnNode) insn;
            String replacement = replacement(allocation.desc);
            if (replacement != null) allocation.desc = replacement;
          } else if (insn instanceof MethodInsnNode && insn.getOpcode() == INVOKESPECIAL) {
            MethodInsnNode call = (MethodInsnNode) insn;
            String replacement = replacement(call.owner);
            if ("<init>".equals(call.name) && replacement != null) {
              call.owner = replacement;
              constructors++;
            } else if ("<init>".equals(call.name)
                && (BOW.equals(call.owner) || MELEE.equals(call.owner))) {
              constructors++;
            }
          }
        }
      } else if ("setCombatTask".equals(method.name)
          || "func_85036_m".equals(method.name)
          || "func_85036_m"
              .equals(
                  FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
                      node.name, method.name, method.desc))) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
          AbstractInsnNode next = insn.getNext();
          if (insn instanceof MethodInsnNode
              && BOW.equals(((MethodInsnNode) insn).owner)
              && "isBow".equals(((MethodInsnNode) insn).name)) equipmentChecks++;
          if (insn instanceof FieldInsnNode && insn.getOpcode() == GETSTATIC) {
            FieldInsnNode field = (FieldInsnNode) insn;
            String mapped =
                FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(field.owner, field.name, field.desc);
            if ("bow".equals(field.name)
                || "field_151031_f".equals(mapped)
                || "field_151031_f".equals(field.name)) {
              AbstractInsnNode branch = next;
              while (branch != null && branch.getOpcode() < 0) branch = branch.getNext();
              if (!(branch instanceof JumpInsnNode) || branch.getOpcode() != IF_ACMPNE)
                throw new IllegalStateException("Unexpected skeleton bow comparison");
              method.instructions.set(
                  insn,
                  new MethodInsnNode(
                      INVOKESTATIC, BOW, "isBow", "(Lnet/minecraft/item/Item;)Z", false));
              ((JumpInsnNode) branch).setOpcode(IFEQ);
              equipmentChecks++;
            }
          }
          insn = next;
        }
      }
    }
    if (constructors != 2 || equipmentChecks != 1)
      throw new IllegalStateException(
          "Skeleton combat anchors: " + constructors + "/" + equipmentChecks);
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    node.accept(writer);
    return writer.toByteArray();
  }

  private static String replacement(String owner) {
    if ("net/minecraft/entity/ai/EntityAIArrowAttack".equals(owner)) return BOW;
    if ("net/minecraft/entity/ai/EntityAIAttackOnCollide".equals(owner)) return MELEE;
    return null;
  }
}
