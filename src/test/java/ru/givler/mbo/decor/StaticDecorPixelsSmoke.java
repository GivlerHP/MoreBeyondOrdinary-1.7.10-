package ru.givler.mbo.decor;

import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import ru.givler.mbo.client.render.decormodels.*;
import ru.givler.mbo.tileentity.ModelTileBase;
import software.bernie.geckolib3.file.GeoModelLoader;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import sun.misc.Unsafe;

public class StaticDecorPixelsSmoke {
 static final int SIZE=96;
 static final Path ASSETS=Paths.get("src/main/resources/assets/mbo");
 static final TemplateModelRenderer RENDERER=new TemplateModelRenderer();
 static final ModelTileBase TILE=new ModelTileBase("test","test");
 static void fakeMinecraft()throws Exception {
  IReloadableResourceManager resources=(IReloadableResourceManager)Proxy.newProxyInstance(
   IResourceManager.class.getClassLoader(),new Class[]{IReloadableResourceManager.class},(p,m,a)-> {
    if(m.getName().equals("getResource")) {
     Path file=ASSETS.resolve(((ResourceLocation)a[0]).getResourcePath());
     return Proxy.newProxyInstance(IResource.class.getClassLoader(),new Class[]{IResource.class},(rp,rm,ra)->{
      if(rm.getName().equals("getInputStream"))return Files.newInputStream(file);
      if(rm.getName().equals("hasMetadata"))return false;
      return null;
     });
    }
    if(m.getName().equals("getResourceDomains"))return Collections.singleton("mbo");
    return null;
   });
  Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);Unsafe u=(Unsafe)f.get(null);
  Minecraft mc=(Minecraft)u.allocateInstance(Minecraft.class);
  f=Minecraft.class.getDeclaredField("theMinecraft");f.setAccessible(true);f.set(null,mc);
  f=Minecraft.class.getDeclaredField("mcResourceManager");f.setAccessible(true);f.set(mc,resources);
 }
 static void texture() {
  int id=GL11.glGenTextures();GL11.glBindTexture(GL11.GL_TEXTURE_2D,id);
  ByteBuffer pixels=BufferUtils.createByteBuffer(32*32*4);
  for(int y=0;y<32;y++)for(int x=0;x<32;x++) {
   pixels.put((byte)(x*8)).put((byte)(y*8)).put((byte)180).put((byte)((x+y)%7==0?0:(x+y)%3==0?140:255));
  }
  pixels.flip();GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,32,32,0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
  GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);
  GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
  GL11.glEnable(GL11.GL_TEXTURE_2D);
  GL13.glActiveTexture(GL13.GL_TEXTURE1);
  int lightmap=GL11.glGenTextures();GL11.glBindTexture(GL11.GL_TEXTURE_2D,lightmap);
  ByteBuffer light=BufferUtils.createByteBuffer(16*16*4);
  for(int y=0;y<16;y++)for(int x=0;x<16;x++)light.put((byte)(x*17)).put((byte)(y*17)).put((byte)200).put((byte)255);
  light.flip();GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,16,16,0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,light);
  GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);
  GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
  GL11.glEnable(GL11.GL_TEXTURE_2D);GL13.glActiveTexture(GL13.GL_TEXTURE0);
 }
 static byte[] picture(GeoModel model,ResourceLocation location,int turn,float light,boolean cached) {
  GL11.glViewport(0,0,SIZE,SIZE);GL11.glClearColor(.2F,.3F,.4F,1);
  GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
  GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glEnable(GL11.GL_ALPHA_TEST);GL11.glAlphaFunc(GL11.GL_GREATER,.1F);
  GL11.glEnable(GL11.GL_LIGHTING);GL11.glEnable(GL11.GL_LIGHT0);GL11.glEnable(GL11.GL_COLOR_MATERIAL);
  GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK,GL11.GL_AMBIENT_AND_DIFFUSE);
  FloatBuffer ambient=BufferUtils.createFloatBuffer(4);ambient.put(new float[]{light,light,light,1}).flip();
  GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT,ambient);
  GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-4,4,-2,6,-16,16);
  GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GL11.glRotatef(20,1,0,0);GL11.glRotatef(turn*90,0,1,0);
  GL13.glMultiTexCoord2f(GL13.GL_TEXTURE1,light,light);
  if(cached)StaticDecorRenderCache.INSTANCE.render(RENDERER,TILE,location,1,1,1,1);
  else RENDERER.render(model,TILE,0,1,1,1,1);
  ByteBuffer pixels=BufferUtils.createByteBuffer(SIZE*SIZE*4);
  GL11.glReadPixels(0,0,SIZE,SIZE,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
  byte[] result=new byte[pixels.remaining()];pixels.get(result);return result;
 }
 public static void main(String[] args)throws Exception {
  Pbuffer buffer=new Pbuffer(SIZE,SIZE,new PixelFormat(8,24,0),null,null);
  buffer.makeCurrent();fakeMinecraft();texture();
  System.out.println("Hidden OpenGL: "+GL11.glGetString(GL11.GL_RENDERER));
  List<Path> paths=new ArrayList<>();Files.walk(ASSETS.resolve("geo")).filter(p->p.toString().endsWith(".geo.json")).forEach(paths::add);
  int pictures=0;
  for(Path path:paths) {
   ResourceLocation location=new ResourceLocation("mbo",ASSETS.relativize(path).toString().replace('\\','/'));
   GeoModel model=GeoModelLoader.getInstance().loadModel(Minecraft.getMinecraft().getResourceManager(),location);
   for(int turn=0;turn<4;turn++)for(float light:new float[]{.1F,.8F}) {
    byte[] expected=picture(model,location,turn,light,false);
    byte[] actual=picture(model,location,turn,light,true);
    if(!Arrays.equals(expected,actual)) {
     int different=0;for(int i=0;i<expected.length;i++)if(expected[i]!=actual[i])different++;
     throw new AssertionError("Pixel mismatch "+location+" rotation="+turn+" light="+light+" channels="+different);
    }
    pictures++;
   }
   if(pictures%400==0)System.out.println("Compared "+pictures+" images");
  }
  StaticDecorRenderCache.INSTANCE.clear();
  System.out.println("Exact pixels match: "+paths.size()+" models, "+pictures+" images, 4 rotations, 2 live lighting levels, alpha texture.");
  buffer.destroy();
 }
}
