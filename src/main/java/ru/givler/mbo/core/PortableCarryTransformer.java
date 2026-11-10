package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Replaces only carried-light passes; normal weapon rendering remains with vanilla/MF2. */
public final class PortableCarryTransformer implements IClassTransformer, Opcodes {
  private static final String HOOK="ru/givler/mbo/client/render/light/PortableCarry";
  @Override public byte[] transform(String name,String transformedName,byte[] bytes) {
    boolean vanilla="net.minecraft.client.renderer.ItemRenderer".equals(transformedName);
    boolean battle="mods.battlegear2.client.utils.BattlegearRenderHelper".equals(transformedName);
    if(bytes==null || !vanilla && !battle)return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);boolean changed=false;
    for(MethodNode method:node.methods) {
      String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
      String descriptor=FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
      boolean main=vanilla && (mapped.equals("renderItemInFirstPerson") || mapped.equals("func_78440_a")) && method.desc.equals("(F)V");
      boolean off=battle && method.name.equals("renderItemInFirstPerson") && descriptor.equals("(FLnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/ItemRenderer;)V");
      boolean third=battle && method.name.equals("renderItemIn3rdPerson") && descriptor.equals("(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/client/model/ModelBiped;FLnet/minecraft/item/ItemStack;)V");
      if(!main && !off && !third)continue;
      boolean patched=false,hasMf2Tail=false;
      for(AbstractInsnNode insn:method.instructions.toArray())if(insn instanceof MethodInsnNode) {
        MethodInsnNode call=(MethodInsnNode)insn;
        patched|=call.owner.equals(HOOK);
        hasMf2Tail|=call.owner.equals("mods/battlegear2/client/utils/BattlegearRenderHelper") && call.name.equals("renderItemInFirstPerson");
      }
      if(patched)continue;
      InsnList code=new InsnList();LabelNode normal=new LabelNode();
      if(main) {
        code.add(new VarInsnNode(ALOAD,0));code.add(new VarInsnNode(FLOAD,1));
        code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"renderMain","(Lnet/minecraft/client/renderer/ItemRenderer;F)Z",false));
      } else if(off) {
        code.add(new VarInsnNode(FLOAD,0));code.add(new VarInsnNode(ALOAD,1));code.add(new VarInsnNode(ALOAD,2));
        code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"renderOffhand","(FLnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/ItemRenderer;)Z",false));
      } else {
        code.add(new VarInsnNode(ALOAD,0));code.add(new VarInsnNode(ALOAD,1));code.add(new VarInsnNode(FLOAD,2));code.add(new VarInsnNode(ALOAD,3));
        code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"renderOffhandThird","(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/client/model/ModelBiped;FLnet/minecraft/item/ItemStack;)Z",false));
      }
      code.add(new JumpInsnNode(IFEQ,normal));
      if(main && hasMf2Tail) {
        code.add(new VarInsnNode(ALOAD,0));code.add(new VarInsnNode(FLOAD,1));
        code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"finishMain","(Lnet/minecraft/client/renderer/ItemRenderer;F)V",false));
      }
      code.add(new InsnNode(RETURN));code.add(normal);code.add(new FrameNode(F_SAME,0,null,0,null));method.instructions.insert(code);changed=true;
    }
    if(!changed)return bytes;
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
  }
}
