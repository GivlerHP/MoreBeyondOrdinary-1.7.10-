package ru.givler.mbo.integration.nei.core;
import ru.givler.mbo.core.SafeClassWriter;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Patches the tooltip box actually used by NEI/CodeChickenLib. */
public final class NeiTooltipFrameTransformer implements IClassTransformer, Opcodes {
    private static final String TARGET = "codechicken.lib.gui.GuiDraw";
    private static final String HOOK = "ru/givler/mbo/client/gui/TooltipFrameHooks";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !TARGET.equals(transformedName)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        boolean patched = false;
        for (MethodNode method : node.methods) {
            if (!"drawTooltipBox".equals(method.name)
                    || (!"(IIII)V".equals(method.desc) && !"(IIIIIIII)V".equals(method.desc))) continue;
            method.instructions.clear();
            method.tryCatchBlocks.clear();
            method.localVariables = null;
            method.instructions.add(new VarInsnNode(ILOAD, 0));
            method.instructions.add(new VarInsnNode(ILOAD, 1));
            method.instructions.add(new VarInsnNode(ILOAD, 2));
            method.instructions.add(new VarInsnNode(ILOAD, 3));
            method.instructions.add(new MethodInsnNode(INVOKESTATIC, HOOK, "drawNeiTooltipBox", "(IIII)V", false));
            method.instructions.add(new InsnNode(RETURN));
            patched = true;
        }
        if (!patched) {
            System.err.println("[MBO ASM] CodeChickenLib tooltip box was not found");
            return bytes;
        }
        ClassWriter writer = SafeClassWriter.create();
        node.accept(writer);
        System.out.println("[MBO ASM] Installed ornamental frame in CodeChickenLib tooltips");
        return writer.toByteArray();
    }
}
