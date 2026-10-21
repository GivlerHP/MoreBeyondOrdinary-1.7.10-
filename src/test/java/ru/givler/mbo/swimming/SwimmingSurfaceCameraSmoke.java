package ru.givler.mbo.swimming;

import java.lang.reflect.Field;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import ru.givler.mbo.core.WaterloggingCameraHooks;
import sun.misc.Unsafe;

/** Real first-person capture must follow the swimming eyes without requiring GL matrices. */
public final class SwimmingSurfaceCameraSmoke {
  static class TestBlock extends Block {TestBlock(Material material){super(material);}}
  static class TestBiome extends net.minecraft.world.biome.BiomeGenBase {TestBiome(){super(0);}}
  private static final Block WATER=new TestBlock(Material.water), AIR=new TestBlock(Material.air);
  static class Pool extends World {
    net.minecraft.world.biome.BiomeGenBase biome;
    Pool(){super(null,"pool",(net.minecraft.world.WorldSettings)null,null,null);}
    protected int func_152379_p(){return 0;}
    protected IChunkProvider createChunkProvider(){return null;}
    public Entity getEntityByID(int id){return null;}
    public Block getBlock(int x,int y,int z){return y==64 && Math.abs(x)<=1 && Math.abs(z)<=1?WATER:AIR;}
    public int getBlockMetadata(int x,int y,int z){return 0;}
    public net.minecraft.world.biome.BiomeGenBase getBiomeGenForCoords(int x,int z){return biome;}
  }
  static class BoundaryPool extends Pool {
    net.minecraft.world.biome.BiomeGenBase west, east;
    int samples;
    public net.minecraft.world.biome.BiomeGenBase getBiomeGenForCoords(int x,int z){samples++;return x<0?west:east;}
  }
  static class Swimmer extends EntityPlayerMP {
    Swimmer(){super(null,null,null,null);}
    public boolean isPlayerSleeping(){return false;}
  }
  public static void check()throws Exception {
    Field uf=Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);Unsafe u=(Unsafe)uf.get(null);
    Field singleton=Minecraft.class.getDeclaredField("theMinecraft");singleton.setAccessible(true);Object old=singleton.get(null);
    Field captured=WaterloggingCameraHooks.class.getDeclaredField("capturedEntity");captured.setAccessible(true);Object oldEntity=captured.get(null);
    Field camera=WaterloggingCameraHooks.class.getDeclaredField("capturedCamera");camera.setAccessible(true);Object oldCamera=camera.get(null);
    try {
      Minecraft mc=(Minecraft)u.allocateInstance(Minecraft.class);singleton.set(null,mc);
      mc.gameSettings=(GameSettings)u.allocateInstance(GameSettings.class);mc.gameSettings.thirdPersonView=0;
      Pool world=(Pool)u.allocateInstance(Pool.class);world.isRemote=true;Field provider=World.class.getDeclaredField("provider");provider.setAccessible(true);provider.set(world,new net.minecraft.world.WorldProviderSurface());mc.theWorld=null;
      world.biome=(net.minecraft.world.biome.BiomeGenBase)u.allocateInstance(TestBiome.class);
      world.biome.waterColorMultiplier=0xFFFFFF;
      checkBiomeFog(u);
      Swimmer player=(Swimmer)u.allocateInstance(Swimmer.class);player.worldObj=world;player.height=.6F;player.width=.6F;
      player.posX=player.prevPosX=.5;player.posZ=player.prevPosZ=.5;mc.renderViewEntity=player;
      Field box=Entity.class.getDeclaredField("boundingBox");box.setAccessible(true);
      for(float offset:new float[]{0F,1.62F})for(float pitch:new float[]{-80,0,80}) {
        player.yOffset=offset;player.rotationPitch=player.prevRotationPitch=pitch;
        player.posY=64.4+offset;player.prevPosY=64.3+offset;
        box.set(player,AxisAlignedBB.getBoundingBox(.2,64.4,.2,.8,65,.8));
        for(float partial:new float[]{0F,.5F,1F}) {
          WaterloggingCameraHooks.captureCamera(partial);
          if(WaterloggingCameraHooks.getViewBlock(world,player,partial)!=WATER)
            throw new AssertionError("Surface swim camera lost water at pitch="+pitch+" offset="+offset+" partial="+partial);
          net.minecraftforge.client.event.EntityViewRenderEvent.FogColors color =
              new net.minecraftforge.client.event.EntityViewRenderEvent.FogColors(
                  null, player, AIR, partial, 1F, 1F, 1F);
          new ru.givler.mbo.client.render.WaterloggedBlockRenderer().underwaterColor(color);
          if(Math.abs(color.red-.055F)>1E-6 || Math.abs(color.green-.145F)>1E-6 || Math.abs(color.blue-.235F)>1E-6)
            throw new AssertionError("Underwater fog inherited another mod's air block/color");
          net.minecraft.util.Vec3 position=(net.minecraft.util.Vec3)camera.get(null);
          if(Math.abs(position.yCoord-(64.7+.1*partial))>1E-6)throw new AssertionError("Swimming eye interpolation mismatch");
        }
      }
      player.posY=player.prevPosY=64.6+player.yOffset;
      box.set(player,AxisAlignedBB.getBoundingBox(.2,64.6,.2,.8,65.2,.8));
      WaterloggingCameraHooks.captureCamera(1);
      if(WaterloggingCameraHooks.getViewBlock(world,player,1)!=AIR)throw new AssertionError("Water effect retained after eyes actually left pool");
      player.posY=player.prevPosY=64.4+player.yOffset;
      player.rotationYaw=player.prevRotationYaw=0;player.rotationPitch=player.prevRotationPitch=0;
      box.set(player,AxisAlignedBB.getBoundingBox(.2,64.4,.2,.8,65,.8));
      for(int mode:new int[]{1,2}) {
        mc.gameSettings.thirdPersonView=mode;
        for(int repeat=0;repeat<20;repeat++) {
          WaterloggingCameraHooks.beginCameraPosition();
          WaterloggingCameraHooks.thirdPersonDistance(.2,1);
          WaterloggingCameraHooks.captureCamera(1);
          if(WaterloggingCameraHooks.getViewBlock(world,player,1)!=WATER)throw new AssertionError("Clipped F5 camera lost shallow water");
          net.minecraft.util.Vec3 position=(net.minecraft.util.Vec3)camera.get(null);
          double expectedZ=mode==1?.3:.7;
          if(Math.abs(position.yCoord-64.8)>1E-6 || Math.abs(position.zCoord-expectedZ)>1E-6)throw new AssertionError("F5 numerical camera mismatch");
        }
        WaterloggingCameraHooks.beginCameraPosition();WaterloggingCameraHooks.thirdPersonDistance(4,1);WaterloggingCameraHooks.captureCamera(1);
        if(WaterloggingCameraHooks.getViewBlock(world,player,1)!=AIR)throw new AssertionError("F5 camera outside pool inherited player water state");
        net.minecraftforge.client.event.EntityViewRenderEvent.FogColors color =
            new net.minecraftforge.client.event.EntityViewRenderEvent.FogColors(
                null, player, WATER, 1D, .3F, .4F, .5F);
        new ru.givler.mbo.client.render.WaterloggedBlockRenderer().underwaterColor(color);
        if(color.red != .3F || color.green != .4F || color.blue != .5F)
          throw new AssertionError("Dry F5 camera inherited the player's underwater fog");
      }
      mc.gameSettings.thirdPersonView=1;player.rotationPitch=player.prevRotationPitch=90;
      WaterloggingCameraHooks.beginCameraPosition();WaterloggingCameraHooks.thirdPersonDistance(.3,1);WaterloggingCameraHooks.captureCamera(1);
      if(WaterloggingCameraHooks.getViewBlock(world,player,1)!=AIR)throw new AssertionError("F5 above surface retained underwater effect");
      System.out.println("F5 numeric camera: rear/front views, clipped/full distance, repeated captures and true surface crossing passed without GL matrices");
      System.out.println("Surface swim camera: shallow source water, Ctrl pose, vertical look, both anchors, partial interpolation and real surfacing passed");
      cpw.mods.fml.common.eventhandler.SubscribeEvent density =
          ru.givler.mbo.client.render.WaterloggedBlockRenderer.class.getMethod("underwaterFog",
              net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity.class)
              .getAnnotation(cpw.mods.fml.common.eventhandler.SubscribeEvent.class);
      if(!density.receiveCanceled()
          || density.priority() != cpw.mods.fml.common.eventhandler.EventPriority.LOWEST)
        throw new AssertionError("Other mods can bypass the water camera density handler");
    }finally {singleton.set(null,old);captured.set(null,oldEntity);camera.set(null,oldCamera);WaterloggingCameraHooks.beginCameraPosition();}
  }

  private static void checkBiomeFog(Unsafe u)throws Exception {
    BoundaryPool world=(BoundaryPool)u.allocateInstance(BoundaryPool.class);
    world.west=(net.minecraft.world.biome.BiomeGenBase)u.allocateInstance(TestBiome.class);
    world.east=(net.minecraft.world.biome.BiomeGenBase)u.allocateInstance(TestBiome.class);
    world.west.waterColorMultiplier=0xFF0000;world.east.waterColorMultiplier=0x0000FF;
    float[] west=ru.givler.mbo.client.render.WaterFogColors.sample(world,net.minecraft.util.Vec3.createVectorHelper(-100,-64,0));
    float[] east=ru.givler.mbo.client.render.WaterFogColors.sample(world,net.minecraft.util.Vec3.createVectorHelper(100,64,0));
    if(Math.abs(west[0]-.055F)>1E-6 || west[1]!=0 || west[2]!=0
        || east[0]!=0 || east[1]!=0 || Math.abs(east[2]-.235F)>1E-6)
      throw new AssertionError("Biome water tint channels crossed");
    world.samples=0;
    float[] middle=ru.givler.mbo.client.render.WaterFogColors.sample(world,net.minecraft.util.Vec3.createVectorHelper(0,64,0));
    if(Math.abs(middle[0]-.055F*.5F)>1E-6 || middle[1]!=0 || Math.abs(middle[2]-.235F*.5F)>1E-6 || world.samples>16)
      throw new AssertionError("Biome border not equally blended or excess biome lookups");
    float[] before=ru.givler.mbo.client.render.WaterFogColors.sample(world,net.minecraft.util.Vec3.createVectorHelper(-.500001,64,0));
    float[] after=ru.givler.mbo.client.render.WaterFogColors.sample(world,net.minecraft.util.Vec3.createVectorHelper(-.499999,64,0));
    for(int i=0;i<3;i++)if(Math.abs(before[i]-after[i])>1E-6)throw new AssertionError("Fog jumps at negative block boundary");
    System.out.println("Biome water fog: neutral tint, distinct channels, smooth negative-coordinate borders and <=16 biome queries passed");
  }
}
