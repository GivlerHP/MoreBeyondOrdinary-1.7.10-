package ru.givler.mbo.integration.thaumcraft.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class ThaumometerLensTransformer implements IClassTransformer, Opcodes {
  public byte[] transform(String name,String transformedName,byte[] bytes) {
    if (bytes==null || !"thaumcraft.client.renderers.item.ItemThaumometerRenderer".equals(transformedName)) return bytes;
    ClassNode node=new ClassNode(); new ClassReader(bytes).accept(node,0);
    int hooks=0;
    for(MethodNode method:node.methods) if (method.name.equals("renderItem")) {
      for(AbstractInsnNode insn:method.instructions.toArray()) if(insn instanceof MethodInsnNode
          && ((MethodInsnNode)insn).name.equals("renderAll")) {
        InsnList hook=new InsnList(); hook.add(new VarInsnNode(ALOAD,1));
        hook.add(new MethodInsnNode(INVOKESTATIC,"ru/givler/mbo/integration/thaumcraft/client/render/ThaumometerLens","capture",
            "(Lnet/minecraftforge/client/IItemRenderer$ItemRenderType;)V",false));
        method.instructions.insertBefore(insn,hook); hooks++;
      }
    }
    if(hooks!=1) throw new IllegalStateException("MBO thaumometer lens anchor missing");
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer); return writer.toByteArray();
  }
}
