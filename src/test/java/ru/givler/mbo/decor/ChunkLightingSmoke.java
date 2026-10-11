package ru.givler.mbo.decor;

import java.nio.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import ru.givler.mbo.client.render.decormodels.DecorLighting;
import software.bernie.geckolib3.file.GeoModelLoader;
import software.bernie.geckolib3.geo.render.built.*;

/** Compare baked directional lighting against actual OpenGL RenderHelper lights. */
public class ChunkLightingSmoke {
 static byte[] picture(int packed,int meta,int view,boolean baked) {
  GL11.glClearColor(0,0,0,1);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
  GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_BLEND);GL11.glDisable(GL11.GL_CULL_FACE);
  GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-2,2,-2,2,-8,8);
  GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();
  GL11.glRotatef(view*37,0,1,0);GL11.glRotatef(view*13,1,0,0);
  RenderHelper.enableStandardItemLighting();
  GL11.glEnable(GL12.GL_RESCALE_NORMAL);
  if(baked)RenderHelper.disableStandardItemLighting();
  int yaw=new int[]{180,90,0,270}[meta];GL11.glRotatef(yaw,0,1,0);
  float shade=baked?DecorLighting.shade(packed,meta)/255F:1;
  GL11.glColor4f(shade,shade,shade,1);
  GL11.glNormal3b((byte)packed,(byte)(packed>>8),(byte)(packed>>16));
  GL11.glBegin(GL11.GL_QUADS);
  GL11.glVertex3f(-1,-1,0);GL11.glVertex3f(1,-1,0);GL11.glVertex3f(1,1,0);GL11.glVertex3f(-1,1,0);
  GL11.glEnd();
  ByteBuffer pixels=BufferUtils.createByteBuffer(32*32*4);
  GL11.glReadPixels(0,0,32,32,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
  byte[] result=new byte[pixels.remaining()];pixels.get(result);return result;
 }
 public static void main(String[] args)throws Exception {
  Pbuffer buffer=new Pbuffer(32,32,new PixelFormat(8,24,0),null,null);buffer.makeCurrent();
  GL11.glViewport(0,0,32,32);StaticDecorPixelsSmoke.fakeMinecraft();
  Set<Integer> normals=new HashSet<>();
  Field raw=Tessellator.class.getDeclaredField("rawBuffer");raw.setAccessible(true);
  Field index=Tessellator.class.getDeclaredField("rawBufferIndex");index.setAccessible(true);
  List<Path> files=new ArrayList<>();
  try(java.util.stream.Stream<Path> stream=Files.walk(StaticDecorPixelsSmoke.ASSETS.resolve("geo"))) {
   stream.filter(p->p.toString().endsWith(".geo.json")).forEach(files::add);
  }
  for(Path file:files) {
   ResourceLocation location=new ResourceLocation("mbo",StaticDecorPixelsSmoke.ASSETS.relativize(file).toString().replace('\\','/'));
   GeoModel model=GeoModelLoader.getInstance().loadModel(Minecraft.getMinecraft().getResourceManager(),location);
   Tessellator t=new Tessellator();t.startDrawingQuads();
   for(GeoBone bone:model.topLevelBones)StaticDecorPixelsSmoke.RENDERER.renderRecursively(t,StaticDecorPixelsSmoke.TILE,bone,1,1,1,1);
   int[] data=(int[])raw.get(t);
   for(int i=0;i<index.getInt(t);i+=8)normals.add(data[i+6]);
  }
  int max=0,pictures=0;
  for(int normal:normals)for(int meta=0;meta<4;meta++)for(int view=0;view<3;view++) {
   byte[] expected=picture(normal,meta,view,false),actual=picture(normal,meta,view,true);
   for(int i=0;i<expected.length;i++)max=Math.max(max,Math.abs((expected[i]&255)-(actual[i]&255)));
   pictures++;
  }
  System.out.println("TESR/chunk lighting: "+files.size()+" models, "+normals.size()+" distinct normals, "+pictures+" comparisons; max channel difference="+max);
  if(max>1)throw new AssertionError("Lighting mismatch exceeds 8-bit vertex-color rounding: "+max);
  System.out.println("PASS actual RenderHelper lighting vs baked colors, four facings, three camera angles");
  buffer.destroy();
 }
}
