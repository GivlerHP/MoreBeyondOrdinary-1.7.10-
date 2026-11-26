package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class DollPoseTransformer implements IClassTransformer, Opcodes {
    @Override public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !"net.minecraft.client.model.ModelBiped".equals(transformedName)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        for (MethodNode method : node.methods) {
            String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
            if (!"setRotationAngles".equals(method.name) && !"func_78087_a".equals(mapped)) continue;
            if (method.desc.charAt(1) != 'F') continue;
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn.getOpcode() != RETURN) continue;
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(ALOAD, 0));
                hook.add(new VarInsnNode(ALOAD, 7));
                hook.add(new MethodInsnNode(INVOKESTATIC, "ru/givler/mbo/client/render/DollPlayer", "applyPose",
                    "(Ljava/lang/Object;Ljava/lang/Object;)V", false));
                method.instructions.insertBefore(insn, hook);
            }
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            return writer.toByteArray();
        }
        return bytes;
    }
}
