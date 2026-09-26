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
import org.objectweb.asm.tree.VarInsnNode;

/** Replaces vanilla fire icons in the two entity-burning render paths. */
public final class ColoredBurningTransformer implements IClassTransformer, Opcodes {
  private static final String RENDER = "net.minecraft.client.renderer.entity.Render";
  private static final String ITEM = "net.minecraft.client.renderer.ItemRenderer";
  private static final String HOOKS = "ru/givler/mbo/client/render/ColoredBurningRenderHooks";
  private static final String ICON_DESC = "(I)Lnet/minecraft/util/IIcon;";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null || (!RENDER.equals(transformedName) && !ITEM.equals(transformedName))) {
      return bytes;
    }
    boolean entityRender = RENDER.equals(transformedName);
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    int patched = 0;
    for (MethodNode method : node.methods) {
      String methodName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
          node.name, method.name, method.desc);
      if (entityRender && !matches(method.name, methodName, "renderEntityOnFire", "func_76977_a")
          || !entityRender && !matches(method.name, methodName,
              "renderFireInFirstPerson", "func_78442_d")) continue;
      for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null;
          insn = insn.getNext()) {
        if (!(insn instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) insn;
        String desc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(call.desc);
        if (call.getOpcode() != INVOKEVIRTUAL
            || (!ICON_DESC.equals(call.desc) && !ICON_DESC.equals(desc))) continue;
        if (entityRender) method.instructions.insertBefore(call, new VarInsnNode(ALOAD, 1));
        call.setOpcode(INVOKESTATIC);
        call.owner = HOOKS;
        call.name = entityRender ? "entityIcon" : "firstPersonIcon";
        call.desc = entityRender
            ? "(Lnet/minecraft/block/BlockFire;ILnet/minecraft/entity/Entity;)Lnet/minecraft/util/IIcon;"
            : "(Lnet/minecraft/block/BlockFire;I)Lnet/minecraft/util/IIcon;";
        patched++;
      }
    }
    int expected = entityRender ? 2 : 1;
    if (patched != expected) {
      System.err.println("[MBO ASM] Colored burning icons not found in "
          + transformedName + ": " + patched);
      return bytes;
    }
    ClassWriter writer = SafeClassWriter.create();
    node.accept(writer);
    return writer.toByteArray();
  }

  private static boolean matches(String raw, String mapped, String deobf, String srg) {
    return deobf.equals(raw) || srg.equals(raw) || deobf.equals(mapped) || srg.equals(mapped);
  }
}
