package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** The caller survives Angelica replacing RenderGlobal.sortAndRender's entire body. */
public final class DungeonAreaRenderTransformer implements IClassTransformer,Opcodes {
  private static final String HOOK="ru/givler/mbo/client/render/DungeonAreaWorldRenderer";
  public byte[] transform(String name,String target,byte[] bytes) {
    if(bytes==null || !target.equals("net.minecraft.client.renderer.EntityRenderer"))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
    for(MethodNode m:node.methods)for(AbstractInsnNode i:m.instructions.toArray())
      if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.equals(HOOK))return bytes;
    int count=0;
    for(MethodNode m:node.methods)for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode) {
      MethodInsnNode call=(MethodInsnNode)i;
      String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner,call.name,call.desc);
      if(!FMLDeobfuscatingRemapper.INSTANCE.mapType(call.owner).equals("net/minecraft/client/renderer/RenderGlobal")
          || !(call.name.equals("sortAndRender") || call.name.equals("func_72719_a") || mapped.equals("func_72719_a"))
          || !FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(call.desc).equals("(Lnet/minecraft/entity/EntityLivingBase;ID)I"))continue;
      int local=m.maxLocals;m.maxLocals+=4;
      InsnList code=new InsnList();code.add(new VarInsnNode(DSTORE,local));code.add(new VarInsnNode(ISTORE,local+2));code.add(new VarInsnNode(ASTORE,local+3));
      code.add(new VarInsnNode(ILOAD,local+2));code.add(new VarInsnNode(DLOAD,local));
      code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"renderBeforeTerrain","(ID)V",false));
      code.add(new VarInsnNode(ALOAD,local+3));code.add(new VarInsnNode(ILOAD,local+2));code.add(new VarInsnNode(DLOAD,local));
      m.instructions.insertBefore(i,code);count++;
    }
    if(count==0)throw new IllegalStateException("Dungeon terrain-pass caller missing");
    ClassWriter writer=SafeClassWriter.create();node.accept(writer);
    System.out.println("[MBO] Dungeon walls: caller-side terrain hooks="+count);
    return writer.toByteArray();
  }
}
