package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class BlockButtonTransformer implements IClassTransformer, Opcodes {
    private static final String TARGET = "net.minecraft.block.BlockButton";
    private static final String HOOKS = "ru/givler/mbo/core/BlockButtonHooks";

    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (!TARGET.equals(transformedName)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        boolean changed = false;
        for (MethodNode method : node.methods) {
            if (matches(method, "(Lnet/minecraft/world/World;IIII)Z",
                    "canPlaceBlockOnSide", "func_149707_d")) {
                injectIntOverride(method, "canPlaceOnSide", "(Lnet/minecraft/world/World;IIII)I", IRETURN);
                changed = true;
            } else if (matches(method, "(Lnet/minecraft/world/World;III)Z",
                    "canPlaceBlockAt", "func_149742_c")) {
                injectIntOverride(method, "canPlaceAt", "(Lnet/minecraft/world/World;III)I", IRETURN);
                changed = true;
            } else if (matches(method, "(Lnet/minecraft/world/World;IIIIFFFI)I",
                    "onBlockPlaced", "func_149660_a")) {
                injectPlacedOverride(method);
                changed = true;
            } else if (matches(method, "(I)V", "func_150043_b")) {
                injectBoundsOverride(method);
                changed = true;
            } else if (matches(method, "(Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;)V",
                    "onNeighborBlockChange", "func_149695_a")) {
                injectCancelBoolean(method, "handleNeighbor",
                        "(Lnet/minecraft/block/BlockButton;Lnet/minecraft/world/World;III)Z");
                changed = true;
            } else if (matches(method, "(Lnet/minecraft/world/World;IIII)V", "func_150042_a")) {
                injectNotifyOverride(method);
                changed = true;
            } else if (matches(method, "(Lnet/minecraft/world/IBlockAccess;IIII)I",
                    "isProvidingStrongPower", "func_149748_c")) {
                injectStrongPower(method);
                changed = true;
            }
        }
        if (!changed) {
            System.err.println("[MBO ASM] BlockButton methods were not found");
            return bytes;
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        System.out.println("[MBO ASM] Patched BlockButton floor/ceiling placement");
        return writer.toByteArray();
    }

    private static boolean matches(MethodNode method, String desc, String... names) {
        if (!desc.equals(method.desc)) return false;
        for (String name : names) if (name.equals(method.name)) return true;
        return false;
    }

    private static void injectIntOverride(MethodNode method, String hook, String hookDesc, int returnOpcode) {
        Type[] args = Type.getArgumentTypes(method.desc);
        InsnList code = new InsnList();
        int local = method.maxLocals++;
        int slot = 1;
        for (Type arg : args) {
            code.add(new VarInsnNode(arg.getOpcode(ILOAD), slot));
            slot += arg.getSize();
        }
        code.add(new MethodInsnNode(INVOKESTATIC, HOOKS, hook, hookDesc, false));
        code.add(new VarInsnNode(ISTORE, local));
        LabelNode vanilla = new LabelNode();
        code.add(new VarInsnNode(ILOAD, local));
        code.add(new InsnNode(ICONST_M1));
        code.add(new JumpInsnNode(IF_ICMPEQ, vanilla));
        code.add(new VarInsnNode(ILOAD, local));
        code.add(new InsnNode(returnOpcode));
        code.add(vanilla);
        method.instructions.insert(code);
    }

    private static void injectPlacedOverride(MethodNode method) {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new VarInsnNode(ILOAD, 2));
        code.add(new VarInsnNode(ILOAD, 3));
        code.add(new VarInsnNode(ILOAD, 4));
        code.add(new VarInsnNode(ILOAD, 5));
        code.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "placedMeta",
                "(Lnet/minecraft/world/World;IIII)I", false));
        int local = method.maxLocals++;
        code.add(new VarInsnNode(ISTORE, local));
        LabelNode vanilla = new LabelNode();
        code.add(new VarInsnNode(ILOAD, local));
        code.add(new InsnNode(ICONST_M1));
        code.add(new JumpInsnNode(IF_ICMPEQ, vanilla));
        code.add(new VarInsnNode(ILOAD, local));
        code.add(new InsnNode(IRETURN));
        code.add(vanilla);
        method.instructions.insert(code);
    }

    private static void injectBoundsOverride(MethodNode method) {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(ALOAD, 0));
        code.add(new VarInsnNode(ILOAD, 1));
        code.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "setBounds",
                "(Lnet/minecraft/block/BlockButton;I)Z", false));
        LabelNode vanilla = new LabelNode();
        code.add(new JumpInsnNode(IFEQ, vanilla));
        code.add(new InsnNode(RETURN));
        code.add(vanilla);
        method.instructions.insert(code);
    }

    private static void injectCancelBoolean(MethodNode method, String hook, String desc) {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(ALOAD, 0));
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new VarInsnNode(ILOAD, 2));
        code.add(new VarInsnNode(ILOAD, 3));
        code.add(new VarInsnNode(ILOAD, 4));
        code.add(new MethodInsnNode(INVOKESTATIC, HOOKS, hook, desc, false));
        LabelNode vanilla = new LabelNode();
        code.add(new JumpInsnNode(IFEQ, vanilla));
        code.add(new InsnNode(RETURN));
        code.add(vanilla);
        method.instructions.insert(code);
    }

    private static void injectNotifyOverride(MethodNode method) {
        injectCancelBoolean(method, "notifySupport",
                "(Lnet/minecraft/block/BlockButton;Lnet/minecraft/world/World;III)Z");
    }

    private static void injectStrongPower(MethodNode method) {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(ALOAD, 0));
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new VarInsnNode(ILOAD, 2));
        code.add(new VarInsnNode(ILOAD, 3));
        code.add(new VarInsnNode(ILOAD, 4));
        code.add(new VarInsnNode(ILOAD, 5));
        code.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "strongPower",
                "(Lnet/minecraft/block/BlockButton;Lnet/minecraft/world/IBlockAccess;IIII)I", false));
        int local = method.maxLocals++;
        code.add(new VarInsnNode(ISTORE, local));
        LabelNode vanilla = new LabelNode();
        code.add(new VarInsnNode(ILOAD, local));
        code.add(new InsnNode(ICONST_M1));
        code.add(new JumpInsnNode(IF_ICMPEQ, vanilla));
        code.add(new VarInsnNode(ILOAD, local));
        code.add(new InsnNode(IRETURN));
        code.add(vanilla);
        method.instructions.insert(code);
    }
}
