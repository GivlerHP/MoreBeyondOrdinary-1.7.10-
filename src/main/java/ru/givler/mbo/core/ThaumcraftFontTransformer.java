package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * TCFontRenderer is a standalone copy of the old vanilla renderer and therefore
 * bypasses Minecraft.fontRenderer. Keep its book/link parser, but route glyph
 * drawing and width calculations through the renderer installed by MBO.
 */
public final class ThaumcraftFontTransformer implements IClassTransformer, Opcodes {
    private static final String TARGET = "thaumcraft.client.lib.TCFontRenderer";
    private static final String HOOK = "ru/givler/mbo/core/ThaumcraftFontHooks";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !TARGET.equals(transformedName)) return bytes;

        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        int patched = 0;
        for (MethodNode method : node.methods) {
            if ("renderString".equals(method.name)
                    && "(Ljava/lang/String;IIIZ)I".equals(method.desc)) {
                replace(method, drawBody());
                patched++;
            } else if ("getStringWidth".equals(method.name)
                    && "(Ljava/lang/String;)I".equals(method.desc)) {
                replace(method, stringWidthBody());
                patched++;
            } else if ("getCharWidth".equals(method.name) && "(C)I".equals(method.desc)) {
                replace(method, charWidthBody());
                patched++;
            }
        }

        if (patched != 3) {
            System.err.println("[MBO ASM] Incomplete Thaumcraft font patch: " + patched + "/3 methods");
            return bytes;
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        System.out.println("[MBO ASM] Routed Thaumonomicon font through Minecraft.fontRenderer");
        return writer.toByteArray();
    }

    private static void replace(MethodNode method, InsnList body) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.localVariables = null;
        method.instructions.add(body);
    }

    private static InsnList drawBody() {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new VarInsnNode(ILOAD, 2));
        code.add(new VarInsnNode(ILOAD, 3));
        code.add(new VarInsnNode(ILOAD, 4));
        code.add(new VarInsnNode(ILOAD, 5));
        code.add(new MethodInsnNode(INVOKESTATIC, HOOK, "drawString", "(Ljava/lang/String;IIIZ)I", false));
        code.add(new InsnNode(IRETURN));
        return code;
    }

    private static InsnList stringWidthBody() {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new MethodInsnNode(INVOKESTATIC, HOOK, "getStringWidth", "(Ljava/lang/String;)I", false));
        code.add(new InsnNode(IRETURN));
        return code;
    }

    private static InsnList charWidthBody() {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(ILOAD, 1));
        code.add(new MethodInsnNode(INVOKESTATIC, HOOK, "getCharWidth", "(C)I", false));
        code.add(new InsnNode(IRETURN));
        return code;
    }
}
