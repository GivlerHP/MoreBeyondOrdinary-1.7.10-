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
    for(MethodNode m:node.methods)if((m.name.equals("completeDeferred") || m.name.equals("captureDeferred"))) {
      if(original!=null)throw new IllegalStateException("Ambiguous Angelica deferred capture");
      original=m;
    }
    if(original==null)throw new IllegalStateException("Angelica deferred capture missing");
    Type[] args=Type.getArgumentTypes(original.desc);
    Type result=Type.getReturnType(original.desc);
    if(args.length<1 || args.length>2 || java.util.Arrays.stream(args).anyMatch(arg->arg.getSort()!=Type.OBJECT) || result.getSort()!=Type.OBJECT)
      throw new IllegalStateException("Unexpected Angelica deferred capture descriptor: "+original.desc);
    MethodNode wrapper=new MethodNode(original.access,original.name,original.desc,original.signature,
        original.exceptions.toArray(new String[original.exceptions.size()]));
    original.name="mbo$"+original.name+"Isolated";
    original.access=(original.access & ~(ACC_PUBLIC|ACC_PROTECTED)) | ACC_PRIVATE;
    String scope="ru/givler/mbo/integration/angelica/client/render/AngelicaDeferredTessellator";
    String worldPass="com/gtnewhorizons/angelica/rendering/celeritas/threading/RenderPassHelper";
    String forgeHooks="net/minecraftforge/client/ForgeHooksClient";
    int terrainPass=args.length+1, tilePass=terrainPass+1, owned=tilePass+1, value=owned+1;
    Object[] locals=new Object[owned+1];
    locals[0]=node.name;
    for(int i=0;i<args.length;i++)locals[i+1]=args[i].getInternalName();
    locals[terrainPass]=locals[tilePass]=locals[owned]=INTEGER;
    wrapper.visitCode();
    wrapper.visitMethodInsn(INVOKESTATIC,worldPass,"getWorldRenderPass","()I",false);
    wrapper.visitVarInsn(ISTORE,terrainPass);
    wrapper.visitMethodInsn(INVOKESTATIC,"net/minecraftforge/client/MinecraftForgeClient","getRenderPass","()I",false);
    wrapper.visitVarInsn(ISTORE,tilePass);
    wrapper.visitMethodInsn(INVOKESTATIC,scope,"begin","()Z",false);
    wrapper.visitVarInsn(ISTORE,owned);
    Label start=new Label(),end=new Label(),handler=new Label();
    wrapper.visitLabel(start);
    for(int i=0;i<=args.length;i++)wrapper.visitVarInsn(ALOAD,i);
    wrapper.visitMethodInsn(INVOKESPECIAL,node.name,original.name,original.desc,false);
    wrapper.visitVarInsn(ASTORE,value);
    wrapper.visitLabel(end);
    restoreDeferredState(wrapper,worldPass,forgeHooks,scope,terrainPass,tilePass,owned);
    wrapper.visitVarInsn(ALOAD,value);wrapper.visitInsn(ARETURN);
    wrapper.visitLabel(handler);
    wrapper.visitFrame(F_FULL,locals.length,locals,1,new Object[]{"java/lang/Throwable"});
    wrapper.visitVarInsn(ASTORE,value);
    restoreDeferredState(wrapper,worldPass,forgeHooks,scope,terrainPass,tilePass,owned);
    wrapper.visitVarInsn(ALOAD,value);wrapper.visitInsn(ATHROW);
    wrapper.visitTryCatchBlock(start,end,handler,null);
    wrapper.visitMaxs(args.length+1,value+1);wrapper.visitEnd();
    node.methods.add(wrapper);
  }

  private static void restoreDeferredState(MethodNode wrapper,String worldPass,String forgeHooks,String scope,
      int terrainPass,int tilePass,int owned) {
    // Terrain meshing and third-party ISBRHs can change these independently.
    wrapper.visitVarInsn(ILOAD,terrainPass);
    wrapper.visitMethodInsn(INVOKESTATIC,worldPass,"setWorldRenderPass","(I)V",false);
    wrapper.visitVarInsn(ILOAD,tilePass);
    wrapper.visitMethodInsn(INVOKESTATIC,forgeHooks,"setRenderPass","(I)V",false);
    wrapper.visitVarInsn(ILOAD,owned);
    wrapper.visitMethodInsn(INVOKESTATIC,scope,"end","(Z)V",false);
  }
}
