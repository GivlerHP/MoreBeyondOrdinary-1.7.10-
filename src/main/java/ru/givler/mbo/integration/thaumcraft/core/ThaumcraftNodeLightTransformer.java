package ru.givler.mbo.integration.thaumcraft.core;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
/** Invalidate RGB only when Thaumcraft receives updated node data. */
public final class ThaumcraftNodeLightTransformer implements IClassTransformer,Opcodes {
  private static final String HOOK="ru/givler/mbo/client/lighting/ThaumcraftNodeLight";
  public byte[] transform(String name,String target,byte[] bytes) {
    if(bytes==null || !target.equals("thaumcraft.api.TileThaumcraft"))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);boolean changed=false;
    for(MethodNode m:node.methods)if(m.name.equals("onDataPacket")) {
      for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(HOOK))return bytes;
      for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==RETURN) {
        InsnList code=new InsnList();code.add(new VarInsnNode(ALOAD,0));code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"updated","(Lnet/minecraft/tileentity/TileEntity;)V",false));m.instructions.insertBefore(i,code);changed=true;
      }
    }
    if(!changed)throw new IllegalStateException("Thaumcraft node RGB packet hook missing");
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
  }
}
