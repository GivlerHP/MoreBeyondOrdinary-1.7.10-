package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Scope only RenderBlocks world faces; UV changes never leak into items, GUI or particles. */
public final class NaturalTextureTransformer implements IClassTransformer,Opcodes {
  private static final String HOOK="ru/givler/mbo/client/texture/NaturalTextureHooks";
  private static final String[] MCP={"renderFaceYNeg","renderFaceYPos","renderFaceZNeg","renderFaceZPos","renderFaceXNeg","renderFaceXPos"};
  private static final String[] SRG={"func_147768_a","func_147806_b","func_147761_c","func_147734_d","func_147798_e","func_147764_f"};
  public byte[] transform(String name,String target,byte[] bytes) {
    boolean faces="net.minecraft.client.renderer.RenderBlocks".equals(target),tess="net.minecraft.client.renderer.Tessellator".equals(target),
      builtin="com.gtnewhorizons.angelica.utils.NaturalTextureUtils".equals(target);
    if(bytes==null || !(faces||tess||builtin))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
    for(MethodNode m:node.methods)for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.equals(HOOK))return bytes;
    int count=0;
    for(MethodNode m:node.methods) {
      String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,m.name,m.desc);
      String desc=FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(m.desc);
      if(faces && desc.equals("(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V")) {
        int side=-1;for(int j=0;j<6;j++)if(m.name.equals(MCP[j]) || m.name.equals(SRG[j]) || mapped.equals(SRG[j]))side=j;
        if(side<0)continue;int saved=m.maxLocals;m.maxLocals+=3;
        LabelNode start=new LabelNode(),end=new LabelNode(),handler=new LabelNode();InsnList begin=new InsnList();
        begin.add(new VarInsnNode(ALOAD,0));begin.add(new VarInsnNode(ALOAD,1));
        begin.add(new VarInsnNode(DLOAD,2));begin.add(new VarInsnNode(DLOAD,4));begin.add(new VarInsnNode(DLOAD,6));begin.add(new VarInsnNode(ALOAD,8));begin.add(new IntInsnNode(BIPUSH,side));
        begin.add(new MethodInsnNode(INVOKESTATIC,HOOK,"begin","(Lnet/minecraft/client/renderer/RenderBlocks;Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;I)J",false));
        begin.add(new VarInsnNode(LSTORE,saved));begin.add(start);m.instructions.insert(begin);
        for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==RETURN)m.instructions.insertBefore(i,finish(saved));
        m.instructions.add(end);m.instructions.add(handler);m.instructions.add(new VarInsnNode(ASTORE,saved+2));m.instructions.add(finish(saved));
        m.instructions.add(new VarInsnNode(ALOAD,saved+2));m.instructions.add(new InsnNode(ATHROW));m.tryCatchBlocks.add(new TryCatchBlockNode(start,end,handler,null));count++;
      }
      if(tess && desc.equals("(DDDDD)V") && (m.name.equals("addVertexWithUV") || m.name.equals("func_78374_a") || mapped.equals("func_78374_a"))) {
        int temp=m.maxLocals;m.maxLocals+=2;InsnList code=new InsnList();
        code.add(new VarInsnNode(DLOAD,7));code.add(new VarInsnNode(DLOAD,9));code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"u","(DD)D",false));code.add(new VarInsnNode(DSTORE,temp));
        code.add(new VarInsnNode(DLOAD,7));code.add(new VarInsnNode(DLOAD,9));code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"v","(DD)D",false));code.add(new VarInsnNode(DSTORE,9));
        code.add(new VarInsnNode(DLOAD,temp));code.add(new VarInsnNode(DSTORE,7));m.instructions.insert(code);count++;
      }
      if(builtin && m.name.equals("isNaturalBlock") && desc.equals("(Lnet/minecraft/block/Block;)Z")) {
        for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==IRETURN)m.instructions.insertBefore(i,new MethodInsnNode(INVOKESTATIC,HOOK,"allowBuiltin","(Z)Z",false));count++;
      }
    }
    if(count!=(faces?6:1))throw new IllegalStateException("Natural texture anchors missing: "+target+" "+count);
    ClassWriter writer=SafeClassWriter.create();node.accept(writer);return writer.toByteArray();
  }
  private static InsnList finish(int local){InsnList code=new InsnList();code.add(new VarInsnNode(LLOAD,local));code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"end","(J)V",false));return code;}
}
