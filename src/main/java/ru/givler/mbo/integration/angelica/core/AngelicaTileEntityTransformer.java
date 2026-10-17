package ru.givler.mbo.integration.angelica.core;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public final class AngelicaTileEntityTransformer implements IClassTransformer,Opcodes {
  private static final String ROOT="com.gtnewhorizons.angelica.rendering.celeritas.";
  private static final String HOOK="ru/givler/mbo/integration/angelica/client/render/AngelicaTileEntitySnapshot";
  public byte[] transform(String name,String target,byte[] bytes) {
    boolean task=target.equals(ROOT+"AngelicaChunkBuilderMeshingTask"),manager=target.equals(ROOT+"AngelicaRenderSectionManager");
    if(bytes==null || (!task && !manager))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
    if(node.fields.stream().anyMatch(f->f.name.equals("mbo$clientTileSnapshot")))return bytes;
    int count=0;
    for(MethodNode m:node.methods) {
      if(task && m.name.equals("execute"))for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode) {
        MethodInsnNode call=(MethodInsnNode)i;
        if(call.name.equals("hasTileEntity") && call.desc.equals("(I)Z")) {
          // Terrain geometry still uses the same worker. Only the live-TE collection branch is skipped.
          InsnList skip=new InsnList();skip.add(new InsnNode(POP2));skip.add(new InsnNode(ICONST_0));
          m.instructions.insertBefore(i,skip);m.instructions.remove(i);count++;
        }
      }
      if(manager && m.name.equals("updateSectionInfo") && m.desc.endsWith(";)Z")) {
        InsnList code=new InsnList();code.add(new VarInsnNode(ALOAD,0));code.add(new VarInsnNode(ALOAD,1));code.add(new VarInsnNode(ALOAD,2));
        code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"refresh","(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V",false));m.instructions.insert(code);count++;
      }
    }
    if(count!=1)throw new IllegalStateException("Angelica client TESR snapshot anchors: "+target+"="+count);
    if(task) isolateDeferred(node);
    node.fields.add(new FieldNode(ACC_PRIVATE|ACC_STATIC|ACC_FINAL|ACC_SYNTHETIC,"mbo$clientTileSnapshot","Z",null,1));
    ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);return out.toByteArray();
  }

  private static void isolateDeferred(ClassNode node) {
    MethodNode original=null;
    for(MethodNode m:node.methods)if(m.name.equals("completeDeferred")) {
      if(original!=null)throw new IllegalStateException("Ambiguous Angelica completeDeferred");
      original=m;
    }
    if(original==null)throw new IllegalStateException("Angelica completeDeferred missing");
    Type[] args=Type.getArgumentTypes(original.desc);
    Type result=Type.getReturnType(original.desc);
    if(args.length!=2 || args[0].getSort()!=Type.OBJECT || args[1].getSort()!=Type.OBJECT || result.getSort()!=Type.OBJECT)
      throw new IllegalStateException("Unexpected Angelica completeDeferred descriptor: "+original.desc);
    MethodNode wrapper=new MethodNode(original.access,original.name,original.desc,original.signature,
        original.exceptions.toArray(new String[original.exceptions.size()]));
    original.name="mbo$completeDeferredIsolated";
    original.access=(original.access & ~(ACC_PUBLIC|ACC_PROTECTED)) | ACC_PRIVATE;
    String scope="ru/givler/mbo/integration/angelica/client/render/AngelicaDeferredTessellator";
    wrapper.visitCode();
    wrapper.visitMethodInsn(INVOKESTATIC,scope,"begin","()Z",false);
    wrapper.visitVarInsn(ISTORE,3);
    Label start=new Label(),end=new Label(),handler=new Label();
    wrapper.visitLabel(start);
    wrapper.visitVarInsn(ALOAD,0);wrapper.visitVarInsn(ALOAD,1);wrapper.visitVarInsn(ALOAD,2);
    wrapper.visitMethodInsn(INVOKESPECIAL,node.name,original.name,original.desc,false);
    wrapper.visitVarInsn(ASTORE,4);
    wrapper.visitLabel(end);
    wrapper.visitVarInsn(ILOAD,3);wrapper.visitMethodInsn(INVOKESTATIC,scope,"end","(Z)V",false);
    wrapper.visitVarInsn(ALOAD,4);wrapper.visitInsn(ARETURN);
    wrapper.visitLabel(handler);
    wrapper.visitFrame(F_FULL,4,new Object[]{node.name,args[0].getInternalName(),args[1].getInternalName(),INTEGER},1,new Object[]{"java/lang/Throwable"});
    wrapper.visitVarInsn(ASTORE,4);
    wrapper.visitVarInsn(ILOAD,3);wrapper.visitMethodInsn(INVOKESTATIC,scope,"end","(Z)V",false);
    wrapper.visitVarInsn(ALOAD,4);wrapper.visitInsn(ATHROW);
    wrapper.visitTryCatchBlock(start,end,handler,null);
    wrapper.visitMaxs(3,5);wrapper.visitEnd();
    node.methods.add(wrapper);
  }
}
