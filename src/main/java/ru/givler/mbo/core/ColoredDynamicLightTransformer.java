package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Combine OF returns and MF2's helper: its early return is inserted after MBO in some installations. */
public final class ColoredDynamicLightTransformer implements IClassTransformer, Opcodes {
  @Override public byte[] transform(String name, String transformedName, byte[] bytes) {
    boolean mf2 = "mods.battlegear2.client.utils.DynamicLightHook".equals(transformedName);
    if (bytes == null || (!mf2 && !"DynamicLights".equals(transformedName))) return bytes;
    ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0);
    int patched = 0;
    for (MethodNode method : node.methods) {
      if (!method.name.equals(mf2 ? "getExtraLightLevel" : "getLightLevel") || !FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc).equals("(Lnet/minecraft/entity/Entity;)I")) continue;
      for (AbstractInsnNode instruction : method.instructions.toArray()) {
        if (instruction instanceof MethodInsnNode && ((MethodInsnNode) instruction).owner.equals("ru/givler/mbo/client/lighting/ColoredLightAuto")) return bytes;
        if (instruction.getOpcode() != IRETURN) continue;
        InsnList list = new InsnList(); list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKESTATIC, "ru/givler/mbo/client/lighting/ColoredLightAuto", "optifineLevel", "(ILnet/minecraft/entity/Entity;)I", false));
        method.instructions.insertBefore(instruction, list); patched++;
      }
    }
    if (patched == 0) { System.err.println("[MBO ASM] OptiFine colored dynamic light hook unavailable"); return bytes; }
    ClassWriter writer = SafeClassWriter.create(); node.accept(writer); return writer.toByteArray();
  }
}
