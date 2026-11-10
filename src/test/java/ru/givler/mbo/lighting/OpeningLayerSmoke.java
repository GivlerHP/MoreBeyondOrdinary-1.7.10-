package ru.givler.mbo.lighting;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import ru.givler.mbo.integration.angelica.core.AngelicaRenderTransformer;

/** Exercise the installed Java 8/17 Angelica terrain boundaries, not vanilla's overwritten method. */
public final class OpeningLayerSmoke {
  public static void main(String[] args)throws Exception {
    String target="com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer";
    try(ZipFile jar=new ZipFile(args[0])) {
      for(String prefix:new String[]{"","META-INF/versions/17/"}) {
        ZipEntry entry=jar.getEntry(prefix+target.replace('.','/')+".class");
        if(entry==null)throw new AssertionError("Missing installed renderer "+prefix);
        byte[] input;
        try(java.io.InputStream in=jar.getInputStream(entry);java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()){
          byte[] b=new byte[8192];for(int n;(n=in.read(b))>=0;)out.write(b,0,n);input=out.toByteArray();
        }
        // This standalone Forge test uses ASM5. Inspect the unchanged Java17 method bodies
        // with a supported header; no renderer class is defined or executed by this test.
        if((input[7]&255)>52){input[6]=0;input[7]=52;}
        AngelicaRenderTransformer patch=new AngelicaRenderTransformer();
        byte[] output=patch.transform(target,target,input);
        if(Arrays.equals(input,output)||!Arrays.equals(output,patch.transform(target,target,output)))throw new AssertionError("Missing or repeated terrain hook");
        ClassNode node=new ClassNode();new ClassReader(output).accept(node,0);int boundaries=0;
        for(MethodNode method:node.methods)if(method.name.equals("drawChunkLayer")&&method.desc.startsWith("(Lcom/gtnewhorizons/angelica/rendering/celeritas/BlockRenderLayer;")){
          new Analyzer(new BasicVerifier()).analyze(node.name,method);
          for(AbstractInsnNode i:method.instructions.toArray())if(i.getOpcode()==Opcodes.RETURN){
            AbstractInsnNode call=i.getPrevious();
            if(!(call instanceof MethodInsnNode)||!((MethodInsnNode)call).name.equals("renderAfterAngelicaOpaqueLayer"))throw new AssertionError("Terrain exit misses animation draw");
            boundaries++;
          }
        }
        if(boundaries==0)throw new AssertionError("No real terrain boundary");
      }
    }
    AngelicaTileEntitySmoke.run();
    System.out.println("PASS installed Angelica Java 8/17 opaque animation boundaries, ASM stacks, idempotency and TESR collection");
  }
}
