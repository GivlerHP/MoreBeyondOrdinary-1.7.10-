package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class WaterloggingFlowTransformer implements IClassTransformer, Opcodes {
  private static final String TARGET = "net.minecraft.block.BlockDynamicLiquid";
  private static final String DESCRIPTION = "(Lnet/minecraft/world/World;IIILjava/util/Random;)V";
  private static final String FLOW_DECAY_DESCRIPTION = "(Lnet/minecraft/world/World;IIII)I";
  private static final String CAN_FLOW_DESCRIPTION = "(Lnet/minecraft/world/World;III)Z";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (!TARGET.equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    boolean patchedTick = false;
    boolean patchedDecay = false;
    boolean patchedCanFlow = false;
    for (MethodNode method : node.methods) {
      String mappedName =
          FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      String mappedDescription = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
      if (CAN_FLOW_DESCRIPTION.equals(mappedDescription)
          && ("func_149809_q".equals(method.name)
              || "func_149809_q".equals(mappedName))) {
        LabelNode vanilla = new LabelNode();
        InsnList protection = new InsnList();
        protection.add(new VarInsnNode(ALOAD, 0));
        protection.add(new VarInsnNode(ALOAD, 1));
        protection.add(new VarInsnNode(ILOAD, 2));
        protection.add(new VarInsnNode(ILOAD, 3));
        protection.add(new VarInsnNode(ILOAD, 4));
        protection.add(
            new MethodInsnNode(
                INVOKESTATIC,
                "ru/givler/mbo/core/WaterloggingFlowHooks",
                "protectsFromWaterFlow",
                "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;III)Z",
                false));
        protection.add(new JumpInsnNode(IFEQ, vanilla));
        protection.add(new InsnNode(ICONST_0));
        protection.add(new InsnNode(IRETURN));
        protection.add(vanilla);
        method.instructions.insert(protection);
        patchedCanFlow = true;
        continue;
      }
      if (FLOW_DECAY_DESCRIPTION.equals(mappedDescription)
          && ("func_149810_a".equals(method.name)
              || "getSmallestFlowDecay".equals(method.name)
              || "func_149810_a".equals(mappedName)
              || "getSmallestFlowDecay".equals(mappedName))) {
        String adjacentSourceField = findAdjacentSourceField(method);
        if (adjacentSourceField != null) {
          LabelNode vanilla = new LabelNode();
          InsnList sourceHook = new InsnList();
          sourceHook.add(new VarInsnNode(ALOAD, 1));
          sourceHook.add(new VarInsnNode(ILOAD, 2));
          sourceHook.add(new VarInsnNode(ILOAD, 3));
          sourceHook.add(new VarInsnNode(ILOAD, 4));
          sourceHook.add(
              new MethodInsnNode(
                  INVOKESTATIC,
                  "ru/givler/mbo/core/WaterloggingFlowHooks",
                  "isWaterloggedFlowSource",
                  "(Lnet/minecraft/world/World;III)Z",
                  false));
          sourceHook.add(new JumpInsnNode(IFEQ, vanilla));
          sourceHook.add(new VarInsnNode(ALOAD, 0));
          sourceHook.add(new InsnNode(DUP));
          sourceHook.add(new FieldInsnNode(GETFIELD, node.name, adjacentSourceField, "I"));
          sourceHook.add(new InsnNode(ICONST_1));
          sourceHook.add(new InsnNode(IADD));
          sourceHook.add(new FieldInsnNode(PUTFIELD, node.name, adjacentSourceField, "I"));
          sourceHook.add(new InsnNode(ICONST_0));
          sourceHook.add(new InsnNode(IRETURN));
          sourceHook.add(vanilla);
          method.instructions.insert(sourceHook);
          patchedDecay = true;
        }
        continue;
      }
      if (!DESCRIPTION.equals(mappedDescription)
          || !("updateTick".equals(method.name)
              || "func_149674_a".equals(method.name)
              || "updateTick".equals(mappedName)
              || "func_149674_a".equals(mappedName))) continue;
      InsnList hook = new InsnList();
      hook.add(new VarInsnNode(ALOAD, 0));
      hook.add(new VarInsnNode(ALOAD, 1));
      hook.add(new VarInsnNode(ILOAD, 2));
      hook.add(new VarInsnNode(ILOAD, 3));
      hook.add(new VarInsnNode(ILOAD, 4));
      hook.add(
          new MethodInsnNode(
              INVOKESTATIC,
              "ru/givler/mbo/core/WaterloggingFlowHooks",
              "onLiquidTick",
              "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;III)V",
              false));
      method.instructions.insert(hook);
      for (AbstractInsnNode instruction = method.instructions.getFirst();
          instruction != null;
          instruction = instruction.getNext()) {
        if (instruction.getOpcode() != RETURN) continue;
        InsnList after = new InsnList();
        after.add(new VarInsnNode(ALOAD, 0));
        after.add(new VarInsnNode(ALOAD, 1));
        after.add(new VarInsnNode(ILOAD, 2));
        after.add(new VarInsnNode(ILOAD, 3));
        after.add(new VarInsnNode(ILOAD, 4));
        after.add(
            new MethodInsnNode(
                INVOKESTATIC,
                "ru/givler/mbo/core/WaterloggingFlowHooks",
                "afterLiquidTick",
                "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;III)V",
                false));
        method.instructions.insertBefore(instruction, after);
      }
      patchedTick = true;
    }
    if (!patchedTick || !patchedDecay || !patchedCanFlow) {
      System.err.println(
          "[MBO ASM] BlockDynamicLiquid patch incomplete: tick="
              + patchedTick
              + ", decay="
              + patchedDecay
              + ", canFlow="
              + patchedCanFlow);
      return bytes;
    }
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    node.accept(writer);
    System.out.println("[MBO ASM] Patched flowing water and waterlogged source decay");
    return writer.toByteArray();
  }

  private static String findAdjacentSourceField(MethodNode method) {
    for (AbstractInsnNode instruction = method.instructions.getFirst();
        instruction != null;
        instruction = instruction.getNext())
      if (instruction.getOpcode() == PUTFIELD && instruction instanceof FieldInsnNode) {
        FieldInsnNode field = (FieldInsnNode) instruction;
        if ("I".equals(field.desc)) return field.name;
      }
    return null;
  }
}
