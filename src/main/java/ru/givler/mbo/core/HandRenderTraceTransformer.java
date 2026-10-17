package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Passive first-person trace. Hooks do nothing unless Angelica compatibility enables them. */
public final class HandRenderTraceTransformer implements IClassTransformer, Opcodes {
  private static final String HOOK="ru/givler/mbo/integration/angelica/client/HandRenderTrace";
  @Override public byte[] transform(String name,String target,byte[] bytes) {
    if(bytes==null)return bytes;
    boolean item="net.minecraft.client.renderer.ItemRenderer".equals(target)
        || "com.geco.darkrootgarden.client.render.item.AnimatedFirstPersonItemRenderer".equals(target);
    boolean owner="com.geco.darkrootgarden.client.animation.PlayerAnimationFirstPersonInstaller".equals(target);
    if(!item && !owner)return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
    for(MethodNode m:node.methods)for(AbstractInsnNode i:m.instructions.toArray())
      if(i instanceof MethodInsnNode && HOOK.equals(((MethodInsnNode)i).owner))return bytes;
    int count=0;
    for(MethodNode m:node.methods) {
      if(owner && m.name.equals("renderHand") && m.desc.equals("(FI)Z")) {
        for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==IRETURN) {
          InsnList trace=new InsnList();trace.add(new InsnNode(DUP));
          trace.add(new MethodInsnNode(INVOKESTATIC,HOOK,"gardenOwnsHand","(Z)V",false));m.instructions.insertBefore(i,trace);
        }
        count++;
      } else if(item && m.desc.equals("(F)V") && (m.name.equals("renderItemInFirstPerson") || m.name.equals("func_78440_a") || m.name.equals("a"))) {
        InsnList enter=new InsnList();enter.add(new VarInsnNode(ALOAD,0));
        enter.add(new MethodInsnNode(INVOKESTATIC,HOOK,"enter","(Ljava/lang/Object;)V",false));m.instructions.insert(enter);
        for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==RETURN)
          m.instructions.insertBefore(i,new MethodInsnNode(INVOKESTATIC,HOOK,"exit","()V",false));
        count++;
      } else if(item && cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(m.desc).startsWith("(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;I")) {
        InsnList draw=new InsnList();draw.add(new VarInsnNode(ALOAD,2));
        draw.add(new MethodInsnNode(INVOKESTATIC,HOOK,"item","(Ljava/lang/Object;)V",false));m.instructions.insert(draw);
        count++;
      }
    }
    if(count==0)throw new IllegalStateException("First-person trace anchors absent: "+target);
    ClassWriter writer=SafeClassWriter.create();node.accept(writer);
    System.out.println("[MBO HandTrace] Instrumented "+target+": "+count);
    return writer.toByteArray();
  }
}
