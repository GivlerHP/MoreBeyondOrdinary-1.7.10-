package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class WaterloggingEntityTransformer implements IClassTransformer, Opcodes {
  private static final String TARGET = "net.minecraft.entity.Entity";
  private static final String DESCRIPTION = "(Lnet/minecraft/block/material/Material;)Z";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (!TARGET.equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    for (MethodNode method : node.methods) {
      String mappedName =
          FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      String mappedDescription = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
      if (!DESCRIPTION.equals(mappedDescription)
          || !("isInsideOfMaterial".equals(method.name)
              || "func_70055_a".equals(method.name)
              || "isInsideOfMaterial".equals(mappedName)
              || "func_70055_a".equals(mappedName))) continue;
      LabelNode vanilla = new LabelNode();
      InsnList hook = new InsnList();
      hook.add(new VarInsnNode(ALOAD, 0));
      hook.add(new VarInsnNode(ALOAD, 1));
      hook.add(
          new MethodInsnNode(
              INVOKESTATIC,
              "ru/givler/mbo/core/WaterloggingEntityHooks",
              "isInsideWaterlogged",
              "(Lnet/minecraft/entity/Entity;Lnet/minecraft/block/material/Material;)Z",
              false));
      hook.add(new JumpInsnNode(IFEQ, vanilla));
      hook.add(new InsnNode(ICONST_1));
      hook.add(new InsnNode(IRETURN));
      hook.add(vanilla);
      method.instructions.insert(hook);
      ClassWriter writer =
          new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
      node.accept(writer);
      System.out.println("[MBO ASM] Patched entity waterlogged material checks");
      return writer.toByteArray();
    }
    System.err.println("[MBO ASM] Entity.isInsideOfMaterial was not found");
    return bytes;
  }
}
