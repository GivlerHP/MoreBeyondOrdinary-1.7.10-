package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class WaterloggingFarmlandTransformer implements IClassTransformer, Opcodes {
  private static final String TARGET = "net.minecraft.block.BlockFarmland";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (!TARGET.equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    for (MethodNode method : node.methods) {
      String mappedName =
          FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      String mappedDescription = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
      if ("(Lnet/minecraft/world/World;III)Z".equals(mappedDescription)
          && ("func_149821_m".equals(method.name) || "func_149821_m".equals(mappedName))) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.instructions.add(new VarInsnNode(ALOAD, 1));
        method.instructions.add(new VarInsnNode(ILOAD, 2));
        method.instructions.add(new VarInsnNode(ILOAD, 3));
        method.instructions.add(new VarInsnNode(ILOAD, 4));
        method.instructions.add(
            new MethodInsnNode(
                INVOKESTATIC,
                "ru/givler/mbo/core/WaterloggingFarmlandHooks",
                "hasNearbyWater",
                "(Lnet/minecraft/world/World;III)Z",
                false));
        method.instructions.add(new InsnNode(IRETURN));
        ClassWriter writer = SafeClassWriter.create();
        node.accept(writer);
        System.out.println("[MBO ASM] Patched farmland waterlogged hydration");
        return writer.toByteArray();
      }
    }
    System.err.println("[MBO ASM] BlockFarmland water search was not found");
    return bytes;
  }
}
