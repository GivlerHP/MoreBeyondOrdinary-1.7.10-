package ru.givler.mbo.integration.angelica.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Optional adapter: Angelica tessellates RenderBlocks in absolute world coordinates. */
public final class AngelicaRenderTransformer implements IClassTransformer, Opcodes {
  @Override public byte[] transform(String name, String transformedName, byte[] bytes) {
    if (bytes == null) return bytes;
    if ("com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer".equals(transformedName)) return patchTiles(bytes);
    if (!"com.gtnewhorizons.angelica.rendering.celeritas.world.WorldSlice".equals(transformedName)) return bytes;
    ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0);
    String coordinates="ru/givler/mbo/client/lighting/ColoredLightRenderHooks$WorldCoordinates";
    if(node.interfaces.contains(coordinates))return bytes;
    node.interfaces.add(coordinates);
    for(String method:new String[]{"lightOriginX","lightOriginY","lightOriginZ"}) {
      MethodNode m=new MethodNode(ACC_PUBLIC,method,"()D",null,null);
      m.instructions.add(new InsnNode(DCONST_0)); m.instructions.add(new InsnNode(DRETURN)); node.methods.add(m);
    }
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer);
    System.out.println("[MBO] Angelica WorldSlice: world-space colored light enabled");
    return writer.toByteArray();
  }
  private static byte[] patchTiles(byte[] bytes) {
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
    String hook="ru/givler/mbo/integration/angelica/client/render/AngelicaTileEntityHooks";
    FieldNode frame=null;
    for(FieldNode f:node.fields)if(f.name.equals("frameTEs"))frame=f;
    if(frame==null)throw new IllegalStateException("Angelica frameTEs missing");
    int patched=0;
    for(MethodNode m:node.methods)if(m.name.equals("renderBlockEntities") && m.desc.equals("(F)I")) {
      for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode) {
        MethodInsnNode call=(MethodInsnNode)i;
        if(call.owner.equals(hook))return bytes;
        if(call.getOpcode()==INVOKESPECIAL && call.owner.equals("org/embeddedt/embeddium/impl/render/terrain/SimpleWorldRenderer")
            && call.name.equals("renderBlockEntities") && call.desc.equals("(Ljava/lang/Object;)I")) {
          InsnList code=new InsnList();code.add(new VarInsnNode(ALOAD,0));code.add(new VarInsnNode(ALOAD,0));
          code.add(new FieldInsnNode(GETFIELD,node.name,frame.name,frame.desc));
          code.add(new MethodInsnNode(INVOKESTATIC,hook,"complete","(Ljava/lang/Object;Ljava/util/List;)V",false));
          m.instructions.insert(i,code);patched++;
        }
      }
    }
    if(patched!=1)throw new IllegalStateException("Angelica TESR collection anchors: "+patched);
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);
    System.out.println("[MBO] Angelica TESR continuity hook installed");
    return writer.toByteArray();
  }
}
