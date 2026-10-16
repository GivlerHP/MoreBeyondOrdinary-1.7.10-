package ru.givler.mbo.lighting;

import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

/** A successful reobf task must produce SRG member references, not a dev jar. */
public final class ProductionJarSmoke {
  public static void main(String[] args)throws Exception {
    try(ZipFile jar=new ZipFile(args[0])) {
      ClassNode node=new ClassNode();
      new ClassReader(jar.getInputStream(jar.getEntry("ru/givler/mbo/registry/VanillaBlockReplacer.class"))).accept(node,0);
      boolean found=false;
      for(MethodNode method:node.methods)if(method.name.equals("separateWoodenLocalizationKeys"))
        for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction instanceof FieldInsnNode) {
          FieldInsnNode field=(FieldInsnNode)instruction;
          if(!field.owner.equals("net/minecraft/init/Blocks"))continue;
          if(field.name.equals("wooden_button"))throw new AssertionError("Production jar contains dev Minecraft references; rebuild jar and reobf before deployment");
          if(field.name.equals("field_150471_bO"))found=true;
        }
      if(!found)throw new AssertionError("Production SRG reference missing");
      System.out.println("Production jar verified: SRG Minecraft references present");
    }
  }
}
