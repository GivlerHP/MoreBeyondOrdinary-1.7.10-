import java.io.*;
import java.nio.file.*;
import java.util.jar.*;
import cpw.mods.fml.common.patcher.ClassPatchManager;
import cpw.mods.fml.relauncher.Side;

/** Run with Java 8 and the tested Crucible JAR first on the classpath. */
public final class ExtractCrucible {
  public static void main(String[] args) throws Exception {
    java.lang.reflect.Field side=cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");
    side.setAccessible(true);side.set(null,Side.SERVER);
    ClassPatchManager.INSTANCE.setup(Side.SERVER);
    try(JarFile vanilla=new JarFile(args[0])) {
      for(int i=2;i<args.length;i+=2) {
        String raw=args[i],mapped=args[i+1];
        ByteArrayOutputStream output=new ByteArrayOutputStream();
        try(InputStream input=vanilla.getInputStream(vanilla.getJarEntry(raw.replace('.','/')+".class"))) {
          byte[] buffer=new byte[8192];int count;
          while((count=input.read(buffer))!=-1)output.write(buffer,0,count);
        }
        byte[] patched=ClassPatchManager.INSTANCE.applyPatch(raw,mapped,output.toByteArray());
        Path target=Paths.get(args[1],mapped.replace('.','/')+".class");
        Files.createDirectories(target.getParent());Files.write(target,patched);
      }
    }
  }
}
