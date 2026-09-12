package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class WaterloggingMovementTransformer implements IClassTransformer, Opcodes {
  private static final String TARGET = "net.minecraft.entity.Entity";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (!TARGET.equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    boolean patched = false;
    for (MethodNode method : node.methods) {
      String mappedName =
          FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      String mappedDescription = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
      if ("()Z".equals(mappedDescription)
          && ("handleWaterMovement".equals(method.name)
              || "func_70072_I".equals(method.name)
              || "handleWaterMovement".equals(mappedName)
              || "func_70072_I".equals(mappedName))) {
        for (AbstractInsnNode instruction = method.instructions.getFirst();
            instruction != null;
            instruction = instruction.getNext()) {
          if (instruction.getOpcode() != IRETURN) continue;
          InsnList hook = new InsnList();
          hook.add(new VarInsnNode(ALOAD, 0));
          hook.add(
              new MethodInsnNode(
                  INVOKESTATIC,
                  "ru/givler/mbo/core/WaterloggingEntityHooks",
                  "touchesWaterlogged",
                  "(Lnet/minecraft/entity/Entity;)Z",
                  false));
          hook.add(new InsnNode(IOR));
          method.instructions.insertBefore(instruction, hook);
        }
        patched = true;
      } else if ("(DZ)V".equals(mappedDescription)
          && ("updateFallState".equals(method.name)
              || "func_70064_a".equals(method.name)
              || "updateFallState".equals(mappedName)
              || "func_70064_a".equals(mappedName))) {
        InsnList hook = new InsnList();
        hook.add(new VarInsnNode(ALOAD, 0));
        hook.add(
            new MethodInsnNode(
                INVOKESTATIC,
                "ru/givler/mbo/core/WaterloggingEntityHooks",
                "resetFallDistanceInWaterlogged",
                "(Lnet/minecraft/entity/Entity;)V",
                false));
        method.instructions.insert(hook);
        patched = true;
      }
    }
    if (patched) {
      ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
      node.accept(writer);
      System.out.println("[MBO ASM] Patched waterlogged entity movement and falling");
      return writer.toByteArray();
    }
    System.err.println("[MBO ASM] Entity.handleWaterMovement was not found");
    return bytes;
  }
}
