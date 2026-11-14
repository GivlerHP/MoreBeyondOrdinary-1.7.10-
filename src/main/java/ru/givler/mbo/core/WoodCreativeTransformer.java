package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Orders optional wood additions after the owning tab has populated its list. */
public final class WoodCreativeTransformer implements IClassTransformer, Opcodes {
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !("net.minecraft.creativetab.CreativeTabs".equals(transformedName)
                || "minefantasy.mf2.item.list.CreativeTabMF".equals(transformedName)
                || "thaumcraft.common.lib.CreativeTabThaumcraft".equals(transformedName)
                || "biomesoplenty.common.helpers.CreativeTabsBOP".equals(transformedName))) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        boolean changed = false;
        for (MethodNode method : node.methods) {
            if (!(method.name.equals("displayAllReleventItems") || method.name.equals("func_78018_a"))
                    || !method.desc.equals("(Ljava/util/List;)V")) continue;
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn.getOpcode() != RETURN) continue;
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(ALOAD, 0)); hook.add(new VarInsnNode(ALOAD, 1));
                hook.add(new MethodInsnNode(INVOKESTATIC,
                        "ru/givler/mbo/integration/wood/CreativeWoodOrder", "arrange",
                        "(Lnet/minecraft/creativetab/CreativeTabs;Ljava/util/List;)V", false));
                method.instructions.insertBefore(insn, hook);
                changed = true;
            }
        }
        if (!changed) return bytes;
        ClassWriter writer = SafeClassWriter.create(); node.accept(writer);
        return writer.toByteArray();
    }
}
