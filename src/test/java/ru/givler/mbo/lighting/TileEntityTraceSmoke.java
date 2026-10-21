package ru.givler.mbo.lighting;

import java.util.Arrays;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import ru.givler.mbo.client.diagnostics.TileEntityRenderTrace;
import ru.givler.mbo.core.TileEntityTraceTransformer;

/** Observe draws and SRG rebuild calls without requiring a display or modifying their return paths. */
public final class TileEntityTraceSmoke implements Opcodes {
  public static void run()throws Exception {
    // Disabled diagnostics must not touch Minecraft or require an OpenGL context.
    if(TileEntityRenderTrace.begin(null)!=0)throw new AssertionError("Trace enabled by default");
    TileEntityRenderTrace.draw("disabled",0);TileEntityRenderTrace.end(0);
    TileEntityRenderTrace.rebuild(0,0,0,1,1,1);TileEntityRenderTrace.deferred("disabled");
    String gl="com/gtnewhorizons/angelica/glsm/GLStateManager";
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);writer.visit(V1_8,ACC_PUBLIC,gl,null,"java/lang/Object",null);
    method(writer,"glDrawArrays","(III)V",true);
    method(writer,"glDrawElements","(IIILjava/nio/ByteBuffer;)V",true);
    method(writer,"glDrawElements","(ILjava/nio/IntBuffer;)V",true);
    method(writer,"glCallList","(I)V",true);
    method(writer,"notADraw","()V",true);writer.visitEnd();
    check(gl,writer.toByteArray(),4);
    String world="net/minecraft/client/renderer/RenderGlobal";
    writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);writer.visit(V1_8,ACC_PUBLIC,world,null,"java/lang/Object",null);
    method(writer,"func_147585_a","(IIIIII)V",false);writer.visitEnd();check(world,writer.toByteArray(),1);
    System.out.println("TESR trace: disabled mode needs no GL, draw overload stacks, SRG rebuild hook and idempotence passed");
  }
  private static void method(ClassWriter writer,String name,String desc,boolean isStatic) {
    MethodVisitor m=writer.visitMethod(ACC_PUBLIC|(isStatic?ACC_STATIC:0),name,desc,null,null);
    m.visitCode();m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
  }
  private static void check(String name,byte[] input,int expected)throws Exception {
    TileEntityTraceTransformer transformer=new TileEntityTraceTransformer();
    byte[] output=transformer.transform(name.replace('/','.'),name.replace('/','.'),input);
    if(!Arrays.equals(output,transformer.transform(name.replace('/','.'),name.replace('/','.'),output)))throw new AssertionError("Repeated trace injection");
    ClassNode node=new ClassNode();new ClassReader(output).accept(node,0);int hooks=0;
    for(MethodNode m:node.methods) {
      new Analyzer<BasicValue>(new BasicInterpreter()).analyze(node.name,m);
      for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.endsWith("TileEntityRenderTrace"))hooks++;
    }
    if(hooks!=expected)throw new AssertionError("Expected trace hooks "+expected+", actual "+hooks);
  }
}
