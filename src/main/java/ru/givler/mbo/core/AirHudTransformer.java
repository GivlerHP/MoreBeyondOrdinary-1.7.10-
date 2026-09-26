package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Uses Forge's existing bubble renderer during gradual air recovery. */
public final class AirHudTransformer implements IClassTransformer, Opcodes {
  private static final String HUD = "net.minecraftforge.client.GuiIngameForge";
  private static final String HOOKS = "ru/givler/mbo/core/AirHudHooks";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null || !HUD.equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    int patched = 0;
    for (MethodNode method : node.methods) {
      if (!"renderAir".equals(method.name) || !"(II)V".equals(method.desc)) continue;
      for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null;
          insn = insn.getNext()) {
        if (!(insn instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) insn;
        if (call.getOpcode() != INVOKEVIRTUAL
            || (!"(Lnet/minecraft/block/material/Material;)Z".equals(call.desc)
                && !"(Lnet/minecraft/block/material/Material;)Z".equals(
                    FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(call.desc)))) continue;
        call.setOpcode(INVOKESTATIC);
        call.owner = HOOKS;
        call.name = "shouldRender";
        call.desc = "(Lnet/minecraft/entity/Entity;Lnet/minecraft/block/material/Material;)Z";
        patched++;
      }
    }
    if (patched != 1) {
      System.err.println("[MBO ASM] Air HUD condition not found: " + patched);
      return bytes;
    }
    ClassWriter writer = SafeClassWriter.create();
    node.accept(writer);
    return writer.toByteArray();
  }
}
