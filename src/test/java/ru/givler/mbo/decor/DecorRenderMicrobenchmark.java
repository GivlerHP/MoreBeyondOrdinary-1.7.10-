package ru.givler.mbo.decor;

import java.lang.reflect.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.*;
import software.bernie.geckolib3.file.GeoModelLoader;
import software.bernie.geckolib3.geo.render.built.*;
import ru.givler.mbo.client.render.decormodels.*;
import sun.misc.Unsafe;

/** Hidden-context submission microbenchmark; not an in-game FPS prediction. */
public class DecorRenderMicrobenchmark {
 static int[] ordinary(int count, boolean stairs)throws Exception {
  Field field=Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);Unsafe unsafe=(Unsafe)field.get(null);
  Field loader=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");loader.setAccessible(true);
  loader.set(null,unsafe.allocateInstance(cpw.mods.fml.common.Loader.class));
  Bootstrap.func_151354_b();
  Minecraft.getMinecraft().gameSettings=(GameSettings)unsafe.allocateInstance(GameSettings.class);
  IBlockAccess world=(IBlockAccess)Proxy.newProxyInstance(IBlockAccess.class.getClassLoader(),new Class[]{IBlockAccess.class},(p,m,a)->{
   switch(m.getName()) {
    case "getBlock":return Blocks.air;
    case "getBlockMetadata":return 0;
    case "getLightBrightnessForSkyBlocks":return 0xF000F0;
    case "isAirBlock":return true;
    case "getHeight":return 256;
    case "extendedLevelsInChunkCache":return false;
    case "getTileEntity":return null;
    default:throw new AssertionError(m.getName());
   }
  });
  IIcon icon=(IIcon)Proxy.newProxyInstance(IIcon.class.getClassLoader(),new Class[]{IIcon.class},(p,m,a)->{
   switch(m.getName()) {
    case "getMinU":case "getMinV":return 0F;
    case "getMaxU":case "getMaxV":return 1F;
    case "getInterpolatedU":case "getInterpolatedV":return ((Double)a[0]).floatValue()/16;
    case "getIconWidth":case "getIconHeight":return 32;
    default:return "benchmark";
   }
  });
  Blocks.stone.registerBlockIcons((net.minecraft.client.renderer.texture.IIconRegister)Proxy.newProxyInstance(IIcon.class.getClassLoader(),new Class[]{net.minecraft.client.renderer.texture.IIconRegister.class},(p,m,a)->icon));
  Blocks.cobblestone.registerBlockIcons((net.minecraft.client.renderer.texture.IIconRegister)Proxy.newProxyInstance(IIcon.class.getClassLoader(),new Class[]{net.minecraft.client.renderer.texture.IIconRegister.class},(p,m,a)->icon));
  RenderBlocks blocks=new RenderBlocks(world);blocks.renderAllFaces=true;blocks.setOverrideBlockTexture(icon);
  int[] lists=new int[(count+255)/256];
  for(int group=0;group<lists.length;group++) {
   lists[group]=GLAllocation.generateDisplayLists(1);GL11.glNewList(lists[group],GL11.GL_COMPILE);
   Tessellator.instance.startDrawingQuads();
   for(int i=group*256;i<Math.min(count,(group+1)*256);i++) {
    if(stairs) blocks.renderBlockStairs((net.minecraft.block.BlockStairs)Blocks.stone_stairs,(i%100)*2,0,(i/100)*2);
    else blocks.renderStandardBlock(Blocks.stone,(i%100)*2,0,(i/100)*2);
   }
   Tessellator.instance.draw();GL11.glEndList();
  }
  return lists;
 }
 static double[] time(Runnable draw) {
  for(int i=0;i<3;i++){draw.run();GL11.glFinish();}
  double[] cpu=new double[7],total=new double[7];
  for(int i=0;i<7;i++) {
   GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);GL11.glFinish();
   long start=System.nanoTime();draw.run();cpu[i]=(System.nanoTime()-start)/1e6;
   GL11.glFinish();total[i]=(System.nanoTime()-start)/1e6;
  }
  Arrays.sort(cpu);Arrays.sort(total);return new double[]{cpu[3],total[3]};
 }
 static void models(int count,GeoModel model,ResourceLocation location,boolean cached) {
  for(int i=0;i<count;i++) {
   GL11.glPushMatrix();GL11.glTranslatef((i%100)*2,0,(i/100)*2);
   if(cached)StaticDecorRenderCache.INSTANCE.render(StaticDecorPixelsSmoke.RENDERER,StaticDecorPixelsSmoke.TILE,location,1,1,1,1);
   else StaticDecorPixelsSmoke.RENDERER.render(model,StaticDecorPixelsSmoke.TILE,0,1,1,1,1);
   GL11.glPopMatrix();
  }
 }
 public static void main(String[] args)throws Exception {
  Pbuffer buffer=new Pbuffer(96,96,new PixelFormat(8,24,0),null,null);buffer.makeCurrent();
  StaticDecorPixelsSmoke.fakeMinecraft();StaticDecorPixelsSmoke.texture();
  OpenGlHelper.defaultTexUnit=GL13.GL_TEXTURE0;OpenGlHelper.lightmapTexUnit=GL13.GL_TEXTURE1;
  GL11.glViewport(0,0,96,96);GL11.glEnable(GL11.GL_DEPTH_TEST);
  GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-2,202,-2,8,-110,110);
  GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GL11.glRotatef(45,1,0,0);
  StringBuilder csv=new StringBuilder("model,cubes,count,path,cpu_submit_ms,finish_ms\n");
  for(final int count:new int[]{100,1000,5000}) {
   final int[] lists=ordinary(count,false);
   double[] result=time(()->{for(int id:lists)GL11.glCallList(id);});
   csv.append(String.format(Locale.ROOT,"vanilla_stone,1,%d,chunk_batch,%.6f,%.6f%n",count,result[0],result[1]));
   for(int id:lists)GLAllocation.deleteDisplayLists(id);
   final int[] stairLists=ordinary(count,true);
   result=time(()->{for(int id:stairLists)GL11.glCallList(id);});
   csv.append(String.format(Locale.ROOT,"mbo_stairs_render_path,2,%d,chunk_batch,%.6f,%.6f%n",count,result[0],result[1]));
   for(int id:stairLists)GLAllocation.deleteDisplayLists(id);
   for(String name:new String[]{"geo/decor/rope.geo.json","geo/decor/prison_wagon.geo.json"}) {
    final ResourceLocation location=new ResourceLocation("mbo",name);
    final GeoModel model=GeoModelLoader.getInstance().loadModel(Minecraft.getMinecraft().getResourceManager(),location);
    int cubes=0;List<GeoBone> todo=new ArrayList<>(model.topLevelBones);
    for(int i=0;i<todo.size();i++){cubes+=todo.get(i).childCubes.size();todo.addAll(todo.get(i).childBones);}
    for(final boolean cached:new boolean[]{false,true}) {
     result=time(()->models(count,model,location,cached));
     csv.append(String.format(Locale.ROOT,"%s,%d,%d,%s,%.6f,%.6f%n",name,cubes,count,cached?"cached_vertices":"original_gecko",result[0],result[1]));
    }
   }
   System.out.println("Finished "+count+" objects");
  }
  Path report=Paths.get("build/decor-render-microbenchmark.csv");Files.write(report,csv.toString().getBytes(StandardCharsets.UTF_8));
  System.out.print(csv);StaticDecorRenderCache.INSTANCE.clear();buffer.destroy();
 }
}
