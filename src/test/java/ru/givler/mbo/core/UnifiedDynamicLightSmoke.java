package ru.givler.mbo.core;

import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

/** Verify the adapter against the actual shipped FT classes, including production descriptors. */
public final class UnifiedDynamicLightSmoke {
  public static void main(String[] args) throws Exception {
    ProductionTransformersSmoke.Mapping mapping=new ProductionTransformersSmoke.Mapping(Paths.get(args[1]));
    try(ZipFile jar=new ZipFile(args[0])) {
      for(String simple:new String[]{"DynamicLights","DynamicLight"}) {
        String name="com.falsepattern.falsetweaks.modules.dynlights.base."+simple;
        byte[] production=ProductionTransformersSmoke.readStream(jar.getInputStream(jar.getEntry(name.replace('.','/')+".class")));
        ClassWriter mapped=new ClassWriter(0);
        new ClassReader(production).accept(new RemappingClassAdapter(mapped,mapping),ClassReader.EXPAND_FRAMES);
        byte[] input=mapped.toByteArray();
        ColoredDynamicLightTransformer transformer=new ColoredDynamicLightTransformer();
        byte[] result=transformer.transform(name,name,input);
        if(Arrays.equals(input,result))throw new AssertionError("FT adapter was not installed: "+name);
        if(!Arrays.equals(result,transformer.transform(name,name,result)))throw new AssertionError("Adapter is not idempotent");
        ClassNode node=new ClassNode();new ClassReader(result).accept(node,0);
        int marks=0,syncs=0,levels=0;
        for(MethodNode method:node.methods) {
          if((method.access&(Opcodes.ACC_ABSTRACT|Opcodes.ACC_NATIVE))==0)new Analyzer<BasicValue>(new BasicVerifier()).analyze(node.name,method);
          for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction instanceof MethodInsnNode) {
            MethodInsnNode call=(MethodInsnNode)instruction;
            if(!call.owner.endsWith("ColoredDynamicLightBridge"))continue;
            if(call.name.equals("mark"))marks++;
            if(call.name.equals("sync") || call.name.equals("changed"))syncs++;
            if(call.name.equals("level"))levels++;
          }
        }
        if(simple.equals("DynamicLight")?marks!=2:syncs<4 || levels==0)throw new AssertionError("Missing lifecycle hooks");
        System.out.println("FT dynamic-light adapter verified: "+simple+" marks="+marks+" syncs="+syncs+" levels="+levels);
      }
    }
  }
}
