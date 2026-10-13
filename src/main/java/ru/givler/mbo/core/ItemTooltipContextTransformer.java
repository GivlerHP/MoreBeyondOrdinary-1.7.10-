package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Marks the tooltip method itself, preserving GuiContainer instruction patterns used by NEI. */
public final class ItemTooltipContextTransformer implements IClassTransformer, Opcodes {
  private static final String TARGET = "net.minecraft.client.gui.GuiScreen";
  private static final String HOOK = "ru/givler/mbo/core/TooltipFrameHooks";
  private static final String TOOLTIP_DESC =
      "(Lnet/minecraft/item/ItemStack;II)V";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null || !TARGET.equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    boolean patched = false;
    for (MethodNode method : node.methods) {
      if (!TOOLTIP_DESC.equals(FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc)))
        continue;
      for(AbstractInsnNode insn:method.instructions.toArray()) {
        if(insn instanceof MethodInsnNode && HOOK.equals(((MethodInsnNode)insn).owner)
            && "beginItemTooltip".equals(((MethodInsnNode)insn).name))return bytes;
      }
      LabelNode start=new LabelNode(),end=new LabelNode(),handler=new LabelNode();
      InsnList entry=new InsnList();entry.add(new MethodInsnNode(INVOKESTATIC,HOOK,"beginItemTooltip","()V",false));entry.add(start);
      method.instructions.insert(entry);
      for(AbstractInsnNode insn:method.instructions.toArray())if(insn.getOpcode()==RETURN)
        method.instructions.insertBefore(insn,new MethodInsnNode(INVOKESTATIC,HOOK,"endItemTooltip","()V",false));
      int error=method.maxLocals++;
      method.instructions.add(end);method.instructions.add(handler);
      method.instructions.add(new VarInsnNode(ASTORE,error));
      method.instructions.add(new MethodInsnNode(INVOKESTATIC,HOOK,"endItemTooltip","()V",false));
      method.instructions.add(new VarInsnNode(ALOAD,error));method.instructions.add(new InsnNode(ATHROW));
      method.tryCatchBlocks.add(new TryCatchBlockNode(start,end,handler,null));
      patched=true;break;
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
