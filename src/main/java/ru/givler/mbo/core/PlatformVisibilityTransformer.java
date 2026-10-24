package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Celeritas visibility must inspect the structure, without changing entity collisions. */
public final class PlatformVisibilityTransformer implements IClassTransformer, Opcodes {
  @Override public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null || !"com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer".equals(transformedName)) return bytes;
    ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0);
    boolean changed = false;
    FMLDeobfuscatingRemapper remapper = FMLDeobfuscatingRemapper.INSTANCE;
    for (MethodNode method : node.methods) {
      if (!method.name.equals("isEntityVisible") || !remapper.mapMethodDesc(method.desc).equals("(Lnet/minecraft/entity/Entity;)Z")) continue;
      for (AbstractInsnNode instruction : method.instructions.toArray()) {
        if (!(instruction instanceof MethodInsnNode)) continue;
        MethodInsnNode call = (MethodInsnNode) instruction;
        String mapped = remapper.mapMethodName(call.owner, call.name, call.desc);
        if (call.getOpcode() == INVOKEVIRTUAL && remapper.mapType(call.owner).equals("net/minecraft/entity/Entity")
            && (call.name.equals("getBoundingBox") || call.name.equals("func_70046_E") || mapped.equals("func_70046_E"))
            && remapper.mapMethodDesc(call.desc).equals("()Lnet/minecraft/util/AxisAlignedBB;")) {
          method.instructions.set(call, new MethodInsnNode(INVOKESTATIC,
              "ru/givler/mbo/client/render/PlatformVisibilityHooks", "renderBounds",
              "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/AxisAlignedBB;", false));
          changed = true;
        }
      }
    }
    if (!changed) return bytes;
    ClassWriter writer = new ClassWriter(0); node.accept(writer);
    return writer.toByteArray();
  }
}
