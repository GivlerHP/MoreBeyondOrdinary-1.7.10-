package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Directory iterators need a stable name while CraftTweaker sorts them before next(). */
public final class MineTweakerScriptTransformer implements IClassTransformer, Opcodes {
    @Override public byte[] transform(String name, String target, byte[] bytes) {
        if (bytes == null || !"minetweaker.runtime.providers.ScriptIteratorDirectory".equals(target)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        for (MethodNode method : node.methods) {
            if (!method.name.equals("getName") || !method.desc.equals("()Ljava/lang/String;")) continue;
            for (AbstractInsnNode insn : method.instructions.toArray())
                if (insn instanceof MethodInsnNode && ((MethodInsnNode) insn).name.equals("getGroupName")) return bytes;
            LabelNode selected = new LabelNode();
            InsnList code = new InsnList();
            code.add(new VarInsnNode(ALOAD, 0));
            code.add(new FieldInsnNode(GETFIELD, node.name, "current", "Ljava/io/File;"));
            code.add(new JumpInsnNode(IFNONNULL, selected));
            code.add(new VarInsnNode(ALOAD, 0));
            code.add(new MethodInsnNode(INVOKEVIRTUAL, node.name, "getGroupName", "()Ljava/lang/String;", false));
            code.add(new InsnNode(ARETURN));
            code.add(selected);
            code.add(new FrameNode(F_SAME, 0, null, 0, null));
            method.instructions.insert(code);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            return writer.toByteArray();
        }
        throw new IllegalStateException("CraftTweaker directory iterator getName anchor missing");
    }
}
