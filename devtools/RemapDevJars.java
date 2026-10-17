import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

/** Replaces the SRG-to-MCP dev service supplied by legacy CodeChickenCore. Never shipped in MBO. */
public final class RemapDevJars {
  public static void main(String[] args)throws Exception {
    final Map<String,String> names=new HashMap<String,String>();
    for(String line:Files.readAllLines(Paths.get(args[0]))) {
      String[] p=line.split(" ");
      if(p[0].equals("FD:"))names.put(simple(p[1]),simple(p[2]));
      if(p[0].equals("MD:"))names.put(simple(p[1]),simple(p[3]));
    }
    Remapper mapper=new Remapper(){
      public String mapFieldName(String owner,String name,String desc){return names.getOrDefault(name,name);}
      public String mapMethodName(String owner,String name,String desc){return names.getOrDefault(name,name);}
      public Object mapValue(Object value){return value instanceof String?names.getOrDefault(value,value.toString()):super.mapValue(value);}
    };
    Path dir=Paths.get(args[1]);Files.createDirectories(dir);
    for(int i=2;i<args.length;i++) {
      Path input=Paths.get(args[i]),target=dir.resolve(input.getFileName());int changed=0;
      try(JarFile jar=new JarFile(input.toFile());JarOutputStream out=new JarOutputStream(Files.newOutputStream(target))) {
        Enumeration<JarEntry> entries=jar.entries();
        while(entries.hasMoreElements()) {
          JarEntry e=entries.nextElement();String name=e.getName();
          if(e.isDirectory() || name.startsWith("META-INF/versions/") || name.equals("module-info.class") || name.matches("META-INF/[^/]+\\.(SF|RSA|DSA)"))continue;
          ByteArrayOutputStream bytes=new ByteArrayOutputStream();
          try(InputStream stream=jar.getInputStream(e)){byte[] b=new byte[8192];int n;while((n=stream.read(b))!=-1)bytes.write(b,0,n);}
          byte[] data=bytes.toByteArray();
          if(name.endsWith(".class")) {
            ClassWriter writer=new ClassWriter(0);
            new ClassReader(data).accept(new RemappingClassAdapter(writer,mapper),ClassReader.EXPAND_FRAMES);
            data=writer.toByteArray();changed++;
          }
          JarEntry copy=new JarEntry(name);copy.setTime(0);out.putNextEntry(copy);out.write(data);out.closeEntry();
        }
      }
      System.out.println("Dev MCP: "+input.getFileName()+" ("+changed+" classes)");
    }
  }
  static String simple(String name){return name.substring(name.lastIndexOf('/')+1);}
}
