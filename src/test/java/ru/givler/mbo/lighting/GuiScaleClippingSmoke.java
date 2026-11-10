package ru.givler.mbo.lighting;
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import net.minecraft.client.gui.ScaledResolution;
import ru.givler.mbo.integration.angelica.client.gui.AngelicaGuiScale;
import ru.givler.mbo.integration.angelica.core.AngelicaGuiScaleTransformer;
public final class GuiScaleClippingSmoke implements Opcodes {
  public static void main(String[] args)throws Exception {
    Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)f.get(null);
    for(boolean fractional:new boolean[]{false,true}) {
      String name="ScaleFixture"+(fractional?"Fractional":"Vanilla");
      ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V1_8,ACC_PUBLIC,name,null,"net/minecraft/client/gui/ScaledResolution",fractional?new String[]{"com/github/basdxz/rightproperguiscale/mixin/interfaces/client/minecraft/IScaledResolutionMixin"}:null);
      MethodVisitor m=w.visitMethod(ACC_PUBLIC,fractional?"scaleFactor":"getScaleFactor",fractional?"()F":"()I",null,null);m.visitCode();
      if(fractional)m.visitLdcInsn(1.35F);else m.visitInsn(ICONST_3);m.visitInsn(fractional?FRETURN:IRETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();
      final byte[] bytes=w.toByteArray();Class<?> c=new ClassLoader(GuiScaleClippingSmoke.class.getClassLoader()){Class<?> define(){return defineClass(null,bytes,0,bytes.length);}}.define();
      double scale=AngelicaGuiScale.factor((ScaledResolution)unsafe.allocateInstance(c));
      if(Math.abs(scale-(fractional?1.35:3))>1e-6)throw new AssertionError("Wrong clipping scale "+scale);
    }
    try(ZipFile jar=new ZipFile(".angelica-dev/libs/angelica-2.2.33-dev.jar")) {
      for(String name:new String[]{"me.flashyreese.mods.reeses_sodium_options.client.gui.frame.AbstractFrame"}) {
        java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();try(java.io.InputStream in=jar.getInputStream(jar.getEntry(name.replace('.','/')+".class"))){byte[] b=new byte[8192];for(int n;(n=in.read(b))>=0;)out.write(b,0,n);}
        byte[] original=out.toByteArray();AngelicaGuiScaleTransformer patch=new AngelicaGuiScaleTransformer();byte[] changed=patch.transform(name,name,original);
        if(Arrays.equals(original,changed))throw new AssertionError("No scale hook: "+name);
        if(!Arrays.equals(changed,patch.transform(name,name,changed)))throw new AssertionError("Not idempotent");
        ClassNode node=new ClassNode();new ClassReader(changed).accept(node,0);int hooks=0;
        for(MethodNode method:node.methods){if((method.access&(ACC_ABSTRACT|ACC_NATIVE))==0)new Analyzer(new BasicVerifier()).analyze(node.name,method);
          for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.endsWith("/AngelicaGuiScale"))hooks++;}
        if(hooks==0)throw new AssertionError("Missing factor calls");
      }
    }
    System.out.println("PASS fractional clipping 1.35, vanilla fallback 3, actual Angelica settings-frame bytecode and idempotency");
  }
}
