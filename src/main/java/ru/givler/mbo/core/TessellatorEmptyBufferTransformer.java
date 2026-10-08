package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Empty translucent batches still have a valid vertex state; PriorityQueue requires capacity > 0. */
public final class TessellatorEmptyBufferTransformer implements IClassTransformer, Opcodes {
  @Override public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null || !"net.minecraft.client.renderer.Tessellator".equals(transformedName)) return bytes;
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    for (MethodNode method : node.methods) {
      String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc);
      if (!(method.name.equals("getVertexState") || method.name.equals("func_147564_a")
          || mapped.equals("getVertexState") || mapped.equals("func_147564_a"))) continue;
      for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null; instruction = instruction.getNext()) {
        if (!(instruction instanceof MethodInsnNode)) continue;
        MethodInsnNode constructor = (MethodInsnNode)instruction;
        if (!constructor.owner.equals("java/util/PriorityQueue") || !constructor.name.equals("<init>")
            || !constructor.desc.equals("(ILjava/util/Comparator;)V")) continue;
        // Locate the int capacity load, before construction of the comparator and its float inputs.
        for (AbstractInsnNode before = constructor.getPrevious(); before != null; before = before.getPrevious()) {
          if (!(before instanceof FieldInsnNode)) continue;
          FieldInsnNode capacity = (FieldInsnNode)before;
          if (capacity.getOpcode() != GETFIELD || !capacity.owner.equals(node.name) || !capacity.desc.equals("I")) continue;
          AbstractInsnNode next = capacity.getNext();
          while (next != null && next.getOpcode() < 0) next = next.getNext();
          if (next != null && next.getOpcode() == ICONST_1) return bytes;
          InsnList guard = new InsnList();
          guard.add(new InsnNode(ICONST_1));
          guard.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Math", "max", "(II)I", false));
          method.instructions.insert(capacity, guard);
          ClassWriter writer = SafeClassWriter.create();
          node.accept(writer);
          System.out.println("[MBO ASM] Protected empty translucent vertex sorting");
          return writer.toByteArray();
        }
      }
    }
    throw new IllegalStateException("MBO Tessellator sorting capacity anchor missing");
  }
}
