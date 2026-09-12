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

public final class WaterloggingRenderTransformer implements IClassTransformer, Opcodes {
  private static final String TARGET = "net.minecraft.block.BlockLiquid";
  private static final String DESCRIPTION =
      "(Lnet/minecraft/world/IBlockAccess;IIII)Z";

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
          || !("shouldSideBeRendered".equals(method.name)
              || "func_149646_a".equals(method.name)
              || "shouldSideBeRendered".equals(mappedName)
              || "func_149646_a".equals(mappedName))) continue;
      LabelNode vanilla = new LabelNode();
      InsnList hook = new InsnList();
      hook.add(new VarInsnNode(ALOAD, 1));
      hook.add(new VarInsnNode(ILOAD, 2));
      hook.add(new VarInsnNode(ILOAD, 3));
      hook.add(new VarInsnNode(ILOAD, 4));
      hook.add(
          new MethodInsnNode(
              INVOKESTATIC,
              "ru/givler/mbo/core/WaterloggingRenderHooks",
              "isWaterlogged",
              "(Lnet/minecraft/world/IBlockAccess;III)Z",
              false));
      hook.add(new JumpInsnNode(IFEQ, vanilla));
      hook.add(new InsnNode(ICONST_0));
      hook.add(new InsnNode(IRETURN));
      hook.add(vanilla);
      method.instructions.insert(hook);
      ClassWriter writer = SafeClassWriter.create();
      node.accept(writer);
      System.out.println("[MBO ASM] Patched liquid faces next to waterlogged blocks");
      return writer.toByteArray();
    }
    System.err.println("[MBO ASM] BlockLiquid.shouldSideBeRendered was not found");
    return bytes;
  }
}
