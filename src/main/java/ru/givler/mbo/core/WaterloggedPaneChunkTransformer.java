package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class WaterloggedPaneChunkTransformer implements IClassTransformer, Opcodes {
  private static final String HOOK = "ru/givler/mbo/client/render/WaterloggedPaneChunkHooks";

  @Override public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null || !"net.minecraft.client.renderer.WorldRenderer".equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    int patched = 0;
    for (MethodNode method : node.methods) {
      MethodInsnNode anchor = null;
      int pass = -1;
      for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
        if (!(insn instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) insn;
        String mapped = mapped(call);
        if ((mapped.equals("renderBlockByRenderType") || mapped.equals("func_147805_b"))
            && FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(call.desc).equals("(Lnet/minecraft/block/Block;III)Z"))
          if (anchor == null) anchor = call;
        if (mapped.equals("canRenderInPass") && call.desc.equals("(I)Z")) {
          AbstractInsnNode load = previousCode(call);
          if (load instanceof VarInsnNode && load.getOpcode() == ILOAD) pass = ((VarInsnNode) load).var;
        }
      }
      if (anchor == null) continue;
      if (pass < 0) throw new IllegalStateException("MBO pane water render pass local missing");
      // Discover coordinates and renderer from the actual call, including OptiFine's locals.
      int[] locals = new int[5];
      AbstractInsnNode load = anchor;
      for (int i = 4; i >= 0; --i) {
        load = previousCode(load);
        if (!(load instanceof VarInsnNode) || load.getOpcode() != (i < 2 ? ALOAD : ILOAD))
          throw new IllegalStateException("MBO pane water block render arguments missing");
        locals[i] = ((VarInsnNode) load).var;
      }
      for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null;) {
        AbstractInsnNode next = insn.getNext();
        if (insn instanceof MethodInsnNode) {
          MethodInsnNode call = (MethodInsnNode) insn;
          String mapped = mapped(call);
          if (mapped.equals("getRenderBlockPass") || mapped.equals("func_149701_w")) {
            method.instructions.insertBefore(call, context(locals));
            method.instructions.set(call, new MethodInsnNode(INVOKESTATIC, HOOK, "renderPass",
                "(Lnet/minecraft/block/Block;Lnet/minecraft/client/renderer/RenderBlocks;III)I", false));
            ++patched;
          } else if (mapped.equals("canRenderInPass") && call.desc.equals("(I)Z")) {
            method.instructions.insertBefore(call, context(locals));
            method.instructions.set(call, new MethodInsnNode(INVOKESTATIC, HOOK, "canRender",
                "(Lnet/minecraft/block/Block;ILnet/minecraft/client/renderer/RenderBlocks;III)Z", false));
            ++patched;
          } else if (mapped.equals("renderBlockByRenderType") || mapped.equals("func_147805_b")) {
            method.instructions.insertBefore(call, new VarInsnNode(ILOAD, pass));
            method.instructions.set(call, new MethodInsnNode(INVOKESTATIC, HOOK, "render",
                "(Lnet/minecraft/client/renderer/RenderBlocks;Lnet/minecraft/block/Block;IIII)Z", false));
            ++patched;
          }
        }
        insn = next;
      }
    }
    if (patched < 3) throw new IllegalStateException("MBO pane water chunk anchors missing");
    ClassWriter writer = SafeClassWriter.create();
    node.accept(writer);
    System.out.println("[MBO ASM] Added pane water to sorted chunk geometry");
    return writer.toByteArray();
  }

  private static String mapped(MethodInsnNode call) {
    return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner, call.name, call.desc);
  }

  private static AbstractInsnNode previousCode(AbstractInsnNode insn) {
    do { insn = insn.getPrevious(); } while (insn != null && insn.getOpcode() < 0);
    return insn;
  }

  private static InsnList context(int[] locals) {
    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, locals[0]));
    for (int i = 2; i < 5; ++i) list.add(new VarInsnNode(ILOAD, locals[i]));
    return list;
  }
}
