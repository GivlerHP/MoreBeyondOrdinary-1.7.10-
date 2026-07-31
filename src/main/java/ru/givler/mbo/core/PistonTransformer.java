package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class PistonTransformer implements IClassTransformer, Opcodes {
    private static final String TARGET = "net.minecraft.block.BlockPistonBase";

    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (!TARGET.equals(transformedName)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        boolean changed = false;
        for (MethodNode method : node.methods) {
            String mappedName = FMLDeobfuscatingRemapper.INSTANCE
                    .mapMethodName(node.name, method.name, method.desc);
            String mappedDesc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
            if (("(Lnet/minecraft/world/World;IIIII)Z".equals(method.desc)
                    || "(Lnet/minecraft/world/World;IIIII)Z".equals(mappedDesc))
                    && ("onBlockEventReceived".equals(method.name)
                    || "func_149696_a".equals(method.name)
                    || "onBlockEventReceived".equals(mappedName)
                    || "func_149696_a".equals(mappedName))) {
                inject(method);
                changed = true;
            }
        }
        if (!changed) {
            System.err.println("[MBO ASM] BlockPistonBase event method was not found");
            return bytes;
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        System.out.println("[MBO ASM] Patched BlockPistonBase slime behavior");
        return writer.toByteArray();
    }

    private static void inject(MethodNode method) {
        int result = method.maxLocals++;
        InsnList code = new InsnList();
        code.add(new VarInsnNode(ALOAD, 0));
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new VarInsnNode(ILOAD, 2));
        code.add(new VarInsnNode(ILOAD, 3));
        code.add(new VarInsnNode(ILOAD, 4));
        code.add(new VarInsnNode(ILOAD, 5));
        code.add(new VarInsnNode(ILOAD, 6));
        code.add(new MethodInsnNode(INVOKESTATIC, "ru/givler/mbo/core/PistonHooks",
                "handleEvent", "(Lnet/minecraft/block/BlockPistonBase;Lnet/minecraft/world/World;IIIII)I", false));
        code.add(new VarInsnNode(ISTORE, result));
        LabelNode vanilla = new LabelNode();
        code.add(new VarInsnNode(ILOAD, result));
        code.add(new InsnNode(ICONST_M1));
        code.add(new org.objectweb.asm.tree.JumpInsnNode(IF_ICMPEQ, vanilla));
        code.add(new VarInsnNode(ILOAD, result));
        code.add(new InsnNode(IRETURN));
        code.add(vanilla);
        method.instructions.insert(code);
    }
}
