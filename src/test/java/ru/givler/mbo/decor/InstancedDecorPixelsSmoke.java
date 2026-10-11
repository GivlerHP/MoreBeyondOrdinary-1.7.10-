package ru.givler.mbo.decor;

import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.util.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.client.render.decormodels.*;
import software.bernie.geckolib3.file.GeoModelLoader;
import software.bernie.geckolib3.geo.render.built.*;
import sun.misc.Unsafe;

/** Real GPU shader/instancing test, independent of the game launch and world saves. */
public class InstancedDecorPixelsSmoke {
 static Method useProgram,draw;static Constructor<?> meshConstructor,gpuConstructor;
 static final InstancedDecorRenderer GPU=InstancedDecorRenderer.INSTANCE;
 static final int SIZE=64;
 static class TestWorld extends net.minecraft.client.multiplayer.WorldClient {
  BlockModels block;
  TestWorld(){super(null,null,0,net.minecraft.world.EnumDifficulty.NORMAL,null);}
  public net.minecraft.block.Block getBlock(int x,int y,int z){return block;}
  public int getBlockMetadata(int x,int y,int z){return 2;}
  public int getLightBrightnessForSkyBlocks(int x,int y,int z,int light){return 0x00F000F0;}
 }
 static class TestRenderer extends EntityRenderer {
  TestRenderer(){super(null,null);}
  public void enableLightmap(double partialTicks) {}
 }
 static class TestTextures extends net.minecraft.client.renderer.texture.TextureManager {
  int texture;
  TestTextures(){super(null);}
  public void bindTexture(ResourceLocation location){GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);}
 }
 static void lifecycle(Object mesh,BlockModels block,Unsafe unsafe)throws Exception {
  Field prepared=ChunkDecorRenderer.class.getDeclaredField("meshes");prepared.setAccessible(true);
  Map map=(Map)prepared.get(ChunkDecorRenderer.INSTANCE);map.put(block,mesh);block.setDecorChunkReady(true);
  TestWorld world=(TestWorld)unsafe.allocateInstance(TestWorld.class);world.block=block;
  world.loadedTileEntityList=new ArrayList();
  for(int x:new int[]{15,16}) {
   ru.givler.mbo.tileentity.ModelTileBase tile=new ru.givler.mbo.tileentity.ModelTileBase();
   tile.xCoord=x;tile.yCoord=8;tile.zCoord=8;tile.setWorldObj(world);world.loadedTileEntityList.add(tile);
   if(!GPU.renders(tile))throw new AssertionError("Boundary model did not choose GPU renderer");
  }
  if(!ChunkDecorRenderer.INSTANCE.fits(block,15,8,8,2))throw new AssertionError("Cross-boundary fixture should fit vanilla visibility margin");
  Minecraft mc=Minecraft.getMinecraft();mc.theWorld=world;
  mc.renderGlobal=(RenderGlobal)unsafe.allocateInstance(RenderGlobal.class);
  mc.renderGlobal.tileEntities=new ArrayList(world.loadedTileEntityList);
  // Match the real static-decor lifecycle: non-ticking tiles only live in the render list.
  world.loadedTileEntityList.clear();
  mc.renderViewEntity=(net.minecraft.entity.EntityLivingBase)unsafe.allocateInstance(net.minecraft.entity.passive.EntityPig.class);
  mc.entityRenderer=(TestRenderer)unsafe.allocateInstance(TestRenderer.class);
  TestTextures textures=new TestTextures();textures.texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);mc.renderEngine=textures;
  GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-64,64,-64,64,-64,64);
  GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();
  GL11.glEnable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_BLEND);GL11.glDepthMask(false);
  GL13.glActiveTexture(GL13.GL_TEXTURE1);GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
  GPU.renderWorld(new net.minecraftforge.client.event.RenderWorldLastEvent(null,0));
  if(!GPU.statistics().contains("visible=2, draws=1"))throw new AssertionError(GPU.statistics());
  if(!GL11.glIsEnabled(GL11.GL_CULL_FACE)||GL11.glIsEnabled(GL11.GL_BLEND)||GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK)
    ||GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE)!=GL13.GL_TEXTURE1||GL11.glGetInteger(GL13.GL_CLIENT_ACTIVE_TEXTURE)!=GL13.GL_TEXTURE1)
   throw new AssertionError("World renderer leaked OpenGL state");
  GPU.unload(new net.minecraftforge.event.world.WorldEvent.Unload(world));
  if(!GPU.statistics().contains("meshes=0, geometry=0 bytes"))throw new AssertionError("World unload didn't release GPU meshes");
  GPU.onResourceManagerReload(mc.getResourceManager());
  if(!GPU.renders((ru.givler.mbo.tileentity.ModelTileBase)mc.renderGlobal.tileEntities.get(0)))throw new AssertionError("Resource reload lost rendering");
  block.withDynamicModelRender();
  if(GPU.renders((ru.givler.mbo.tileentity.ModelTileBase)mc.renderGlobal.tileEntities.get(0)))throw new AssertionError("Dynamic registry flag didn't restore TESR");
  mc.theWorld=null;mc.renderGlobal=null;GL13.glActiveTexture(GL13.GL_TEXTURE0);GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
  GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDepthMask(true);
  System.out.println("PASS cross-chunk routing, two instances/one mesh draw, GL state restoration, unload/reload and dynamic fallback");
 }
 static void texture() {
  StaticDecorPixelsSmoke.texture();
  // Binary alpha, as admitted by the experimental renderer.
  ByteBuffer pixels=BufferUtils.createByteBuffer(32*32*4);
  for(int y=0;y<32;y++)for(int x=0;x<32;x++)pixels.put((byte)(x*8)).put((byte)(y*8)).put((byte)180).put((byte)((x+y)%7==0?0:255));
  pixels.flip();GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,32,32,0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
  GL13.glActiveTexture(GL13.GL_TEXTURE1);GL11.glMatrixMode(GL11.GL_TEXTURE);GL11.glLoadIdentity();
  GL11.glScalef(1/256F,1/256F,1/256F);GL11.glTranslatef(8,8,8);
  GL11.glMatrixMode(GL11.GL_MODELVIEW);GL13.glActiveTexture(GL13.GL_TEXTURE0);
 }
 static byte[] picture(GeoModel model,Object gpu,int meta,int light,boolean fog,boolean instanced)throws Exception {
  GL11.glViewport(0,0,SIZE,SIZE);GL11.glClearColor(.2F,.3F,.4F,1);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
  GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glEnable(GL11.GL_ALPHA_TEST);GL11.glAlphaFunc(GL11.GL_GREATER,.1F);
  GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glDisable(GL11.GL_CULL_FACE);
  GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-4,4,-2,6,-16,16);
  GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GL11.glTranslatef(0,0,-6);GL11.glRotatef(20,1,0,0);GL11.glRotatef(23,0,1,0);
  RenderHelper.enableStandardItemLighting();GL11.glEnable(GL12.GL_RESCALE_NORMAL);
  if(fog)GL11.glEnable(GL11.GL_FOG);else GL11.glDisable(GL11.GL_FOG);
  GL11.glFogi(GL11.GL_FOG_MODE,GL11.GL_LINEAR);GL11.glFogf(GL11.GL_FOG_START,3);GL11.glFogf(GL11.GL_FOG_END,12);
  FloatBuffer fogColor=BufferUtils.createFloatBuffer(4);fogColor.put(new float[]{.3F,.5F,.7F,1}).flip();GL11.glFog(GL11.GL_FOG_COLOR,fogColor);
  if(GLContext.getCapabilities().GL_NV_fog_distance)GL11.glFogi(0x855A,0x855C); // GL_EYE_PLANE_ABSOLUTE_NV
  GL13.glMultiTexCoord2f(GL13.GL_TEXTURE1,light,light);
  if(instanced) {
   useProgram.invoke(GPU);
   FloatBuffer data=BufferUtils.createFloatBuffer(6);data.put(new float[]{.5F,.01F,.5F,meta,light,light}).flip();
   draw.invoke(GPU,gpu,data,1);GL20.glUseProgram(0);
   GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
   GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,0);
  } else {
   GL11.glPushMatrix();GL11.glTranslatef(.5F,.01F,.5F);GL11.glRotatef(new int[]{180,90,0,270}[meta],0,1,0);
   StaticDecorPixelsSmoke.RENDERER.render(model,StaticDecorPixelsSmoke.TILE,0,1,1,1,1);GL11.glPopMatrix();
  }
  ByteBuffer pixels=BufferUtils.createByteBuffer(SIZE*SIZE*4);GL11.glReadPixels(0,0,SIZE,SIZE,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
  byte[] result=new byte[pixels.remaining()];pixels.get(result);return result;
 }
 public static void main(String[] args)throws Exception {
  Pbuffer context=new Pbuffer(SIZE,SIZE,new PixelFormat(8,24,0),null,null);context.makeCurrent();
  StaticDecorPixelsSmoke.fakeMinecraft();OpenGlHelper.defaultTexUnit=GL13.GL_TEXTURE0;OpenGlHelper.lightmapTexUnit=GL13.GL_TEXTURE1;
  System.out.println(GL11.glGetString(GL11.GL_RENDERER)+" / "+GL11.glGetString(GL11.GL_VERSION));
  System.out.println(GPU.setEnabled(true));
  if(!BlockModels.decorInstancedRendering)throw new AssertionError("GPU initialization failed");
  texture();
  Class<?> meshClass=Class.forName("ru.givler.mbo.client.render.decormodels.ChunkDecorRenderer$Mesh");
  Class<?> gpuClass=Class.forName("ru.givler.mbo.client.render.decormodels.InstancedDecorRenderer$GpuMesh");
  meshConstructor=meshClass.getDeclaredConstructor(int[].class,IIcon.class,BlockModels.class);meshConstructor.setAccessible(true);
  gpuConstructor=gpuClass.getDeclaredConstructor(meshClass,BlockModels.class);gpuConstructor.setAccessible(true);
  useProgram=GPU.getClass().getDeclaredMethod("useProgram");useProgram.setAccessible(true);
  draw=GPU.getClass().getDeclaredMethod("draw",gpuClass,FloatBuffer.class,int.class);draw.setAccessible(true);
  Field raw=Tessellator.class.getDeclaredField("rawBuffer");raw.setAccessible(true);
  Field index=Tessellator.class.getDeclaredField("rawBufferIndex");index.setAccessible(true);
  Field access=Unsafe.class.getDeclaredField("theUnsafe");access.setAccessible(true);Unsafe unsafe=(Unsafe)access.get(null);
  IIcon icon=(IIcon)Proxy.newProxyInstance(IIcon.class.getClassLoader(),new Class[]{IIcon.class},(p,m,a)->"mbo:models/decor/test");
  int pictures=0,max=0;long channels=0,error=0,largeDifferences=0;
  Object benchmark=null;
  Object lifecycleMesh=null;BlockModels lifecycleBlock=null;
  for(String name:new String[]{"rope","prison_wagon","torture_table","witch_s_chair","tools_for_oversupply"}) {
   ResourceLocation location=new ResourceLocation("mbo","geo/decor/"+name+".geo.json");
   GeoModel model=GeoModelLoader.getInstance().loadModel(Minecraft.getMinecraft().getResourceManager(),location);
   Tessellator t=new Tessellator();t.startDrawingQuads();
   for(GeoBone bone:model.topLevelBones)StaticDecorPixelsSmoke.RENDERER.renderRecursively(t,StaticDecorPixelsSmoke.TILE,bone,1,1,1,1);
   int[] data=Arrays.copyOf((int[])raw.get(t),index.getInt(t));
   BlockModels block=(BlockModels)unsafe.allocateInstance(BlockModels.class);ChunkDecorSmoke.set(block,"modelName",name);
   Object mesh=meshConstructor.newInstance(data,icon,block);Object gpu=gpuConstructor.newInstance(mesh,block);
   if(name.equals("prison_wagon")){lifecycleMesh=mesh;lifecycleBlock=block;}
   GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,0);
   for(int meta=0;meta<4;meta++)for(int light:new int[]{32,240})for(boolean fog:new boolean[]{false,true}) {
    byte[] expected=picture(model,gpu,meta,light,fog,false),actual=picture(model,gpu,meta,light,fog,true);
    int localMax=0;long localError=0;int localLarge=0;
    for(int i=0;i<expected.length;i++) {
     int difference=Math.abs((expected[i]&255)-(actual[i]&255));localMax=Math.max(localMax,difference);localError+=difference;
     if(difference>2)localLarge++;
    }
    max=Math.max(max,localMax);error+=localError;channels+=expected.length;largeDifferences+=localLarge;pictures++;
    if(localLarge>32)throw new AssertionError("GPU pixels differ: "+name+" facing="+meta+" light="+light+" fog="+fog+" channels>2="+localLarge+" max="+localMax);
   }
   if(benchmark==null)benchmark=gpu;
   else {Field vbo=gpuClass.getDeclaredField("vbo");vbo.setAccessible(true);GL15.glDeleteBuffers(vbo.getInt(gpu));}
  }
  System.out.println("GPU/TESR: "+pictures+" images, max channel difference="+max+", mean channel error="+(error/(double)channels)+", channels differing >2="+largeDifferences);
  final Object batch=benchmark;
  FloatBuffer data=BufferUtils.createFloatBuffer(1000*6);for(int i=0;i<1000;i++)data.put(new float[]{(i%100)*2,0,(i/100)*2,2,240,240});data.flip();
  GL11.glDisable(GL11.GL_FOG);useProgram.invoke(GPU);
  double[] timing=DecorRenderMicrobenchmark.time(()->{try{draw.invoke(GPU,batch,data,1000);}catch(Exception e){throw new RuntimeException(e);}});
  System.out.printf(Locale.ROOT,"1000 instances, one draw: CPU %.6f ms, finish %.6f ms%n",timing[0],timing[1]);
  GL20.glUseProgram(0);Field vbo=gpuClass.getDeclaredField("vbo");vbo.setAccessible(true);GL15.glDeleteBuffers(vbo.getInt(benchmark));
  lifecycle(lifecycleMesh,lifecycleBlock,unsafe);
  int glError=GL11.glGetError();if(glError!=GL11.GL_NO_ERROR)throw new AssertionError("OpenGL error "+glError);
  GPU.setEnabled(false);checkOff();context.destroy();
  System.out.println("PASS instanced shader, fixed TESR lighting/lightmap, fog, rotations, single-draw batching and disable cleanup");
 }
 static void checkOff(){if(BlockModels.decorInstancedRendering)throw new AssertionError("GPU mode didn't turn off");}
}
