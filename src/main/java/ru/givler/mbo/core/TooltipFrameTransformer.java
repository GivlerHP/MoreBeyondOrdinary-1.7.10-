package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Installs the ornamental frame in every GUI using GuiScreen.drawHoveringText. */
public final class TooltipFrameTransformer implements IClassTransformer, Opcodes {
    private static final String TARGET = "net.minecraft.client.gui.GuiScreen";
    private static final String HOOK = "ru/givler/mbo/core/TooltipFrameHooks";
    private static final String DESC =
            "(Ljava/util/List;IILnet/minecraft/client/gui/FontRenderer;)V";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !TARGET.equals(transformedName)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        boolean patched = false;
        for (MethodNode method : node.methods) {
            String mappedDesc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
            if (!DESC.equals(mappedDesc)) continue;
            method.instructions.clear();
            method.tryCatchBlocks.clear();
            method.localVariables = null;
            method.instructions.add(new VarInsnNode(ALOAD, 0));
            method.instructions.add(new VarInsnNode(ALOAD, 1));
            method.instructions.add(new VarInsnNode(ILOAD, 2));
            method.instructions.add(new VarInsnNode(ILOAD, 3));
            method.instructions.add(new VarInsnNode(ALOAD, 4));
            method.instructions.add(new MethodInsnNode(INVOKESTATIC, HOOK, "drawTooltip",
                    "(Lnet/minecraft/client/gui/GuiScreen;Ljava/util/List;IILnet/minecraft/client/gui/FontRenderer;)V",
                    false));
            method.instructions.add(new InsnNode(RETURN));
            patched = true;
            break;
        }
        if (!patched) {
            System.err.println("[MBO ASM] GuiScreen tooltip method was not found");
            return bytes;
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
            @Override protected String getCommonSuperClass(String a, String b) { return "java/lang/Object"; }
        };
        node.accept(writer);
        System.out.println("[MBO ASM] Installed tiled ornamental tooltip frame");
        return writer.toByteArray();
    }
}
