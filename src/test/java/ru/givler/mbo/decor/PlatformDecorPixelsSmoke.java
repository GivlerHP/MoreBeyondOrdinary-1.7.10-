package ru.givler.mbo.decor;

import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import ru.givler.mbo.block.model.BlockDecorCage;
import ru.givler.mbo.client.render.RenderMovingPlatform;
import ru.givler.mbo.client.render.decormodels.TemplateModelRenderer;
import ru.givler.mbo.movingplatform.*;
import software.bernie.geckolib3.resource.GeckoLibCache;
import software.bernie.geckolib3.file.*;
import sun.misc.Unsafe;

/** Actual moving-platform TESR path must draw both static decor and animated cage geometry. */
public final class PlatformDecorPixelsSmoke {
 static class TestWorld extends CollisionHelperLifecycleSmoke.TestWorld {
  int lightQueries;
  public int getLightBrightnessForSkyBlocks(int x,int y,int z,int minimum){lightQueries++;return y>=2?0x00f000f0:0;}
  public long getTotalWorldTime(){return 20;}
 }
 static class Textures extends TextureManager {
  Textures(){super(null);}
  public void bindTexture(ResourceLocation location){}
 }
 static class RenderTile extends ru.givler.mbo.tileentity.ModelTileBase {
  // Animation is verified separately against Gecko's controller; this harness has no Forge access transformers.
  public boolean isAnimated(){return false;}
 }
 static void set(Object o,Class<?> type,String name,Object value)throws Exception{Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 public static void main(String[] args)throws Exception {
  Pbuffer buffer=new Pbuffer(128,128,new PixelFormat(8,24,0),null,null);buffer.makeCurrent();
  try {
   StaticDecorPixelsSmoke.fakeMinecraft();StaticDecorPixelsSmoke.texture();Minecraft.getMinecraft().renderEngine=new Textures();
   Field access=Unsafe.class.getDeclaredField("theUnsafe");access.setAccessible(true);Unsafe u=(Unsafe)access.get(null);
   Minecraft.getMinecraft().gameSettings=(net.minecraft.client.settings.GameSettings)u.allocateInstance(net.minecraft.client.settings.GameSettings.class);
   TestWorld world=(TestWorld)u.allocateInstance(TestWorld.class);set(world,net.minecraft.world.World.class,"provider",new net.minecraft.world.WorldProviderSurface());
   BlockDecorCage cage=new BlockDecorCage();world.owner=cage;
   RenderMovingPlatform renderer=(RenderMovingPlatform)u.allocateInstance(RenderMovingPlatform.class);
   set(renderer,RenderMovingPlatform.class,"decorRenderer",new TemplateModelRenderer(){protected boolean useSharedGeometry(){return true;}});
   set(renderer,RenderMovingPlatform.class,"decorTiles",new WeakHashMap<>());
   Method render=RenderMovingPlatform.class.getDeclaredMethod("renderDecor",EntityMovingPlatform.class,float.class);render.setAccessible(true);
   EntityMovingPlatform platform=new EntityMovingPlatform(world);
   software.bernie.geckolib3.GeckoLib.hasInitialized=true;
   for(String name:new String[]{"iron_cage","bench"}) {
    ResourceLocation geometry=new ResourceLocation("mbo","geo/"+name+".geo.json");
    GeckoLibCache.getInstance().getGeoModels().put(geometry,GeoModelLoader.getInstance().loadModel(Minecraft.getMinecraft().getResourceManager(),geometry));
    if(name.equals("iron_cage")) {
     ResourceLocation clips=new ResourceLocation("mbo","animations/instruments_of_torture/iron_cage.animation.json");
     GeckoLibCache.getInstance().getAnimations().put(clips,AnimationFileLoader.getInstance().loadAllAnimations(software.bernie.geckolib3.molang.MolangRegistrar.getParser(),clips,Minecraft.getMinecraft().getResourceManager()));
    }
    PlatformBlock b=new PlatformBlock(0,0,0,name.equals("iron_cage")?cage:new ru.givler.mbo.block.BlockModels(net.minecraft.block.material.Material.wood,"platform_test_bench","instruments_of_torture/bench","instruments_of_torture/bench"),0);
    b.tileData=new NBTTagCompound();b.tileData.setString("model","instruments_of_torture/"+name);b.tileData.setString("texture","instruments_of_torture/"+name);b.tileData.setInteger("frameCount",1);
    platform.getBlocks().clear();platform.getBlocks().add(b);
    for(int facing=0;facing<4;facing++) {
     b.meta=facing;
     ru.givler.mbo.tileentity.ModelTileBase renderTile=new RenderTile();renderTile.readFromNBT(b.tileData);
     renderTile.blockType=b.block;renderTile.blockMetadata=b.meta;renderTile.setWorldObj(world);
     Map<PlatformBlock,ru.givler.mbo.tileentity.ModelTileBase> renderTiles=new WeakHashMap<>();renderTiles.put(b,renderTile);
     set(renderer,RenderMovingPlatform.class,"decorTiles",renderTiles);
     GL11.glViewport(0,0,128,128);GL11.glClearColor(0,0,0,1);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
     GL11.glDisable(GL11.GL_LIGHTING);GL11.glEnable(GL11.GL_DEPTH_TEST);
     GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-2,3,-1,3,-10,10);
     GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GL11.glRotatef(20,1,0,0);
     render.invoke(renderer,platform,0F);
     ByteBuffer pixels=BufferUtils.createByteBuffer(128*128*4);GL11.glReadPixels(0,0,128,128,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
     int visible=0;for(int i=0;i<pixels.capacity();i+=4)if(pixels.get(i)!=0||pixels.get(i+1)!=0||pixels.get(i+2)!=0)visible++;
     if(visible<20)throw new AssertionError("Moving model disappeared: "+name+" facing "+facing);
     if(renderTile.movingPlatformLight!=0)throw new AssertionError("Model in darkness uses stored white lighting");
     int before=world.lightQueries;render.invoke(renderer,platform,0F);
     if(world.lightQueries!=before)throw new AssertionError("Repeated model render re-queries unchanged light samples");
     platform.setPosition(0,2,0);render.invoke(renderer,platform,1F);
     if(renderTile.movingPlatformLight!=0x00f000f0)throw new AssertionError("Model light does not follow platform into bright area");
     platform.setPosition(0,0,0);
    }
   }
   System.out.println("PASS actual platform model renderer: cage/bench on four facings, dynamic dark-to-bright light and reused light samples");
  } finally {buffer.destroy();}
 }
}
