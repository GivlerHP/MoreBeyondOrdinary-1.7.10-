package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class WaterloggingCameraTransformer implements IClassTransformer, Opcodes {
  private static final String TARGET = "net.minecraft.client.renderer.EntityRenderer";
  private static final String ACTIVE_RENDER_INFO = "net/minecraft/client/renderer/ActiveRenderInfo";
  private static final String DESCRIPTION =
      "(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;F)Lnet/minecraft/block/Block;";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (!TARGET.equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    int patched = 0;
    for (MethodNode method : node.methods)
      for (AbstractInsnNode instruction = method.instructions.getFirst();
          instruction != null;
          instruction = instruction.getNext()) {
        if (!(instruction instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) instruction;
        if (call.getOpcode() != INVOKESTATIC
            || !ACTIVE_RENDER_INFO.equals(call.owner)
            || !DESCRIPTION.equals(call.desc)) continue;
        call.owner = "ru/givler/mbo/core/WaterloggingCameraHooks";
        call.name = "getViewBlock";
        call.itf = false;
        patched++;
      }
    if (patched == 0) {
      System.err.println("[MBO ASM] EntityRenderer camera material calls were not found");
      return bytes;
    }
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    node.accept(writer);
    System.out.println("[MBO ASM] Patched " + patched + " waterlogged camera material calls");
    return writer.toByteArray();
  }
}
