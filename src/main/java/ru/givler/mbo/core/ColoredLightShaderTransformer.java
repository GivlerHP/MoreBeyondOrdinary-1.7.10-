package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** E7 bridge only uploads data to shader programs that explicitly declare the MBO interface. */
public final class ColoredLightShaderTransformer implements IClassTransformer, Opcodes {
  @Override public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null || !"shadersmod.client.Shaders".equals(transformedName)) return bytes;
    ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0);
    for (MethodNode method : node.methods) {
      if (method.name.equals("uninit") && method.desc.equals("()V")) {
        method.instructions.insert(new MethodInsnNode(INVOKESTATIC, "ru/givler/mbo/client/lighting/ColoredLightShaderBridge", "resetPrograms", "()V", false));
      }
      if (!method.name.equals("useProgram") || !method.desc.equals("(I)V")) continue;
      for (AbstractInsnNode instruction : method.instructions.toArray()) {
        if (instruction instanceof MethodInsnNode && ((MethodInsnNode) instruction).owner.equals("ru/givler/mbo/client/lighting/ColoredLightShaderBridge")) return bytes;
        if (instruction.getOpcode() != RETURN) continue;
        InsnList code = new InsnList();
        for (String field : new String[]{"cameraPositionX", "cameraPositionY", "cameraPositionZ"}) code.add(new FieldInsnNode(GETSTATIC, node.name, field, "D"));
        code.add(new MethodInsnNode(INVOKESTATIC, "ru/givler/mbo/client/lighting/ColoredLightShaderBridge", "useProgram", "(DDD)V", false));
        method.instructions.insertBefore(instruction, code);
      }
    }
    ClassWriter writer = SafeClassWriter.create(); node.accept(writer); return writer.toByteArray();
  }
}
