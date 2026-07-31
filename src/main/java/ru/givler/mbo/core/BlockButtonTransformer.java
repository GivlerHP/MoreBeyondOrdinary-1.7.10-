package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
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
        boolean placedByPatched = false;
        boolean mcpMethodNames = false;
        for (MethodNode method : node.methods) {
            if (matches(node.name, method, "(Lnet/minecraft/world/World;IIII)Z",
                    "canPlaceBlockOnSide", "func_149707_d")) {
                injectIntOverride(method, "canPlaceOnSide", "(Lnet/minecraft/world/World;IIII)I", IRETURN);
                changed = true;
            } else if (matches(node.name, method, "(Lnet/minecraft/world/World;III)Z",
                    "canPlaceBlockAt", "func_149742_c")) {
                injectIntOverride(method, "canPlaceAt", "(Lnet/minecraft/world/World;III)I", IRETURN);
                changed = true;
            } else if (matches(node.name, method, "(Lnet/minecraft/world/World;IIIIFFFI)I",
                    "onBlockPlaced", "func_149660_a")) {
                mcpMethodNames = "onBlockPlaced".equals(method.name);
                injectPlacedOverride(method);
                changed = true;
            } else if (matches(node.name, method,
                    "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;)V",
                    "onBlockPlacedBy", "func_149689_a")) {
                injectPlacedBy(method);
                changed = true;
                placedByPatched = true;
            } else if (matches(node.name, method, "(I)V", "func_150043_b")) {
                injectBoundsOverride(method);
                changed = true;
            } else if (matches(node.name, method, "(Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;)V",
                    "onNeighborBlockChange", "func_149695_a")) {
                injectCancelBoolean(method, "handleNeighbor",
                        "(Lnet/minecraft/block/BlockButton;Lnet/minecraft/world/World;III)Z");
                changed = true;
            } else if (matches(node.name, method, "(Lnet/minecraft/world/World;IIII)V", "func_150042_a")) {
                injectNotifyOverride(method);
                changed = true;
            } else if (matches(node.name, method, "(Lnet/minecraft/world/IBlockAccess;IIII)I",
                    "isProvidingStrongPower", "func_149748_c")) {
                injectStrongPower(method);
                changed = true;
            }
        }
        if (!placedByPatched) {
            addPlacedByOverride(node, mcpMethodNames ? "onBlockPlacedBy" : "func_149689_a");
            changed = true;
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

    private static boolean matches(String owner, MethodNode method, String desc, String... names) {
        FMLDeobfuscatingRemapper remapper = FMLDeobfuscatingRemapper.INSTANCE;
        String mappedDesc = remapper.mapMethodDesc(method.desc);
        if (!desc.equals(method.desc) && !desc.equals(mappedDesc)) return false;
        String mappedName = remapper.mapMethodName(owner, method.name, method.desc);
        for (String name : names) {
            if (name.equals(method.name) || name.equals(mappedName)) return true;
        }
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

    private static void injectPlacedBy(MethodNode method) {
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction.getOpcode() != RETURN) continue;
            InsnList code = new InsnList();
            code.add(new VarInsnNode(ALOAD, 1));
            code.add(new VarInsnNode(ILOAD, 2));
            code.add(new VarInsnNode(ILOAD, 3));
            code.add(new VarInsnNode(ILOAD, 4));
            code.add(new VarInsnNode(ALOAD, 5));
            code.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "placedBy",
                    "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/EntityLivingBase;)V", false));
            method.instructions.insertBefore(instruction, code);
        }
    }

    private static void addPlacedByOverride(ClassNode node, String methodName) {
        String desc = "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/EntityLivingBase;"
                + "Lnet/minecraft/item/ItemStack;)V";
        MethodNode method = new MethodNode(ACC_PUBLIC, methodName, desc, null, null);
        method.instructions.add(new VarInsnNode(ALOAD, 1));
        method.instructions.add(new VarInsnNode(ILOAD, 2));
        method.instructions.add(new VarInsnNode(ILOAD, 3));
        method.instructions.add(new VarInsnNode(ILOAD, 4));
        method.instructions.add(new VarInsnNode(ALOAD, 5));
        method.instructions.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "placedBy",
                "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/EntityLivingBase;)V", false));
        method.instructions.add(new InsnNode(RETURN));
        node.methods.add(method);
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
