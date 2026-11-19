package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Makes Forge's armor passes use the selected cosmetic model and texture. */
public final class ArmorSkinTransformer implements IClassTransformer, Opcodes {
    private static final String HOOK = "ru/givler/mbo/client/render/weaponskin/ArmorSkinRenderer";

    @Override public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !"net.minecraftforge.client.ForgeHooksClient".equals(transformedName)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        boolean changed = false;
        for (MethodNode method : node.methods) {
            boolean model = method.name.equals("getArmorModel") && Type.getArgumentTypes(method.desc).length == 4;
            boolean texture = method.name.equals("getArmorTexture") && Type.getArgumentTypes(method.desc).length == 5;
            if (!model && !texture) continue;
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(ALOAD, 1));
            if (model) hook.add(new VarInsnNode(ALOAD, 3));
            hook.add(new MethodInsnNode(INVOKESTATIC, HOOK, model ? "model" : "texture",
                    model ? "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
                          : "(Ljava/lang/Object;)Ljava/lang/Object;", false));
            LabelNode fallback = new LabelNode();
            hook.add(new InsnNode(DUP));
            hook.add(new JumpInsnNode(IFNULL, fallback));
            hook.add(new TypeInsnNode(CHECKCAST, Type.getReturnType(method.desc).getInternalName()));
            hook.add(new InsnNode(ARETURN));
            hook.add(fallback);
            hook.add(new FrameNode(F_SAME1, 0, null, 1, new Object[]{"java/lang/Object"}));
            hook.add(new InsnNode(POP));
            method.instructions.insert(hook);
            changed = true;
        }
        if (!changed) return bytes;
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
