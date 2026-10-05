package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/** Suppress materialized elevator blocks that have passed its saved upper boundary. */
public final class PlatformClippingTransformer implements IClassTransformer, Opcodes {
  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null || !"net.minecraft.client.renderer.RenderBlocks".equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    FieldNode access = null;
    for (FieldNode field : node.fields)
      if (FMLDeobfuscatingRemapper.INSTANCE.mapDesc(field.desc)
          .equals("Lnet/minecraft/world/IBlockAccess;")) access = field;
    if (access == null) throw new IllegalStateException("MBO platform clip block access missing");
    for (MethodNode method : node.methods) {
      String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      if (!FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc)
              .equals("(Lnet/minecraft/block/Block;III)Z")
          || !(mapped.equals("renderBlockByRenderType") || mapped.equals("func_147805_b"))) continue;
      LabelNode visible = new LabelNode();
      InsnList hook = new InsnList();
      hook.add(new VarInsnNode(ALOAD, 1));
      hook.add(new VarInsnNode(ALOAD, 0));
      hook.add(new FieldInsnNode(GETFIELD, node.name, access.name, access.desc));
      hook.add(new VarInsnNode(ILOAD, 2));
      hook.add(new VarInsnNode(ILOAD, 3));
      hook.add(new VarInsnNode(ILOAD, 4));
      hook.add(new MethodInsnNode(INVOKESTATIC, "ru/givler/mbo/client/render/PlatformClippingHooks",
          "shouldHideBlock", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockAccess;III)Z", false));
      hook.add(new JumpInsnNode(IFEQ, visible));
      hook.add(new InsnNode(ICONST_0));
      hook.add(new InsnNode(IRETURN));
      hook.add(visible);
      method.instructions.insert(hook);
      ClassWriter writer = SafeClassWriter.create();
      node.accept(writer);
      System.out.println("[MBO ASM] Patched stationary elevator upper clipping");
      return writer.toByteArray();
    }
    throw new IllegalStateException("MBO platform clip render method missing");
  }
}
