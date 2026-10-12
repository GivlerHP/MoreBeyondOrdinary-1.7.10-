package ru.givler.mbo.decor;

import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import org.lwjgl.opengl.*;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.client.render.decormodels.ChunkDecorRenderer;
import sun.misc.Unsafe;

/** Bake real PNG/Geo resources, verify per-facing emission and reversible render routing. */
public class ChunkDecorSmoke {
 static void check(boolean value,String message) { if(!value)throw new AssertionError(message); }
 static void set(Object object,String name,Object value)throws Exception {
  Field f=BlockModels.class.getDeclaredField(name); f.setAccessible(true); f.set(object,value);
 }
 public static void main(String[] args)throws Exception {
  Pbuffer buffer=new Pbuffer(32,32,new PixelFormat(8,24,0),null,null); buffer.makeCurrent();
  StaticDecorPixelsSmoke.fakeMinecraft();
  int visibilityLists=GL11.glGenLists(3);
  net.minecraft.client.renderer.WorldRenderer section=new net.minecraft.client.renderer.WorldRenderer(null,new ArrayList(),-16,16,0,visibilityLists);
  net.minecraft.util.AxisAlignedBB sectionBounds=section.rendererBoundingBox;
  check(sectionBounds.minX==-22 && sectionBounds.maxX==6 && sectionBounds.minY==10 && sectionBounds.maxY==38
      && sectionBounds.minZ==-6 && sectionBounds.maxZ==22,"actual vanilla frustum/occlusion margin differs from routing assumption");
  GL11.glDeleteLists(visibilityLists,3);
  Field directory=Minecraft.class.getDeclaredField("mcDataDir");directory.setAccessible(true);
  directory.set(Minecraft.getMinecraft(),new java.io.File("build/chunk-validation"));
  net.minecraft.client.renderer.OpenGlHelper.defaultTexUnit=GL13.GL_TEXTURE0;
  net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit=GL13.GL_TEXTURE1;
  Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);Unsafe unsafe=(Unsafe)f.get(null);
  f=BlockModels.class.getDeclaredField("ALL_MODELS");f.setAccessible(true);
  List<BlockModels> models=(List<BlockModels>)f.get(null);models.clear();
  try(java.util.stream.Stream<Path> files=Files.list(Paths.get("src/main/resources/assets/mbo/geo"))) {
   files.filter(p->p.toString().endsWith(".geo.json")).forEach(p->{
    try {
     String name=p.getFileName().toString().replace(".geo.json","");
     Path png=Paths.get("src/main/resources/assets/mbo/textures/models/decor/"+name+".png");
     if(!Files.isRegularFile(png))return;
     BlockModels block=(BlockModels)unsafe.allocateInstance(BlockModels.class);
     set(block,"modelName","instruments_of_torture/"+name);set(block,"textureName","instruments_of_torture/"+name);
     models.add(block);
    } catch(Exception e) {throw new RuntimeException(e);}
   });
  }
  TextureMap atlas=(TextureMap)unsafe.allocateInstance(TextureMap.class);
  Field registered=TextureMap.class.getDeclaredField("mapRegisteredSprites");registered.setAccessible(true);
  registered.set(atlas,new HashMap<>());
  ChunkDecorRenderer renderer=ChunkDecorRenderer.INSTANCE;
  renderer.stitch(new TextureStitchEvent.Pre(atlas));
  BlockModels.decorChunkRenderId=1234;BlockModels.decorChunkRendering=true;
  int eligible=0;
  int emitted=0,boundaries=0,boundaryEmissions=0;
  BlockModels benchmarkBlock=null;
  Field raw=Tessellator.class.getDeclaredField("rawBuffer");raw.setAccessible(true);
  Field index=Tessellator.class.getDeclaredField("rawBufferIndex");index.setAccessible(true);
  for(BlockModels block:models) {
   if(!block.usesDecorChunkRendering())continue;
   eligible++;
   if(benchmarkBlock==null)benchmarkBlock=block;
   System.out.println("Chunk eligible: "+block.getModelName());
   check(block.getRenderType()==1234,"chunk route");
   BlockModels.decorInstancedRendering=true;
   check(block.getRenderType()==-1 && block.isDecorChunkReady(),"GPU overrides chunk routing without losing baked mesh");
   BlockModels.decorInstancedRendering=false;
   check(block.getRenderType()==1234,"GPU off restores previous chunk route");
   for(final int meta:new int[]{0,1,2,3}) {
    IBlockAccess world=(IBlockAccess)Proxy.newProxyInstance(IBlockAccess.class.getClassLoader(),new Class[]{IBlockAccess.class},(p,m,a)->{
     if(m.getName().equals("getBlockMetadata"))return meta;
     if(m.getName().equals("getLightBrightnessForSkyBlocks"))return 0x00F000E0;
     throw new AssertionError(m.getName());
    });
    Tessellator.instance.startDrawingQuads();
    boolean fits=renderer.fits(block,8,24,8,meta);
    check(renderer.renderWorldBlock(world,8,24,8,block,1234,null)==fits,"emit routing");
    int[] data=(int[])raw.get(Tessellator.instance);int length=index.getInt(Tessellator.instance);
    check(fits ? length>0 && length%64==0 : length==0,"both windings or TESR fallback");
    if(fits)emitted++;
    if(fits&&block.getModelName().endsWith("/hourglass")) {
     double minX=Double.POSITIVE_INFINITY,maxX=Double.NEGATIVE_INFINITY,minZ=minX,maxZ=maxX;
     for(int i=0;i<length;i+=8){double px=Float.intBitsToFloat(data[i]),pz=Float.intBitsToFloat(data[i+2]);minX=Math.min(minX,px);maxX=Math.max(maxX,px);minZ=Math.min(minZ,pz);maxZ=Math.max(maxZ,pz);}
     check(Math.abs((minX+maxX)/2-8.5)<.0001&&Math.abs((minZ+maxZ)/2-8.5)<.0001,"hourglass must be centred on the placement block for facing "+meta);
    }
    for(int i=0;i<length;i+=8) {
     float x=Float.intBitsToFloat(data[i]),y=Float.intBitsToFloat(data[i+1]),z=Float.intBitsToFloat(data[i+2]);
     check(x>=-6 && x<=22 && y>=10 && y<=38 && z>=-6 && z<=22,"vanilla section visibility containment");
     check(data[i+7]==0x00F000E0,"lightmap");
    }
    Tessellator.instance.draw();
    Tessellator.instance.startDrawingQuads();
    fits=renderer.fits(block,-16,31,15,meta);
    check(renderer.renderWorldBlock(world,-16,31,15,block,1234,null)==fits,"negative/chunk boundary routing");
    if(!fits) {check(index.getInt(Tessellator.instance)==0,"oversized geometry keeps TESR");boundaries++;}
    else {check(index.getInt(Tessellator.instance)>0,"boundary geometry missing");boundaryEmissions++;}
    Tessellator.instance.draw();
   }
   block.withDynamicModelRender();check(block.getRenderType()==-1,"existing dynamic fallback");
  }
  check(eligible>0,"no eligible models");
  check(emitted>0 && boundaryEmissions>0,"interior and cross-boundary emissions exercised");
  Method boundsCheck=ChunkDecorRenderer.class.getDeclaredMethod("fitsBounds",double[].class,int.class,int.class,int.class);boundsCheck.setAccessible(true);
  check((Boolean)boundsCheck.invoke(null,new double[]{-6,-6,-6,7,7,7},0,0,0),"visibility margin included");
  check(!(Boolean)boundsCheck.invoke(null,new double[]{-6.01,0,0,1,1,1},0,0,0),"outside visibility margin must keep TESR");
  System.out.println("Interior emissions="+emitted+", boundary chunk emissions="+boundaryEmissions+", oversized TESR fallbacks="+boundaries);
  final BlockModels measured=benchmarkBlock;
  set(measured,"dynamicModelRender",false);
  IBlockAccess benchWorld=(IBlockAccess)Proxy.newProxyInstance(IBlockAccess.class.getClassLoader(),new Class[]{IBlockAccess.class},(p,m,a)->{
   if(m.getName().equals("getBlockMetadata"))return 0;
   if(m.getName().equals("getLightBrightnessForSkyBlocks"))return 0x00F000F0;
   throw new AssertionError(m.getName());
  });
  final int[] lists=new int[4];
  for(int group=0;group<4;group++) {
   lists[group]=GL11.glGenLists(1);GL11.glNewList(lists[group],GL11.GL_COMPILE);
   Tessellator.instance.startDrawingQuads();
   for(int i=group*256;i<Math.min(1000,(group+1)*256);i++)
    renderer.renderWorldBlock(benchWorld,(i%100)*2,0,(i/100)*2,measured,1234,null);
   Tessellator.instance.draw();GL11.glEndList();
  }
  ResourceLocation location=ru.givler.mbo.client.model.decormodels.DecorResources.model(measured.getModelName());
  software.bernie.geckolib3.geo.render.built.GeoModel model=software.bernie.geckolib3.file.GeoModelLoader.getInstance()
   .loadModel(Minecraft.getMinecraft().getResourceManager(),location);
  StaticDecorPixelsSmoke.texture();
  double[] chunk=DecorRenderMicrobenchmark.time(()->{for(int id:lists)GL11.glCallList(id);});
  double[] cached=DecorRenderMicrobenchmark.time(()->DecorRenderMicrobenchmark.models(1000,model,location,true));
  System.out.printf(Locale.ROOT,"1000 %s: chunk CPU %.6f ms, cached TESR geometry CPU %.6f ms%n",measured.getModelName(),chunk[0],cached[0]);
  for(int id:lists)GL11.glDeleteLists(id,1);
  renderer.setEnabled(false);
  for(BlockModels block:models)check(block.getRenderType()==-1,"off restores TESR");
  System.out.println(renderer.statistics());
  System.out.println("PASS chunk baking, four facings, lightmap, double-sided emission, dynamic/off routing");
  buffer.destroy();
 }
}
