package ru.givler.mbo.integration.angelica.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Only the bundled fallback artwork is filtered; user rule parsing and selection stay native. */
public final class AngelicaCtmTransformer implements IClassTransformer,Opcodes {
  private static final String TARGET="com.prupe.mcpatcher.ctm.TileOverride";
  private static final String HOOK="ru/givler/mbo/integration/angelica/client/AngelicaDefaultCtmRules";
  public byte[] transform(String name,String target,byte[] bytes) {
    if(bytes==null)return bytes;
    if("com.prupe.mcpatcher.ctm.GlassPaneRenderer".equals(target))return paneDiagnostics(bytes);
    if("com.prupe.mcpatcher.mal.block.BlockAPI".equals(target))return legacyIds(bytes);
    if(!TARGET.equals(target))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0;
    for(MethodNode m:node.methods)if(m.name.equals("create") && m.desc.equals("(Lcom/prupe/mcpatcher/mal/resource/PropertiesFile;Lcom/prupe/mcpatcher/mal/tile/TileLoader;)Lcom/prupe/mcpatcher/ctm/TileOverride;")) {
      for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.equals(HOOK))return bytes;
      LabelNode nativeRule=new LabelNode();InsnList code=new InsnList();code.add(new VarInsnNode(ALOAD,0));
      code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"skip","(Ljava/lang/Object;)Z",false));code.add(new JumpInsnNode(IFEQ,nativeRule));
      code.add(new InsnNode(ACONST_NULL));code.add(new InsnNode(ARETURN));code.add(nativeRule);code.add(new FrameNode(F_SAME,0,null,0,null));
      m.instructions.insert(code);count++;
    }
    if(count!=1)throw new IllegalStateException("Angelica CTM factory anchors: "+count);
    System.out.println("[MBO] Angelica stock CTM factory filter installed");
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
  }
  private byte[] legacyIds(byte[] bytes) {
    String hook="ru/givler/mbo/integration/angelica/client/AngelicaLegacyBlockIds";
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0;
    for(MethodNode method:node.methods)if(method.name.equals("createMatchers") && method.desc.equals("(Lcom/prupe/mcpatcher/mal/resource/PropertiesFile;Ljava/lang/String;)Ljava/util/List;")) {
      for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.equals(hook))return bytes;
      InsnList code=new InsnList();code.add(new VarInsnNode(ALOAD,1));
      code.add(new MethodInsnNode(INVOKESTATIC,hook,"resolve","(Ljava/lang/String;)Ljava/lang/String;",false));
      code.add(new VarInsnNode(ASTORE,1));method.instructions.insert(code);count++;
    }
    if(count!=1)throw new IllegalStateException("Angelica numeric block selector anchors: "+count);
    System.out.println("[MBO] Angelica OptiFine numeric block selectors installed");
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
  }
  private byte[] paneDiagnostics(byte[] bytes) {
    String hook="ru/givler/mbo/integration/angelica/client/AngelicaPaneCtmDiagnostics";
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0;
    for(MethodNode method:node.methods)if(method.name.equals("setupIcons") && cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc).equals("(Lnet/minecraft/client/renderer/RenderBlocks;Lnet/minecraft/block/Block;Lnet/minecraft/util/IIcon;III)Z")) {
      for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.equals(hook))return bytes;
      for(AbstractInsnNode i:method.instructions.toArray())if(i.getOpcode()==IRETURN) {
        InsnList code=new InsnList();
        for(int local=0;local<3;local++)code.add(new VarInsnNode(ALOAD,local));
        for(int local=3;local<6;local++)code.add(new VarInsnNode(ILOAD,local));
        code.add(new MethodInsnNode(INVOKESTATIC,hook,"selected","(ZLnet/minecraft/client/renderer/RenderBlocks;Lnet/minecraft/block/Block;Lnet/minecraft/util/IIcon;III)Z",false));
        method.instructions.insertBefore(i,code);
      }
      count++;
    }
    if(count!=1)throw new IllegalStateException("Angelica pane CTM diagnostic anchors: "+count);
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
  }
}
