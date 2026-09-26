package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Keeps effects without entity particles out of vanilla's combined particle color. */
public final class PotionParticleTransformer implements IClassTransformer, Opcodes {
    private static final String ENTITY = "net.minecraft.entity.EntityLivingBase";
    private static final String HELPER = "net/minecraft/potion/PotionHelper";
    private static final String WORLD = "net/minecraft/world/World";
    private static final String POLICY = "ru/givler/mbo/potion/PotionParticlePolicy";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !ENTITY.equals(transformedName)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        int colorPatches = 0;
        int spawnPatches = 0;
        for (MethodNode method : node.methods) {
            boolean potionUpdate = false;
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null;
                    insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) insn;
                if ((HELPER.equals(call.owner)
                        || HELPER.equals(FMLDeobfuscatingRemapper.INSTANCE.map(call.owner)))
                        && "(Ljava/util/Collection;)I".equals(call.desc)) {
                    potionUpdate = true;
                    break;
                }
            }
            if (!potionUpdate) continue;
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null;
                    insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) insn;
                boolean helper = HELPER.equals(call.owner)
                        || HELPER.equals(FMLDeobfuscatingRemapper.INSTANCE.map(call.owner));
                boolean world = WORLD.equals(call.owner)
                        || WORLD.equals(FMLDeobfuscatingRemapper.INSTANCE.map(call.owner));
                if (helper && "(Ljava/util/Collection;)I".equals(call.desc)) {
                    call.owner = POLICY;
                    call.name = "color";
                    colorPatches++;
                } else if (helper && "(Ljava/util/Collection;)Z".equals(call.desc)) {
                    call.owner = POLICY;
                    call.name = "ambient";
                    colorPatches++;
                } else if (world && "(Ljava/lang/String;DDDDDD)V".equals(call.desc)) {
                    method.instructions.insertBefore(call, new VarInsnNode(ALOAD, 0));
                    call.setOpcode(INVOKESTATIC);
                    call.owner = POLICY;
                    call.name = "spawnParticle";
                    call.desc = "(Lnet/minecraft/world/World;Ljava/lang/String;DDDDDD"
                            + "Lnet/minecraft/entity/EntityLivingBase;)V";
                    spawnPatches++;
                }
            }
        }
        if (colorPatches != 2 || spawnPatches != 1) {
            System.err.println("[MBO ASM] Potion particle hooks not found: "
                    + colorPatches + " color, " + spawnPatches + " spawn");
            return bytes;
        }
        ClassWriter writer = SafeClassWriter.create();
        node.accept(writer);
        return writer.toByteArray();
    }
}
