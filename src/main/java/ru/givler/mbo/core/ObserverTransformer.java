package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Reports block and metadata changes to observers without loading surrounding chunks. */
public final class ObserverTransformer implements IClassTransformer, Opcodes {
    private static final String HOOK = "ru/givler/mbo/core/ObserverHooks";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !"net.minecraft.world.World".equals(transformedName)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        boolean blockPatched = false, metadataPatched = false;
        for (MethodNode method : node.methods) {
            String mappedName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
            String mappedDesc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
            if ("(IIILnet/minecraft/world/chunk/Chunk;Lnet/minecraft/block/Block;Lnet/minecraft/block/Block;I)V".equals(mappedDesc)
                    && ("markAndNotifyBlock".equals(method.name) || "markAndNotifyBlock".equals(mappedName))) {
                for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                     instruction = instruction.getNext()) {
                    if (instruction.getOpcode() == RETURN)
                        method.instructions.insertBefore(instruction, coordinatesHook());
                }
                blockPatched = true;
            } else if ("(IIIII)Z".equals(mappedDesc)
                    && ("setBlockMetadataWithNotify".equals(method.name)
                    || "func_72921_c".equals(method.name)
                    || "setBlockMetadataWithNotify".equals(mappedName))) {
                for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                     instruction = instruction.getNext()) {
                    if (instruction.getOpcode() != IRETURN) continue;
                    InsnList hook = new InsnList();
                    hook.add(new InsnNode(DUP));
                    hook.add(new VarInsnNode(ALOAD, 0));
                    hook.add(new VarInsnNode(ILOAD, 1));
                    hook.add(new VarInsnNode(ILOAD, 2));
                    hook.add(new VarInsnNode(ILOAD, 3));
                    hook.add(new MethodInsnNode(INVOKESTATIC, HOOK, "onMetadataChanged",
                            "(ZLnet/minecraft/world/World;III)V", false));
                    method.instructions.insertBefore(instruction, hook);
                }
                metadataPatched = true;
            }
        }
        if (!blockPatched || !metadataPatched) {
            System.err.println("[MBO ASM] Observer world patch incomplete: block=" + blockPatched
                    + ", metadata=" + metadataPatched);
            return bytes;
        }
        ClassWriter writer = SafeClassWriter.create();
        node.accept(writer);
        return writer.toByteArray();
    }

    private static InsnList coordinatesHook() {
        InsnList hook = new InsnList();
        hook.add(new VarInsnNode(ALOAD, 0));
        hook.add(new VarInsnNode(ILOAD, 1));
        hook.add(new VarInsnNode(ILOAD, 2));
        hook.add(new VarInsnNode(ILOAD, 3));
        hook.add(new MethodInsnNode(INVOKESTATIC, HOOK, "onBlockChanged",
                "(Lnet/minecraft/world/World;III)V", false));
        return hook;
    }
}
