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

public final class WaterloggingLiquidHeightTransformer implements IClassTransformer, Opcodes {
  private static final String TARGET = "net.minecraft.client.renderer.RenderBlocks";
  private static final String DESCRIPTION = "(IIILnet/minecraft/block/material/Material;)F";

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
          || !("getLiquidHeight".equals(method.name)
              || "func_147729_a".equals(method.name)
              || "getLiquidHeight".equals(mappedName)
              || "func_147729_a".equals(mappedName))) continue;
      LabelNode vanilla = new LabelNode();
      InsnList hook = new InsnList();
      hook.add(new VarInsnNode(ALOAD, 0));
      hook.add(new VarInsnNode(ILOAD, 1));
      hook.add(new VarInsnNode(ILOAD, 2));
      hook.add(new VarInsnNode(ILOAD, 3));
      hook.add(new VarInsnNode(ALOAD, 4));
      hook.add(
          new MethodInsnNode(
              INVOKESTATIC,
              "ru/givler/mbo/client/render/WaterloggedLiquidHeightHooks",
              "getHeightOverride",
              "(Lnet/minecraft/client/renderer/RenderBlocks;IIILnet/minecraft/block/material/Material;)F",
              false));
      hook.add(new InsnNode(DUP));
      hook.add(new InsnNode(FCONST_0));
      hook.add(new InsnNode(FCMPG));
      hook.add(new JumpInsnNode(IFLT, vanilla));
      hook.add(new InsnNode(FRETURN));
      hook.add(vanilla);
      hook.add(new InsnNode(POP));
      method.instructions.insert(hook);
      ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
      node.accept(writer);
      System.out.println("[MBO ASM] Patched liquid height below waterlogged blocks");
      return writer.toByteArray();
    }
    System.err.println("[MBO ASM] RenderBlocks.getLiquidHeight was not found");
    return bytes;
  }
}
