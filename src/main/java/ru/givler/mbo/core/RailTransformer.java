package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class RailTransformer implements IClassTransformer, Opcodes {
    private static final String TARGET = "net.minecraft.block.BlockRailBase";
    private static final String HOOK = "ru/givler/mbo/core/RailHooks";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !TARGET.equals(transformedName)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        for (MethodNode method : node.methods) {
            String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
            String desc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
            if ("()I".equals(desc) && ("getRenderType".equals(mapped)
                    || "func_149645_b".equals(mapped)
                    || "getRenderType".equals(method.name) || "func_149645_b".equals(method.name))) {
                method.instructions.clear();
                method.tryCatchBlocks.clear();
                method.instructions.add(new MethodInsnNode(INVOKESTATIC, HOOK, "getRenderType", "()I", false));
                method.instructions.add(new InsnNode(IRETURN));
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
            @Override protected String getCommonSuperClass(String a, String b) { return "java/lang/Object"; }
        };
        node.accept(writer);
        return writer.toByteArray();
    }
}
