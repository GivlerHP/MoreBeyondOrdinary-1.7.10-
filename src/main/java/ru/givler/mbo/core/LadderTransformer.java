package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class LadderTransformer implements IClassTransformer, Opcodes {
    private static final String TARGET = "net.minecraft.block.BlockLadder";
    private static final String HOOK = "ru/givler/mbo/core/LadderHooks";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !TARGET.equals(transformedName)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        for (MethodNode method : node.methods) {
            FMLDeobfuscatingRemapper remapper = FMLDeobfuscatingRemapper.INSTANCE;
            String desc = remapper.mapMethodDesc(method.desc);
            // These descriptors are unique among methods declared by BlockLadder.
            // Matching by descriptor also works when this transformer runs before
            // Forge has made the obfuscated -> SRG method-name mappings available.
            if (desc.equals("()I")) {
                method.instructions.clear();
                method.tryCatchBlocks.clear();
                method.instructions.add(new MethodInsnNode(INVOKESTATIC, HOOK, "getRenderType", "()I", false));
                method.instructions.add(new InsnNode(IRETURN));
            } else if (desc.equals("(Lnet/minecraft/world/World;III)Z")) {
                InsnList code = loadWorldXYZ();
                code.add(new MethodInsnNode(INVOKESTATIC, HOOK, "canPlaceOnSpecial",
                        "(Lnet/minecraft/world/World;III)Z", false));
                LabelNode vanilla = new LabelNode();
                code.add(new JumpInsnNode(IFEQ, vanilla));
                code.add(new InsnNode(ICONST_1));
                code.add(new InsnNode(IRETURN));
                code.add(vanilla);
                method.instructions.insert(code);
            } else if (desc.equals("(Lnet/minecraft/world/World;IIIIFFFI)I")) {
                int local = method.maxLocals++;
                InsnList code = loadWorldXYZ();
                code.add(new VarInsnNode(ILOAD, 5));
                code.add(new MethodInsnNode(INVOKESTATIC, HOOK, "specialPlacementMetadata",
                        "(Lnet/minecraft/world/World;IIII)I", false));
                code.add(new VarInsnNode(ISTORE, local));
                code.add(new VarInsnNode(ILOAD, local));
                LabelNode vanilla = new LabelNode();
                code.add(new JumpInsnNode(IFLT, vanilla));
                code.add(new VarInsnNode(ILOAD, local));
                code.add(new InsnNode(IRETURN));
                code.add(vanilla);
                method.instructions.insert(code);
            } else if (desc.equals("(Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;)V")) {
                InsnList code = loadWorldXYZ();
                code.add(new MethodInsnNode(INVOKESTATIC, HOOK, "hasSpecialSupport",
                        "(Lnet/minecraft/world/World;III)Z", false));
                LabelNode vanilla = new LabelNode();
                code.add(new JumpInsnNode(IFEQ, vanilla));
                code.add(new InsnNode(RETURN));
                code.add(vanilla);
                method.instructions.insert(code);
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
            @Override protected String getCommonSuperClass(String type1, String type2) {
                return "java/lang/Object";
            }
        };
        node.accept(writer);
        return writer.toByteArray();
    }

    private static InsnList loadWorldXYZ() {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new VarInsnNode(ILOAD, 2));
        code.add(new VarInsnNode(ILOAD, 3));
        code.add(new VarInsnNode(ILOAD, 4));
        return code;
    }

}
