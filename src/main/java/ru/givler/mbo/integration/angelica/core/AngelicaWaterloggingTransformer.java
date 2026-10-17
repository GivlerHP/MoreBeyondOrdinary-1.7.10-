package ru.givler.mbo.integration.angelica.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Optional terrain adapter. Vanilla/OptiFine keep WaterloggedPaneChunkTransformer. */
public final class AngelicaWaterloggingTransformer implements IClassTransformer,Opcodes {
  private static final String ROOT="com.gtnewhorizons.angelica.rendering.celeritas.";
  private static final String HOOK="ru/givler/mbo/client/render/WaterloggedPaneChunkHooks";
  private static final String DATA="ru/givler/mbo/waterlogging/ClientWaterloggedBlocks";
  private static final String VIEW="ru/givler/mbo/core/WaterloggingRenderHooks$ClientView";
  public byte[] transform(String name,String target,byte[] bytes) {
    if(bytes==null || target==null)return bytes;
    boolean slice=target.equals(ROOT+"world.WorldSlice"),task=target.equals(ROOT+"AngelicaChunkBuilderMeshingTask"),
        threaded=target.equals(ROOT+"threading.ThreadedAngelicaChunkBuilderMeshingTask");
    if(!slice && !task && !threaded)return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
    for(FieldNode f:node.fields)if(f.name.equals("mbo$waterChunkHook"))return bytes;
    if(slice)patchSlice(node);else if(task)patchTask(node);else patchThreaded(node);
    node.fields.add(new FieldNode(ACC_PRIVATE|ACC_STATIC|ACC_FINAL|ACC_SYNTHETIC,"mbo$waterChunkHook","Z",null,1));
    ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);
    System.out.println("[MBO] Angelica waterlogging adapter: "+target);
    return out.toByteArray();
  }

  private static FieldNode field(ClassNode node,String name) {
    for(FieldNode f:node.fields)if(f.name.equals(name))return f;
    throw new IllegalStateException("Angelica waterlogging field missing: "+node.name+"."+name);
  }
  private static void patchSlice(ClassNode node) {
    FieldNode world=field(node,"world");for(String axis:new String[]{"baseX","baseY","baseZ"})field(node,axis);
    node.interfaces.add(VIEW);
    node.fields.add(new FieldNode(ACC_PRIVATE,"mbo$waterSnapshot","Ljava/util/Set;",null,null));
    int copies=0,releases=0;
    for(MethodNode m:node.methods) {
      if(m.name.equals("copyData"))for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==RETURN) {
        InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,0));c.add(new VarInsnNode(ALOAD,0));
        c.add(new FieldInsnNode(GETFIELD,node.name,world.name,world.desc));
        // base = origin - 16; snapshot starts at origin - 2 and spans 20 blocks.
        for(String axis:new String[]{"baseX","baseY","baseZ"}) {
          c.add(new VarInsnNode(ALOAD,0));c.add(new FieldInsnNode(GETFIELD,node.name,axis,"I"));
          c.add(new IntInsnNode(BIPUSH,14));c.add(new InsnNode(IADD));
        }
        c.add(new MethodInsnNode(INVOKESTATIC,DATA,"snapshotForRender","(Ljava/lang/Object;III)Ljava/util/Set;",false));
        c.add(new FieldInsnNode(PUTFIELD,node.name,"mbo$waterSnapshot","Ljava/util/Set;"));
        m.instructions.insertBefore(i,c);copies++;
      }
      if(m.name.equals("releaseContext")) {
        InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,0));c.add(new InsnNode(ACONST_NULL));
        c.add(new FieldInsnNode(PUTFIELD,node.name,"mbo$waterSnapshot","Ljava/util/Set;"));m.instructions.insert(c);releases++;
      }
    }
    if(copies!=1 || releases!=1)throw new IllegalStateException("Angelica water snapshot lifecycle anchors: "+copies+"/"+releases);
    MethodNode contains=new MethodNode(ACC_PUBLIC,"mbo$isWaterlogged","(III)Z",null,null);
    contains.visitCode();contains.visitVarInsn(ALOAD,0);contains.visitFieldInsn(GETFIELD,node.name,"mbo$waterSnapshot","Ljava/util/Set;");
    contains.visitVarInsn(ILOAD,1);contains.visitVarInsn(ILOAD,2);contains.visitVarInsn(ILOAD,3);
    contains.visitMethodInsn(INVOKESTATIC,DATA,"snapshotContains","(Ljava/util/Set;III)Z",false);contains.visitInsn(IRETURN);contains.visitMaxs(4,4);contains.visitEnd();node.methods.add(contains);
    MethodNode any=new MethodNode(ACC_PUBLIC,"mbo$hasWaterlogging","()Z",null,null);
    any.visitCode();any.visitVarInsn(ALOAD,0);any.visitFieldInsn(GETFIELD,node.name,"mbo$waterSnapshot","Ljava/util/Set;");
    any.visitMethodInsn(INVOKESTATIC,DATA,"snapshotHasWater","(Ljava/util/Set;)Z",false);any.visitInsn(IRETURN);any.visitMaxs(1,1);any.visitEnd();node.methods.add(any);
  }

  private static void patchTask(ClassNode node) {
    int gates=0,renders=0;
    for(MethodNode m:node.methods) {
      if(m.name.equals("execute") && (m.access&ACC_BRIDGE)==0) {
        int[] context=null;
        for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode) {
          MethodInsnNode c=(MethodInsnNode)i;
          if((c.name.equals("getBlock") || c.name.equals("func_147439_a")) && c.desc.startsWith("(III)")) {
            context=loadsBefore(i,new int[]{ALOAD,ILOAD,ILOAD,ILOAD});break;
          }
        }
        if(context==null)throw new IllegalStateException("Angelica water block coordinates missing");
        for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode) {
          MethodInsnNode call=(MethodInsnNode)i;
          if(!call.name.equals("canRenderInPass") || !call.desc.equals("(I)Z"))continue;
          int[] bp=loadsBefore(i,new int[]{ALOAD,ILOAD});AbstractInsnNode store=nextCode(i);
          if(!(store instanceof VarInsnNode) || store.getOpcode()!=ISTORE)throw new IllegalStateException("Angelica pass result missing");
          int local=((VarInsnNode)store).var;AbstractInsnNode gate=store;
          while((gate=nextCode(gate))!=null)if(gate instanceof VarInsnNode && gate.getOpcode()==ILOAD && ((VarInsnNode)gate).var==local)break;
          if(gate==null || !(nextCode(gate) instanceof JumpInsnNode))throw new IllegalStateException("Angelica pass gate missing");
          InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,bp[0]));c.add(new VarInsnNode(ILOAD,bp[1]));
          c.add(new VarInsnNode(ALOAD,context[0]));for(int n=1;n<4;n++)c.add(new VarInsnNode(ILOAD,context[n]));
          c.add(new MethodInsnNode(INVOKESTATIC,HOOK,"filterAngelicaPass","(ZLnet/minecraft/block/Block;ILnet/minecraft/world/IBlockAccess;III)Z",false));
          m.instructions.insert(gate,c);gates++;
        }
      }
      if(m.name.equals("renderBlock"))for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode) {
        MethodInsnNode c=(MethodInsnNode)i;
        if(!c.name.equals("renderBlockByRenderType") && !c.name.equals("func_147805_b"))continue;
        Type[] args=Type.getArgumentTypes(m.desc);
        if(args.length<8 || !args[6].getClassName().equals("net.minecraft.client.renderer.Tessellator"))throw new IllegalStateException("Angelica renderBlock arguments changed");
        m.instructions.insertBefore(i,new VarInsnNode(ILOAD,6));
        m.instructions.set(i,new MethodInsnNode(INVOKESTATIC,HOOK,"render","(Lnet/minecraft/client/renderer/RenderBlocks;Lnet/minecraft/block/Block;IIII)Z",false));renders++;
      }
    }
    if(gates!=1 || renders!=1)throw new IllegalStateException("Angelica water mesh anchors: "+gates+"/"+renders);
  }

  private static void patchThreaded(ClassNode node) {
    FieldNode access=field(node,"blockAccess");int count=0;
    for(MethodNode m:node.methods)if(m.name.equals("canRenderOffThread")) {
      LabelNode vanilla=new LabelNode();InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,1));c.add(new VarInsnNode(ALOAD,0));
      c.add(new FieldInsnNode(GETFIELD,node.name,access.name,access.desc));
      c.add(new MethodInsnNode(INVOKESTATIC,HOOK,"deferAngelicaBlock","(Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockAccess;)Z",false));
      c.add(new JumpInsnNode(IFEQ,vanilla));c.add(new InsnNode(ICONST_0));c.add(new InsnNode(IRETURN));c.add(vanilla);
      c.add(new FrameNode(F_SAME,0,null,0,null));m.instructions.insert(c);count++;
    }
    if(count!=1)throw new IllegalStateException("Angelica water worker safety anchor missing");
  }
  private static int[] loadsBefore(AbstractInsnNode insn,int[] ops) {
    int[] locals=new int[ops.length];
    for(int n=ops.length-1;n>=0;n--) {
      do{insn=insn.getPrevious();}while(insn!=null && insn.getOpcode()<0);
      if(!(insn instanceof VarInsnNode) || insn.getOpcode()!=ops[n])throw new IllegalStateException("Angelica water argument pattern changed");
      locals[n]=((VarInsnNode)insn).var;
    }
    return locals;
  }
  private static AbstractInsnNode nextCode(AbstractInsnNode i){do{i=i.getNext();}while(i!=null && i.getOpcode()<0);return i;}
}
