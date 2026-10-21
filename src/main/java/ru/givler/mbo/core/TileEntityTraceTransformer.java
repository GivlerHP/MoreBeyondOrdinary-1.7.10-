package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Inactive hooks cost only a flag check. No render calls are replaced or cancelled. */
public final class TileEntityTraceTransformer implements IClassTransformer,Opcodes {
  private static final String HOOK="ru/givler/mbo/client/diagnostics/TileEntityRenderTrace";
  public byte[] transform(String name,String target,byte[] bytes) {
    boolean gl="com.gtnewhorizons.angelica.glsm.GLStateManager".equals(target);
    boolean world="net.minecraft.client.renderer.RenderGlobal".equals(target);
    if(bytes==null || (!gl && !world))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
    for(MethodNode m:node.methods)for(AbstractInsnNode i:m.instructions.toArray())
      if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.equals(HOOK))return bytes;
    int count=0;
    for(MethodNode method:node.methods) {
      if(gl && (method.access&ACC_STATIC)!=0 && (method.name.startsWith("glDrawArrays") || method.name.startsWith("glDrawElements") || method.name.equals("glCallList") || method.name.equals("glCallLists"))) {
        InsnList code=new InsnList();code.add(new LdcInsnNode(method.name+method.desc));
        Type[] args=Type.getArgumentTypes(method.desc);
        int index=method.name.startsWith("glDrawArrays")?2:method.name.startsWith("glDrawElements")?1:-1;
        if(index>=0 && args.length>index && args[index].getSort()==Type.INT)code.add(new VarInsnNode(ILOAD,index));
        else code.add(new InsnNode(ICONST_M1));
        code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"draw","(Ljava/lang/String;I)V",false));
        method.instructions.insert(code);count++;
      }
      if(world && method.desc.equals("(IIIIII)V")) {
        String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
        if(!method.name.equals("markBlockRangeForRenderUpdate") && !method.name.equals("func_147585_a") && !mapped.equals("func_147585_a"))continue;
        InsnList code=new InsnList();for(int i=1;i<=6;i++)code.add(new VarInsnNode(ILOAD,i));
        code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"rebuild","(IIIIII)V",false));method.instructions.insert(code);count++;
      }
    }
    if(count==0){System.err.println("[MBO trace] No observation anchors in "+target);return bytes;}
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);
    System.out.println("[MBO trace] Observation hooks: "+target+"="+count);return writer.toByteArray();
  }
}
