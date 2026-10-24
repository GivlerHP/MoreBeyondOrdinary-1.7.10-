package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class RandomMobTextureTransformer implements IClassTransformer,Opcodes {
  private static final String HOOK="ru/givler/mbo/client/texture/RandomMobTextureHooks";
  public byte[] transform(String name,String target,byte[] bytes) {
    boolean render="net.minecraft.client.renderer.entity.Render".equals(target),nativeMob="com.prupe.mcpatcher.mob.MobRandomizer".equals(target);
    if(bytes==null||!(render||nativeMob))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0;
    for(MethodNode method:node.methods)for(AbstractInsnNode i:method.instructions.toArray())
      if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(HOOK))return bytes;
    for(MethodNode m:node.methods) {
      String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,m.name,m.desc);
      String desc=FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(m.desc);
      if(render&&(m.name.equals("bindEntityTexture")||m.name.equals("func_110777_b")||mapped.equals("func_110777_b"))
          &&(desc.equals("(Lnet/minecraft/entity/Entity;)V")||desc.equals("(Lnet/minecraft/entity/Entity;)Z"))) {
        for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode) {
          MethodInsnNode call=(MethodInsnNode)i;
          String cd=FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(call.desc);
          if(cd.equals("(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/ResourceLocation;")) {
            InsnList code=new InsnList();code.add(new VarInsnNode(ALOAD,1));
            code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"resolve","(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/ResourceLocation;",false));
            m.instructions.insert(i,code);count++;
          }
        }
      }
      if(nativeMob&&m.name.equals("randomTexture")&&desc.equals("(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;")) {
        m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();
        m.instructions.add(new VarInsnNode(ALOAD,1));m.instructions.add(new VarInsnNode(ALOAD,0));
        m.instructions.add(new MethodInsnNode(INVOKESTATIC,HOOK,"resolve","(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/ResourceLocation;",false));
        m.instructions.add(new InsnNode(ARETURN));count++;
      }
    }
    if(count!=1)throw new IllegalStateException("Random mob texture anchors: "+target+" "+count);
    ClassWriter writer=SafeClassWriter.create();node.accept(writer);return writer.toByteArray();
  }
}
