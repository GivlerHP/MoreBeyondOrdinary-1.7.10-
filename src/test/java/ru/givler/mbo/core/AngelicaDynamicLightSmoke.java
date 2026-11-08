package ru.givler.mbo.core;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
public final class AngelicaDynamicLightSmoke {
  public static void main(String[] args)throws Exception {
    String name="com.gtnewhorizons.angelica.dynamiclights.DynamicLights";
    byte[] input;
    try(ZipFile jar=new ZipFile(args[0])) {
      input=ProductionTransformersSmoke.readStream(jar.getInputStream(jar.getEntry(name.replace('.','/')+".class")));
    }
    ColoredDynamicLightTransformer transformer=new ColoredDynamicLightTransformer();
    byte[] output=transformer.transform(name,name,input);
    if(Arrays.equals(input,output)||!Arrays.equals(output,transformer.transform(name,name,output)))throw new AssertionError("Missing or non-idempotent hook");
    ClassNode node=new ClassNode();new ClassReader(output).accept(node,0);
    int hooks=0,returns=0;
    for(MethodNode m:node.methods)if(m.name.equals("updateAll")) {
      new Analyzer<BasicValue>(new BasicVerifier()).analyze(node.name,m);
      for(AbstractInsnNode i:m.instructions.toArray()) {
        if(i.getOpcode()==Opcodes.RETURN){returns++;AbstractInsnNode p=i.getPrevious();if(!(p instanceof MethodInsnNode)||!((MethodInsnNode)p).name.equals("sync"))throw new AssertionError("Unsynchronised return");}
        if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.endsWith("/AngelicaDynamicLightBridge"))hooks++;
      }
    }
    if(hooks==0||hooks!=returns)throw new AssertionError("Lifecycle hooks missing");
    System.out.println("ANGELICA_DYNAMIC_LIGHT_OK hooks="+hooks);
  }
}
