package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Scope RGB around model rendering only; shadows, fire and debug geometry stay outside. */
public final class ColoredEntityLightTransformer implements IClassTransformer,Opcodes {
  private static final String HOOK="ru/givler/mbo/client/lighting/ColoredEntityLightHooks";
  public byte[] transform(String name,String transformedName,byte[] bytes) {
    if(bytes==null || !"net.minecraft.client.renderer.entity.RenderManager".equals(transformedName))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
    for(MethodNode method:node.methods)for(AbstractInsnNode insn:method.instructions.toArray())
      if(insn instanceof MethodInsnNode && HOOK.equals(((MethodInsnNode)insn).owner))return bytes;
    int patched=0;
    for(MethodNode method:node.methods) {
      String desc=FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
      String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
      if(!desc.equals("(Lnet/minecraft/entity/Entity;DDDFFZ)Z") || !(method.name.equals("func_147939_a") || method.name.equals("doRenderEntity") || mapped.equals("func_147939_a")))continue;
      int token=method.maxLocals++, error=method.maxLocals++;
      LabelNode start=new LabelNode(),end=new LabelNode(),handler=new LabelNode();
      InsnList init=new InsnList();init.add(new InsnNode(ICONST_0));init.add(new VarInsnNode(ISTORE,token));init.add(start);method.instructions.insert(init);
      for(AbstractInsnNode insn:method.instructions.toArray()) {
        if(insn instanceof MethodInsnNode) {
          MethodInsnNode call=(MethodInsnNode)insn;
          String callName=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner,call.name,call.desc);
          if(call.getOpcode()==INVOKEVIRTUAL && FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(call.desc).equals("(Lnet/minecraft/entity/Entity;DDDFF)V")
              && (call.name.equals("doRender") || call.name.equals("func_76986_a") || callName.equals("func_76986_a"))) {
            InsnList begin=new InsnList();begin.add(new VarInsnNode(ALOAD,1));begin.add(new VarInsnNode(FLOAD,9));
            begin.add(new MethodInsnNode(INVOKESTATIC,HOOK,"begin","(Lnet/minecraft/entity/Entity;F)I",false));begin.add(new VarInsnNode(ISTORE,token));
            method.instructions.insertBefore(call,begin);
            InsnList finish=restore(token);finish.add(new InsnNode(ICONST_0));finish.add(new VarInsnNode(ISTORE,token));method.instructions.insert(call,finish);patched++;
          }
        }
        if(insn.getOpcode()==IRETURN)method.instructions.insertBefore(insn,restore(token));
      }
      method.instructions.add(end);method.instructions.add(handler);method.instructions.add(new VarInsnNode(ASTORE,error));
      method.instructions.add(restore(token));method.instructions.add(new VarInsnNode(ALOAD,error));method.instructions.add(new InsnNode(ATHROW));
      method.tryCatchBlocks.add(new TryCatchBlockNode(start,end,handler,null));
    }
    if(patched!=1){System.err.println("[MBO ASM] Entity RGB anchor unavailable: "+patched);return bytes;}
    ClassWriter writer=SafeClassWriter.create();node.accept(writer);return writer.toByteArray();
  }
  private static InsnList restore(int token){InsnList list=new InsnList();list.add(new VarInsnNode(ILOAD,token));list.add(new MethodInsnNode(INVOKESTATIC,HOOK,"end","(I)V",false));return list;}
}
