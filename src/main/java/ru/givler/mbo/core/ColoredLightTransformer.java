package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Tints the existing packed vertex color; compatible with OptiFine's enlarged vertex layout. */
public final class ColoredLightTransformer implements IClassTransformer, Opcodes {
  private static final String HOOK = "ru/givler/mbo/client/lighting/ColoredLightRenderHooks";
  private static final String[] FACES = {"renderFaceYNeg", "renderFaceYPos", "renderFaceZNeg", "renderFaceZPos", "renderFaceXNeg", "renderFaceXPos"};
  private static final String[] SRG = {"func_147768_a", "func_147806_b", "func_147761_c", "func_147734_d", "func_147798_e", "func_147764_f"};

  @Override public byte[] transform(String name, String transformedName, byte[] bytes) {
    boolean faces = "net.minecraft.client.renderer.RenderBlocks".equals(transformedName);
    boolean tess = "net.minecraft.client.renderer.Tessellator".equals(transformedName);
    if (bytes == null || (!faces && !tess)) return bytes;
    ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0);
    // Avoid double injection when another coremod replays transformations.
    for (MethodNode method : node.methods) for (AbstractInsnNode insn : method.instructions.toArray()) {
      if (insn instanceof MethodInsnNode && HOOK.equals(((MethodInsnNode) insn).owner)) return bytes;
    }
    int patched = faces ? patchFaces(node) : patchTessellator(node);
    if (patched != (faces ? 10 : 1)) {
      System.err.println("[MBO ASM] Colored lighting unavailable in " + transformedName + ": " + patched);
      return bytes;
    }
    ClassWriter writer = SafeClassWriter.create(); node.accept(writer);
    System.out.println("[MBO ASM] Colored lighting: " + transformedName);
    return writer.toByteArray();
  }

  private static int patchFaces(ClassNode node) {
    int patched = 0;
    for (MethodNode method : node.methods) {
      String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      String desc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
      boolean plant = (mapped.equals("renderCrossedSquares") || mapped.equals("func_147746_l") || method.name.equals("renderCrossedSquares")
          || mapped.equals("renderBlockDoublePlant") || mapped.equals("func_147774_a") || method.name.equals("renderBlockDoublePlant"))
          && (desc.equals("(Lnet/minecraft/block/Block;III)Z") || desc.equals("(Lnet/minecraft/block/BlockDoublePlant;III)Z"));
      boolean liquid=(mapped.equals("renderBlockLiquid") || mapped.equals("func_147721_p") || method.name.equals("renderBlockLiquid")) && desc.equals("(Lnet/minecraft/block/Block;III)Z");
      boolean dispatch=(mapped.equals("renderBlockByRenderType") || mapped.equals("func_147805_b") || method.name.equals("renderBlockByRenderType")) && desc.equals("(Lnet/minecraft/block/Block;III)Z");
      boolean wholeBlock=plant || liquid || dispatch;
      if (!wholeBlock && !"(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V".equals(desc)) continue;
      for (int face = 0; face < (wholeBlock ? 1 : FACES.length); face++) {
        if (!wholeBlock && !method.name.equals(FACES[face]) && !method.name.equals(SRG[face]) && !mapped.equals(SRG[face]) && !mapped.equals(FACES[face])) continue;
        int saved = method.maxLocals, error = saved + 2; method.maxLocals = error + 1;
        LabelNode start = new LabelNode(), end = new LabelNode(), handler = new LabelNode();
        InsnList begin = new InsnList(); begin.add(new VarInsnNode(ALOAD, 0));
        for (int i = 0; i < 3; i++) {
          begin.add(new VarInsnNode(wholeBlock ? ILOAD : DLOAD, wholeBlock ? 2 + i : 2 + i * 2));
          if (wholeBlock) begin.add(new InsnNode(I2D));
        }
        if (wholeBlock) begin.add(new IntInsnNode(BIPUSH, dispatch?8:liquid?7:6)); else begin.add(new InsnNode(ICONST_0 + face));
        begin.add(new MethodInsnNode(INVOKESTATIC, HOOK, "begin", "(Lnet/minecraft/client/renderer/RenderBlocks;DDDI)J", false));
        begin.add(new VarInsnNode(LSTORE, saved)); begin.add(start);
        method.instructions.insert(begin);
        for (AbstractInsnNode insn : method.instructions.toArray()) if (insn.getOpcode() == RETURN || insn.getOpcode() == IRETURN) {
          method.instructions.insertBefore(insn, restore(saved));
        }
        method.instructions.add(end); method.instructions.add(handler);
        method.instructions.add(new VarInsnNode(ASTORE, error)); method.instructions.add(restore(saved));
        method.instructions.add(new VarInsnNode(ALOAD, error)); method.instructions.add(new InsnNode(ATHROW));
        method.tryCatchBlocks.add(new TryCatchBlockNode(start, end, handler, null));
        patched++;
      }
    }
    return patched;
  }

  private static InsnList restore(int saved) {
    InsnList list = new InsnList(); list.add(new VarInsnNode(LLOAD, saved));
    list.add(new MethodInsnNode(INVOKESTATIC, HOOK, "end", "(J)V", false)); return list;
  }

  private static int patchTessellator(ClassNode node) {
    String brightness = null;
    for (FieldNode field : node.fields) {
      String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(node.name, field.name, field.desc);
      if (field.desc.equals("I") && (field.name.equals("brightness") || field.name.equals("field_78401_l") || mapped.equals("field_78401_l"))) brightness = field.name;
    }
    if (brightness == null) return 0;
    int patched = 0;
    for (MethodNode method : node.methods) {
      String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      if (!method.desc.equals("(DDD)V") || (!method.name.equals("addVertex") && !method.name.equals("func_78377_a") && !mapped.equals("func_78377_a"))) continue;
      for (AbstractInsnNode insn : method.instructions.toArray()) {
        if (!(insn instanceof FieldInsnNode) || insn.getOpcode() != GETFIELD) continue;
        FieldInsnNode field = (FieldInsnNode) insn;
        if (field.owner.equals(node.name) && field.name.equals(brightness) && field.desc.equals("I")) {
          InsnList light = new InsnList(); light.add(new VarInsnNode(DLOAD, 1)); light.add(new VarInsnNode(DLOAD, 3)); light.add(new VarInsnNode(DLOAD, 5));
          light.add(new MethodInsnNode(INVOKESTATIC, HOOK, "brightnessVertex", "(IDDD)I", false));
          method.instructions.insert(insn, light); continue;
        }
        String fieldName = FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(field.owner, field.name, field.desc);
        if (!field.owner.equals(node.name) || !field.desc.equals("I")
            || (!field.name.equals("color") && !field.name.equals("field_78402_m") && !fieldName.equals("field_78402_m"))) continue;
        InsnList tint = new InsnList(); tint.add(new VarInsnNode(ALOAD, 0));
        tint.add(new FieldInsnNode(GETFIELD, node.name, brightness, "I"));
        tint.add(new VarInsnNode(DLOAD, 1)); tint.add(new VarInsnNode(DLOAD, 3)); tint.add(new VarInsnNode(DLOAD, 5));
        tint.add(new MethodInsnNode(INVOKESTATIC, HOOK, "tintVertex", "(IIDDD)I", false));
        method.instructions.insert(insn, tint); patched++;
      }
    }
    return patched;
  }
}
