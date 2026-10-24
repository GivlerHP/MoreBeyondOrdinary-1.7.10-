package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Extend vanilla metadata and sprite loading without replacing the animation scheduler. */
public final class TextureInterpolationTransformer implements IClassTransformer,Opcodes {
  private static final String HOOK="ru/givler/mbo/client/texture/TextureInterpolation";
  public byte[] transform(String name,String target,byte[] bytes) {
    boolean serializer="net.minecraft.client.resources.data.AnimationMetadataSectionSerializer".equals(target);
    boolean sprite="net.minecraft.client.renderer.texture.TextureAtlasSprite".equals(target);
    if(bytes==null||!(serializer||sprite))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
    for(MethodNode method:node.methods)for(AbstractInsnNode i:method.instructions.toArray())
      if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(HOOK))return bytes;
    int methods=0;
    for(MethodNode method:node.methods) {
      String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
      String desc=FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
      boolean read=serializer&&desc.equals("(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/resources/data/AnimationMetadataSection;");
      boolean write=serializer&&desc.equals("(Lnet/minecraft/client/resources/data/AnimationMetadataSection;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;");
      boolean load=sprite&&desc.equals("([Ljava/awt/image/BufferedImage;Lnet/minecraft/client/resources/data/AnimationMetadataSection;Z)V");
      boolean tick=sprite&&(method.name.equals("updateAnimation")||mapped.equals("func_94219_l"))&&desc.equals("()V");
      boolean clear=sprite&&(method.name.equals("clearFramesTextureData")||mapped.equals("func_130103_l"))&&desc.equals("()V");
      if(!(read||write||load||tick||clear))continue;
      for(AbstractInsnNode i:method.instructions.toArray())if(i.getOpcode()==(read||write?ARETURN:RETURN)) {
        InsnList code=new InsnList();
        if(read) {
          code.add(new VarInsnNode(ALOAD,1));
          code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"read","(Lnet/minecraft/client/resources/data/AnimationMetadataSection;Lcom/google/gson/JsonElement;)Lnet/minecraft/client/resources/data/AnimationMetadataSection;",false));
        }else if(write) {
          code.add(new VarInsnNode(ALOAD,1));
          code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"write","(Lcom/google/gson/JsonElement;Lnet/minecraft/client/resources/data/AnimationMetadataSection;)Lcom/google/gson/JsonElement;",false));
        }else {
          code.add(new VarInsnNode(ALOAD,0));if(load)code.add(new VarInsnNode(ALOAD,2));
          code.add(new MethodInsnNode(INVOKESTATIC,HOOK,load?"loaded":tick?"update":"clear",load?"(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/resources/data/AnimationMetadataSection;)V":"(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V",false));
        }
        method.instructions.insertBefore(i,code);
      }
      methods++;
    }
    if(methods!=(serializer?2:3))throw new IllegalStateException("Texture interpolation anchors: "+target+" "+methods);
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
  }
}
