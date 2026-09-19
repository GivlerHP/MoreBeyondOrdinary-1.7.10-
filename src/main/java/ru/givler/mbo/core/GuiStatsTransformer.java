package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Prevents the vanilla statistics screen from indexing its arrays with an invalid item ID. */
public final class GuiStatsTransformer implements IClassTransformer, Opcodes {
  private static final String TARGET =
      "net.minecraft.client.gui.achievement.GuiStats$StatsBlock";
  private static final String HOOKS = "ru/givler/mbo/core/GuiStatsHooks";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null || !TARGET.equals(transformedName)) return bytes;

    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    for (MethodNode method : node.methods) {
      if (!"<init>".equals(method.name)) continue;

      for (AbstractInsnNode instruction = method.instructions.getFirst();
          instruction != null;
          instruction = instruction.getNext()) {
        if (!(instruction instanceof FieldInsnNode) || instruction.getOpcode() != GETSTATIC) {
          continue;
        }

        FieldInsnNode field = (FieldInsnNode) instruction;
        String mappedOwner = FMLDeobfuscatingRemapper.INSTANCE.mapType(field.owner);
        String mappedName =
            FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(field.owner, field.name, field.desc);
        if ("net/minecraft/stats/StatList".equals(mappedOwner)
            && ("objectMineStats".equals(mappedName) || "field_75939_e".equals(mappedName))
            && "Ljava/util/List;".equals(field.desc)) {
          method.instructions.set(
              field,
              new MethodInsnNode(
                  INVOKESTATIC, HOOKS, "getSafeBlockStats", "()Ljava/util/List;", false));

          ClassWriter writer = SafeClassWriter.create();
          node.accept(writer);
          System.out.println("[MBO ASM] Installed invalid block-statistics filter");
          return writer.toByteArray();
        }
      }
    }

    System.err.println("[MBO ASM] GuiStats block-statistics list access was not found");
    return bytes;
  }
}
