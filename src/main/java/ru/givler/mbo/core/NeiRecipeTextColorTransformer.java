package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Makes the recipe title and page counter readable on dark NEI backgrounds. */
public final class NeiRecipeTextColorTransformer implements IClassTransformer, Opcodes {
    private static final String TARGET = "codechicken.nei.recipe.GuiRecipe";
    private static final int NEI_DARK_GREY = 0x404040;

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !TARGET.equals(transformedName)) return bytes;

        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        int patched = 0;

        for (MethodNode method : node.methods) {
            if (!("drawGuiContainerForegroundLayer".equals(method.name)
                    || "func_146979_b".equals(method.name)) || !"(II)V".equals(method.desc)) continue;

            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null;) {
                AbstractInsnNode next = insn.getNext();
                if (insn instanceof LdcInsnNode
                        && Integer.valueOf(NEI_DARK_GREY).equals(((LdcInsnNode) insn).cst)) {
                    method.instructions.set(insn, new InsnNode(ICONST_M1));
                    patched++;
                }
                insn = next;
            }
        }

        if (patched != 2) {
            System.err.println("[MBO ASM] Expected 2 NEI recipe text colors, found " + patched);
            return bytes;
        }

        ClassWriter writer = SafeClassWriter.create();
        node.accept(writer);
        System.out.println("[MBO ASM] Changed NEI recipe title and page text to white");
        return writer.toByteArray();
    }
}
