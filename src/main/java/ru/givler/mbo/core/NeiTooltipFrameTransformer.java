package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Patches the tooltip box actually used by NEI/CodeChickenLib. */
public final class NeiTooltipFrameTransformer implements IClassTransformer, Opcodes {
    private static final String TARGET = "codechicken.lib.gui.GuiDraw";
    private static final String HOOK = "ru/givler/mbo/core/TooltipFrameHooks";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !TARGET.equals(transformedName)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        boolean patched = false;
        for (MethodNode method : node.methods) {
            if (!"drawTooltipBox".equals(method.name) || !"(IIII)V".equals(method.desc)) continue;
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
            break;
        }
        if (!patched) {
            System.err.println("[MBO ASM] CodeChickenLib tooltip box was not found");
            return bytes;
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        System.out.println("[MBO ASM] Installed ornamental frame in CodeChickenLib tooltips");
        return writer.toByteArray();
    }
}
