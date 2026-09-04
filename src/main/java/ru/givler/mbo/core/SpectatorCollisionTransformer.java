package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Makes spectator players absent from entity collision and projectile ray tracing. */
public final class SpectatorCollisionTransformer implements IClassTransformer, Opcodes {
    private static final String TARGET = "net.minecraft.entity.EntityLivingBase";
    private static final String HOOKS = "ru/givler/mbo/core/SpectatorCollisionHooks";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (!TARGET.equals(transformedName)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        int patched = 0;
        for (MethodNode method : node.methods) {
            if (!"()Z".equals(method.desc)) continue;
            String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
            if (!isTarget(method.name) && !isTarget(mapped)) continue;
            for (AbstractInsnNode instruction = method.instructions.getFirst();
                 instruction != null; instruction = instruction.getNext()) {
                if (instruction.getOpcode() != IRETURN) continue;
                InsnList code = new InsnList();
                // The vanilla boolean is already on the stack. Filtering each return avoids
                // adding control-flow edges and therefore avoids early frame computation.
                code.add(new VarInsnNode(ALOAD, 0));
                code.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "filterCollision",
                        "(ZLnet/minecraft/entity/EntityLivingBase;)Z", false));
                method.instructions.insertBefore(instruction, code);
            }
            patched++;
        }
        if (patched != 2) {
            System.err.println("[MBO ASM] Expected 2 EntityLivingBase collision methods, patched " + patched);
        }
        if (patched == 0) return bytes;
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static boolean isTarget(String name) {
        return "canBeCollidedWith".equals(name) || "func_70067_L".equals(name)
                || "canBePushed".equals(name) || "func_70104_M".equals(name);
    }
}
