package ru.givler.mbo.integration.angelica.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class AngelicaShaderCompatTransformer implements IClassTransformer, Opcodes {
  private static final String HOOK="ru/givler/mbo/integration/angelica/client/AngelicaShaderMacros";
  @Override public byte[] transform(String name,String target,byte[] bytes) {
    if(bytes==null || !"net.coderbot.iris.gl.shader.StandardMacros".equals(target))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int anchors=0;
    for(MethodNode method:node.methods)if(method.name.equals("createStandardEnvironmentDefines")
        && method.desc.equals("()Ljava/lang/Iterable;")) {
      for(AbstractInsnNode instruction:method.instructions.toArray()) {
        if(instruction instanceof MethodInsnNode && ((MethodInsnNode)instruction).owner.equals(HOOK))return bytes;
        if(instruction.getOpcode()==ARETURN) {
          method.instructions.insertBefore(instruction,new MethodInsnNode(INVOKESTATIC,HOOK,
              "withConnectedTextures","(Ljava/lang/Iterable;)Ljava/lang/Iterable;",false));anchors++;
        }
      }
    }
    if(anchors!=1)throw new IllegalStateException("Angelica shader macro anchors: "+anchors);
    ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);
    System.out.println("[MBO] Shader CTM capability macro installed");return out.toByteArray();
  }
}
