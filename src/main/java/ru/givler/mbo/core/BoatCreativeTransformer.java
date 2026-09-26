package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Prevents the legacy vanilla boat from contributing an item to creative tabs or search. */
public final class BoatCreativeTransformer implements IClassTransformer, Opcodes {
  private static final String TARGET = "net.minecraft.item.ItemBoat";
  private static final String DESC =
      "(Lnet/minecraft/item/Item;Lnet/minecraft/creativetab/CreativeTabs;Ljava/util/List;)V";

  @Override
  public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (!TARGET.equals(transformedName)) return bytes;

    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    addEmptyOverride(node, "getSubItems");
    addEmptyOverride(node, "func_150895_a");

    ClassWriter writer = SafeClassWriter.create();
    node.accept(writer);
    System.out.println("[MBO ASM] Hid the vanilla boat from creative tabs and search");
    return writer.toByteArray();
  }

  private static void addEmptyOverride(ClassNode node, String methodName) {
    for (MethodNode method : node.methods) {
      if (methodName.equals(method.name) && DESC.equals(method.desc)) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.instructions.add(new InsnNode(RETURN));
        return;
      }
    }

    MethodNode method = new MethodNode(ACC_PUBLIC, methodName, DESC, null, null);
    InsnList code = method.instructions;
    code.add(new InsnNode(RETURN));
    node.methods.add(method);
  }
}
