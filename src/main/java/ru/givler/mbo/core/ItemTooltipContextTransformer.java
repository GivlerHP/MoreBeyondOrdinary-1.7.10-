package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Marks GuiContainer's call to the inherited ItemStack tooltip renderer. */
public final class ItemTooltipContextTransformer implements IClassTransformer, Opcodes {
  private static final String TARGET = "net.minecraft.client.gui.inventory.GuiContainer";
  private static final String HOOK = "ru/givler/mbo/core/TooltipFrameHooks";
  private static final String DRAW_SCREEN_DESC = "(IIF)V";
  private static final String TOOLTIP_DESC =
      "(Lnet/minecraft/item/ItemStack;II)V";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null || !TARGET.equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    boolean patched = false;
    for (MethodNode method : node.methods) {
      if (!DRAW_SCREEN_DESC.equals(FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc)))
        continue;
      for (AbstractInsnNode insn = method.instructions.getFirst();
          insn != null;
          insn = insn.getNext()) {
        if (!(insn instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) insn;
        String callDesc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(call.desc);
        if (!TOOLTIP_DESC.equals(callDesc)) continue;
        method.instructions.insertBefore(
            call, new MethodInsnNode(INVOKESTATIC, HOOK, "beginItemTooltip", "()V", false));
        method.instructions.insert(
            call, new MethodInsnNode(INVOKESTATIC, HOOK, "endItemTooltip", "()V", false));
        patched = true;
        break;
      }
      if (patched) break;
    }
    if (!patched) {
      System.err.println("[MBO ASM] Item tooltip renderToolTip call was not found");
      return bytes;
    }
    ClassWriter writer = SafeClassWriter.create();
    node.accept(writer);
    System.out.println("[MBO ASM] Limited ornamental frame to ItemStack tooltips");
    return writer.toByteArray();
  }
}
