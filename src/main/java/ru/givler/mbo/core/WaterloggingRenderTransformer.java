package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FieldInsnNode;
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
    if (bytes == null) return null;
    if ("net.minecraft.client.renderer.RenderBlocks".equals(transformedName))
      return patchTopUnderside(bytes);
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
      hook.add(new VarInsnNode(ALOAD, 0));
      hook.add(new VarInsnNode(ALOAD, 1));
      hook.add(new VarInsnNode(ILOAD, 2));
      hook.add(new VarInsnNode(ILOAD, 3));
      hook.add(new VarInsnNode(ILOAD, 4));
      hook.add(new VarInsnNode(ILOAD, 5));
      hook.add(
          new MethodInsnNode(
              INVOKESTATIC,
              "ru/givler/mbo/core/WaterloggingRenderHooks",
              "shouldHideFace",
              "(Lnet/minecraft/block/BlockLiquid;Lnet/minecraft/world/IBlockAccess;IIII)Z",
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

  private static byte[] patchTopUnderside(byte[] bytes) {
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    FieldNode access = null;
    for (FieldNode field : node.fields)
      if (FMLDeobfuscatingRemapper.INSTANCE.mapDesc(field.desc)
          .equals("Lnet/minecraft/world/IBlockAccess;")) access = field;
    if (access == null) throw new IllegalStateException("MBO RenderBlocks block access missing");
    for (MethodNode method : node.methods) {
      String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      if (!FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc)
              .equals("(Lnet/minecraft/block/Block;III)Z")
          || !(mapped.equals("renderBlockLiquid") || mapped.equals("func_147721_p"))) continue;
      int vertices = 0;
      AbstractInsnNode undersideStart = null, undersideEnd = null;
      for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
        if (!(insn instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) insn;
        String callName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner, call.name, call.desc);
        if (!call.desc.equals("(DDDDD)V")
            || !(callName.equals("addVertexWithUV") || callName.equals("func_78374_a"))) continue;
        // The first quad faces up; the second quad is its reversed underside.
        if (++vertices == 4) undersideStart = call.getNext();
        if (vertices == 8) { undersideEnd = call; break; }
      }
      if (undersideStart == null || undersideEnd == null)
        throw new IllegalStateException("MBO liquid top vertices missing");
      LabelNode afterUnderside = new LabelNode();
      InsnList hook = new InsnList();
      hook.add(new VarInsnNode(ALOAD, 1));
      hook.add(new VarInsnNode(ALOAD, 0));
      hook.add(new FieldInsnNode(GETFIELD, node.name, access.name, access.desc));
      hook.add(new VarInsnNode(ILOAD, 2));
      hook.add(new VarInsnNode(ILOAD, 3));
      hook.add(new VarInsnNode(ILOAD, 4));
      hook.add(new MethodInsnNode(INVOKESTATIC, "ru/givler/mbo/core/WaterloggingRenderHooks",
          "shouldHideTopUnderside", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockAccess;III)Z", false));
      hook.add(new JumpInsnNode(IFNE, afterUnderside));
      method.instructions.insertBefore(undersideStart, hook);
      method.instructions.insert(undersideEnd, afterUnderside);
      ClassWriter writer = SafeClassWriter.create();
      node.accept(writer);
      System.out.println("[MBO ASM] Patched water surface underside below opaque blocks");
      return writer.toByteArray();
    }
    throw new IllegalStateException("MBO RenderBlocks.renderBlockLiquid missing");
  }
}
