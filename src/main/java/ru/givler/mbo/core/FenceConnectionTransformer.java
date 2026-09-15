package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class FenceConnectionTransformer implements IClassTransformer, Opcodes {
    private static final String FENCE = "net.minecraft.block.BlockFence";
    private static final String WALL = "net.minecraft.block.BlockWall";
    private static final String RENDER_BLOCKS = "net.minecraft.client.renderer.RenderBlocks";
    private static final String HOOKS = "ru/givler/mbo/core/FenceConnectionHooks";
    private static final String DESC = "(Lnet/minecraft/world/IBlockAccess;III)Z";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        boolean renderBlocks = RENDER_BLOCKS.equals(transformedName);
        String mcpName;
        String srgName;
        if (FENCE.equals(transformedName)) {
            mcpName = "canConnectFenceTo";
            srgName = "func_149826_e";
        } else if (WALL.equals(transformedName)) {
            mcpName = "canConnectWallTo";
            srgName = "func_150091_e";
        } else if (!renderBlocks) {
            return bytes;
        } else {
            mcpName = null;
            srgName = null;
        }

        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        boolean changed = injectOrigins(node, transformedName);
        if (renderBlocks) {
            if (!changed) System.err.println("[MBO ASM] Fence render origins not found in " + transformedName);
            else System.out.println("[MBO ASM] Patched fence render origins in " + transformedName);
            ClassWriter writer = SafeClassWriter.create();
            node.accept(writer);
            return writer.toByteArray();
        }
        for (MethodNode method : node.methods) {
            if (!matches(node.name, method, mcpName, srgName)) continue;
            InsnList code = new InsnList();
            code.add(new VarInsnNode(ALOAD, 0));
            code.add(new VarInsnNode(ALOAD, 1));
            code.add(new VarInsnNode(ILOAD, 2));
            code.add(new VarInsnNode(ILOAD, 3));
            code.add(new VarInsnNode(ILOAD, 4));
            code.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "allowsConnection",
                    "(Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockAccess;III)Z", false));
            LabelNode allowed = new LabelNode();
            code.add(new JumpInsnNode(IFNE, allowed));
            code.add(new InsnNode(ICONST_0));
            code.add(new InsnNode(IRETURN));
            code.add(allowed);
            code.add(new VarInsnNode(ALOAD, 1));
            code.add(new VarInsnNode(ILOAD, 2));
            code.add(new VarInsnNode(ILOAD, 3));
            code.add(new VarInsnNode(ILOAD, 4));
            code.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "isCompatibleNeighbor", DESC, false));
            LabelNode vanilla = new LabelNode();
            code.add(new JumpInsnNode(IFEQ, vanilla));
            code.add(new InsnNode(ICONST_1));
            code.add(new InsnNode(IRETURN));
            code.add(vanilla);
            method.instructions.insert(code);
            changed = true;
            break;
        }
        if (!changed) {
            System.err.println("[MBO ASM] Connection method not found in " + transformedName);
            return bytes;
        }
        ClassWriter writer = SafeClassWriter.create();
        node.accept(writer);
        System.out.println("[MBO ASM] Patched connections in " + transformedName);
        return writer.toByteArray();
    }

    private static boolean injectOrigins(ClassNode node, String transformedName) {
        boolean changed = false;
        for (MethodNode method : node.methods) {
            String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
            boolean target;
            if (RENDER_BLOCKS.equals(transformedName)) {
                target = "renderBlockFence".equals(method.name) || "renderBlockWall".equals(method.name)
                        || "renderBlockFence".equals(mapped) || "renderBlockWall".equals(mapped);
            } else {
                target = "setBlockBoundsBasedOnState".equals(method.name)
                        || "addCollisionBoxesToList".equals(method.name)
                        || "getCollisionBoundingBoxFromPool".equals(method.name)
                        || "setBlockBoundsBasedOnState".equals(mapped)
                        || "addCollisionBoxesToList".equals(mapped)
                        || "getCollisionBoundingBoxFromPool".equals(mapped);
            }
            if (!target) continue;
            InsnList origin = new InsnList();
            int firstCoordinate = RENDER_BLOCKS.equals(transformedName) ? 2 : 2;
            origin.add(new VarInsnNode(ILOAD, firstCoordinate));
            origin.add(new VarInsnNode(ILOAD, firstCoordinate + 1));
            origin.add(new VarInsnNode(ILOAD, firstCoordinate + 2));
            origin.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "setOrigin", "(III)V", false));
            method.instructions.insert(origin);
            changed = true;
        }
        return changed;
    }

    private static boolean matches(String owner, MethodNode method, String mcpName, String srgName) {
        FMLDeobfuscatingRemapper remapper = FMLDeobfuscatingRemapper.INSTANCE;
        String mappedDesc = remapper.mapMethodDesc(method.desc);
        if (!DESC.equals(method.desc) && !DESC.equals(mappedDesc)) return false;
        String mappedName = remapper.mapMethodName(owner, method.name, method.desc);
        return mcpName.equals(method.name) || srgName.equals(method.name)
                || mcpName.equals(mappedName) || srgName.equals(mappedName);
    }
}
