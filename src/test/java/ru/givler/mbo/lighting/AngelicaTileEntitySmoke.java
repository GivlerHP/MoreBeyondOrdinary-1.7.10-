package ru.givler.mbo.lighting;

import java.util.*;
import ru.givler.mbo.integration.angelica.client.render.VisibleTileEntityHistory;
import ru.givler.mbo.integration.angelica.core.AngelicaRenderTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public final class AngelicaTileEntitySmoke implements Opcodes {
  public static void run() throws Exception {
    VisibleTileEntityHistory<Object> history=new VisibleTileEntityHistory<Object>();
    Object world=new Object(), chest=new Object(), sign=new Object(), replacement=new Object();
    Set<Object> live=Collections.newSetFromMap(new IdentityHashMap<Object,Boolean>());
    live.add(chest);live.add(sign);
    List<Object> frame=new ArrayList<Object>(Arrays.asList(chest,sign));
    if(history.complete(world,frame,live::contains)!=0)throw new AssertionError("Initial frame changed");
    for(int i=0;i<10;i++) {
      frame=new ArrayList<Object>(Collections.singletonList(chest));
      if(history.complete(world,frame,live::contains)!=1 || frame.size()!=2 || frame.get(1)!=sign)
        throw new AssertionError("Live visible sign disappeared while section result was incomplete");
    }
    live.remove(sign);live.add(replacement);
    frame=new ArrayList<Object>(Arrays.asList(chest,replacement));
    if(history.complete(world,frame,live::contains)!=0 || frame.contains(sign))throw new AssertionError("Removed tile retained");
    live.clear();frame=new ArrayList<Object>();
    if(history.complete(world,frame,live::contains)!=0 || !frame.isEmpty())throw new AssertionError("Unloaded/out-of-frustum models retained");
    frame.add(chest);history.complete(world,frame,x->true);
    frame.clear();history.complete(new Object(),frame,x->true);
    if(!frame.isEmpty())throw new AssertionError("Tiles leaked between worlds");
    frame.add(chest);history.complete(world,frame,x->true);history.clear();frame.clear();
    history.complete(world,frame,x->true);if(!frame.isEmpty())throw new AssertionError("Unload did not release history");
    transformer();
    System.out.println("Angelica TESR continuity: rebuild gaps, removal/replacement, culling/unload, world changes and ASM stack verified");
  }
  static void transformer()throws Exception {
    String owner="com/gtnewhorizons/angelica/rendering/celeritas/CeleritasWorldRenderer";
    ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);
    w.visit(V1_8,ACC_PUBLIC,owner,null,"org/embeddedt/embeddium/impl/render/terrain/SimpleWorldRenderer",null);
    w.visitField(ACC_PRIVATE,"frameTEs","Ljava/util/ArrayList;",null,null).visitEnd();
    MethodVisitor m=w.visitMethod(ACC_PUBLIC,"renderBlockEntities","(F)I",null,null);m.visitCode();
    m.visitVarInsn(ALOAD,0);m.visitInsn(ACONST_NULL);
    m.visitMethodInsn(INVOKESPECIAL,"org/embeddedt/embeddium/impl/render/terrain/SimpleWorldRenderer","renderBlockEntities","(Ljava/lang/Object;)I",false);
    m.visitInsn(IRETURN);m.visitMaxs(0,0);m.visitEnd();
    w.visitEnd();
    AngelicaRenderTransformer transformer=new AngelicaRenderTransformer();
    byte[] patched=transformer.transform(owner.replace('/','.'),owner.replace('/','.'),w.toByteArray());
    byte[] twice=transformer.transform(owner.replace('/','.'),owner.replace('/','.'),patched);
    if(!Arrays.equals(patched,twice))throw new AssertionError("Double injection");
    ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);int hooks=0;
    for(MethodNode method:node.methods) {
      new Analyzer<BasicValue>(new BasicInterpreter()).analyze(node.name,method);
      for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.endsWith("AngelicaTileEntityHooks"))hooks++;
    }
    if(hooks!=1)throw new AssertionError("Collection boundary not patched exactly once");
    byte[] absent=transformer.transform("unrelated","unrelated",w.toByteArray());
    if(!Arrays.equals(absent,w.toByteArray()))throw new AssertionError("Optional mod absence changed unrelated class");
  }
}
