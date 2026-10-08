package ru.givler.mbo.core;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.TesselatorVertexState;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import java.io.PrintWriter;
import java.io.StringWriter;

/** Reproduces the reported empty-buffer crash and checks populated quad sorting is unchanged. */
public final class TessellatorEmptyBufferSmoke {
  public static void main(String[] args) throws Exception {
    String name = "net.minecraft.client.renderer.Tessellator";
    byte[] original;
    try (InputStream input = TessellatorEmptyBufferSmoke.class.getResourceAsStream("/" + name.replace('.', '/') + ".class")) {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];
      for (int read; (read = input.read(buffer)) >= 0;) output.write(buffer, 0, read);
      original = output.toByteArray();
    }
    TessellatorEmptyBufferTransformer transformer = new TessellatorEmptyBufferTransformer();
    byte[] patched = transformer.transform(name, name, original);
    if (!Arrays.equals(patched, transformer.transform(name, name, patched)))
      throw new AssertionError("Empty-buffer patch is not idempotent");
    StringWriter verification = new StringWriter();
    CheckClassAdapter.verify(new ClassReader(patched), false, new PrintWriter(verification));
    if (!verification.toString().isEmpty()) throw new AssertionError(verification.toString());
    Tessellator old = new Tessellator();
    set(old, "rawBuffer", new int[64]);
    try {
      old.getVertexState(0,0,0);
      throw new AssertionError("Original crash was not reproduced");
    } catch (IllegalArgumentException expected) { }
    Class<?> fixedClass = new Loader().define(name, patched);
    Object fixed = fixedClass.newInstance();
    set(fixed, "rawBuffer", new int[64]);
    TesselatorVertexState empty = state(fixed);
    if (empty.getRawBufferIndex()!=0 || empty.getVertexCount()!=0 || empty.getRawBuffer().length!=0)
      throw new AssertionError("Empty batch contains geometry");
    int[] quads = new int[64];
    for (int quad=0;quad<2;quad++) for (int vertex=0;vertex<4;vertex++) {
      int offset = quad*32+vertex*8;
      quads[offset] = Float.floatToIntBits(vertex%2);
      quads[offset+1] = Float.floatToIntBits(vertex/2);
      quads[offset+2] = Float.floatToIntBits(quad==0?1:10);
      quads[offset+3] = 100+quad;
    }
    for (Object tessellator : new Object[]{old,fixed}) {
      set(tessellator,"rawBuffer",quads.clone());
      set(tessellator,"rawBufferIndex",64);
      set(tessellator,"vertexCount",8);
      set(tessellator,"hasTexture",true);
      set(tessellator,"hasColor",true);
    }
    TesselatorVertexState vanilla = old.getVertexState(0,0,0), result = state(fixed);
    if (!Arrays.equals(vanilla.getRawBuffer(),result.getRawBuffer()) || result.getVertexCount()!=8
        || !result.getHasTexture() || !result.getHasColor())
      throw new AssertionError("Populated quad geometry or render flags changed");
    System.out.println("Reported Tessellator crash reproduced; empty batches and vanilla populated sorting passed");
  }
  private static void set(Object object,String field,Object value) throws Exception {
    Field member=object.getClass().getDeclaredField(field);
    member.setAccessible(true);member.set(object,value);
  }
  private static TesselatorVertexState state(Object object) throws Exception {
    return (TesselatorVertexState)object.getClass().getMethod("getVertexState",float.class,float.class,float.class).invoke(object,0F,0F,0F);
  }
  private static final class Loader extends ClassLoader {
    Class<?> define(String name,byte[] bytes) { return defineClass(name,bytes,0,bytes.length); }
  }
}
