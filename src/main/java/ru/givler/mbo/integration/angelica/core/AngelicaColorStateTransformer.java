package ru.givler.mbo.integration.angelica.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Rewrite both producer and consumers, including mixin bytecode before application. */
public final class AngelicaColorStateTransformer implements IClassTransformer, Opcodes {
  private static final String OWNER="com/prupe/mcpatcher/cc/ColorizeBlock";
  private static final byte[] OWNER_BYTES=OWNER.getBytes(java.nio.charset.StandardCharsets.UTF_8);
  private static final String HOOK="ru/givler/mbo/integration/angelica/client/render/ThreadLocalBlockColors";
  private static final String[] FIELDS={"blockColor","isSmooth",
      "colorRedTopLeft","colorRedBottomLeft","colorRedBottomRight","colorRedTopRight",
      "colorGreenTopLeft","colorGreenBottomLeft","colorGreenBottomRight","colorGreenTopRight",
      "colorBlueTopLeft","colorBlueBottomLeft","colorBlueBottomRight","colorBlueTopRight"};
  @Override public byte[] transform(String name,String target,byte[] bytes) {
    AngelicaColorMixinExtension.install();
    // Every consumer must see the same thread-local result, including optional
    // integrations outside Angelica. Skip class parsing unless its constant pool
    // can reference ColorizeBlock at all.
    if(bytes==null || target==null || (!containsOwner(bytes) && !target.equals("com.prupe.mcpatcher.mal.block.RenderBlocksUtils")))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
    if(patch(node,target)==0)return bytes;
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);
    return writer.toByteArray();
  }
  public static int patch(ClassNode node,String target) {
    int count=0;
    if(node.name.equals("com/prupe/mcpatcher/mal/block/RenderBlocksUtils")) {
      for(MethodNode method:node.methods)if(method.name.equals("isAmbientOcclusionEnabled") && method.desc.equals("()Z")) {
        boolean patched=false;
        for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof FieldInsnNode
            && ((FieldInsnNode)i).owner.equals("net/coderbot/iris/Iris"))patched=true;
        if(!patched)for(AbstractInsnNode i:method.instructions.toArray())if(i.getOpcode()==IRETURN) {
          // Iris separateAo forces RenderBlocks' non-AO path even when the
          // Minecraft setting remains on. MCPatcher must prepare that path's RGB.
          InsnList gate=new InsnList();
          gate.add(new FieldInsnNode(GETSTATIC,"net/coderbot/iris/Iris","enabled","Z"));
          gate.add(new FieldInsnNode(GETSTATIC,"net/coderbot/iris/block_rendering/BlockRenderingSettings","INSTANCE","Lnet/coderbot/iris/block_rendering/BlockRenderingSettings;"));
          gate.add(new MethodInsnNode(INVOKEVIRTUAL,"net/coderbot/iris/block_rendering/BlockRenderingSettings","shouldUseSeparateAo","()Z",false));
          gate.add(new InsnNode(IAND));gate.add(new InsnNode(ICONST_1));gate.add(new InsnNode(IXOR));gate.add(new InsnNode(IAND));
          method.instructions.insertBefore(i,gate);count++;
        }
      }
      for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions.toArray()) {
        if(!(instruction instanceof FieldInsnNode))continue;
        FieldInsnNode field=(FieldInsnNode)instruction;
        if(!field.owner.equals(node.name))continue;
        if(field.getOpcode()==GETSTATIC && (field.name.equals("colorMultiplierType") || field.name.equals("nonAOMultipliers"))) {
          String expected=field.name.equals("colorMultiplierType")?"[I":"[[F";
          if(!field.desc.equals(expected))throw new IllegalStateException("Angelica face color table changed");
          method.instructions.set(field,new MethodInsnNode(INVOKESTATIC,HOOK,
              field.name.equals("colorMultiplierType")?"multiplierTypes":"nonAoMultipliers","()"+expected,false));count++;
        } else if(field.name.equals("layerIndex") && field.desc.equals("I") && (field.getOpcode()==GETSTATIC || field.getOpcode()==PUTSTATIC)) {
          boolean read=field.getOpcode()==GETSTATIC;
          method.instructions.insertBefore(field,new IntInsnNode(BIPUSH,14));
          method.instructions.set(field,new MethodInsnNode(INVOKESTATIC,HOOK,read?"getInt":"setInt",read?"(I)I":"(II)V",false));count++;
        }
      }
    }
    for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions.toArray()) {
      if(!(instruction instanceof FieldInsnNode))continue;
      FieldInsnNode field=(FieldInsnNode)instruction;
      if(!field.owner.equals(OWNER) || (field.getOpcode()!=GETSTATIC && field.getOpcode()!=PUTSTATIC))continue;
      int slot=-1;for(int i=0;i<FIELDS.length;i++)if(FIELDS[i].equals(field.name))slot=i;
      if(slot<0)continue;
      String kind=slot==0?"Int":slot==1?"Boolean":"Float";
      String descriptor=slot==0?"I":slot==1?"Z":"F";
      if(!descriptor.equals(field.desc))throw new IllegalStateException("Angelica color field changed: "+field.name);
      boolean read=field.getOpcode()==GETSTATIC;
      method.instructions.insertBefore(field,new IntInsnNode(BIPUSH,slot));
      method.instructions.set(field,new MethodInsnNode(INVOKESTATIC,HOOK,(read?"get":"set")+kind,
          read?"(I)"+descriptor:"("+descriptor+"I)V",false));count++;
    }
    if(count!=0)System.out.println("[MBO] Thread-local MCPatcher block color results: "+target+" accesses="+count);
    // The post-mixin writer may preserve maxStack rather than recompute it.
    if(count!=0)for(MethodNode method:node.methods)method.maxStack++;
    return count;
  }
  private static boolean containsOwner(byte[] bytes) {
    outer:for(int i=0;i<=bytes.length-OWNER_BYTES.length;i++) {
      if(bytes[i]!=OWNER_BYTES[0])continue;
      for(int j=1;j<OWNER_BYTES.length;j++)if(bytes[i+j]!=OWNER_BYTES[j])continue outer;
      return true;
    }
    return false;
  }
}
