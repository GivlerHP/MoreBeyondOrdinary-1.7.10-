package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class LootingPotionTransformer implements IClassTransformer, Opcodes {
    private static final String TARGET = "net.minecraft.enchantment.EnchantmentHelper";
    private static final String DESC = "(Lnet/minecraft/entity/EntityLivingBase;)I";
    private static final String HOOKS = "ru/givler/mbo/core/LootingPotionHooks";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (!TARGET.equals(transformedName)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        boolean changed = false;
        for (MethodNode method : node.methods) {
            if (!matches(node.name, method)) continue;
            for (AbstractInsnNode instruction = method.instructions.getFirst();
                 instruction != null; instruction = instruction.getNext()) {
                if (instruction.getOpcode() != IRETURN) continue;
                InsnList code = new InsnList();
                code.add(new VarInsnNode(ALOAD, 0));
                code.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "addPotionLevel",
                        "(ILnet/minecraft/entity/EntityLivingBase;)I", false));
                method.instructions.insertBefore(instruction, code);
                changed = true;
            }
            break;
        }
        if (!changed) {
            System.err.println("[MBO ASM] EnchantmentHelper.getLootingModifier was not found");
            return bytes;
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        System.out.println("[MBO ASM] Patched potion looting modifier");
        return writer.toByteArray();
    }

    private static boolean matches(String owner, MethodNode method) {
        FMLDeobfuscatingRemapper remapper = FMLDeobfuscatingRemapper.INSTANCE;
        String mappedDesc = remapper.mapMethodDesc(method.desc);
        if (!DESC.equals(method.desc) && !DESC.equals(mappedDesc)) return false;
        String mappedName = remapper.mapMethodName(owner, method.name, method.desc);
        return "getLootingModifier".equals(method.name) || "func_77519_f".equals(method.name)
                || "getLootingModifier".equals(mappedName) || "func_77519_f".equals(mappedName);
    }
}
