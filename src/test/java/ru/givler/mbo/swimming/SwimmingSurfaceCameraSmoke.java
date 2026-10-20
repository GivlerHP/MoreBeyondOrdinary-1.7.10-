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
  private static final Block WATER=new TestBlock(Material.water), AIR=new TestBlock(Material.air);
  static class Pool extends World {
    Pool(){super(null,"pool",(net.minecraft.world.WorldSettings)null,null,null);}
    protected int func_152379_p(){return 0;}
    protected IChunkProvider createChunkProvider(){return null;}
    public Entity getEntityByID(int id){return null;}
    public Block getBlock(int x,int y,int z){return y==64 && Math.abs(x)<=1 && Math.abs(z)<=1?WATER:AIR;}
    public int getBlockMetadata(int x,int y,int z){return 0;}
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
      }
      mc.gameSettings.thirdPersonView=1;player.rotationPitch=player.prevRotationPitch=90;
      WaterloggingCameraHooks.beginCameraPosition();WaterloggingCameraHooks.thirdPersonDistance(.3,1);WaterloggingCameraHooks.captureCamera(1);
      if(WaterloggingCameraHooks.getViewBlock(world,player,1)!=AIR)throw new AssertionError("F5 above surface retained underwater effect");
      System.out.println("F5 numeric camera: rear/front views, clipped/full distance, repeated captures and true surface crossing passed without GL matrices");
      System.out.println("Surface swim camera: shallow source water, Ctrl pose, vertical look, both anchors, partial interpolation and real surfacing passed");
    }finally {singleton.set(null,old);captured.set(null,oldEntity);camera.set(null,oldCamera);WaterloggingCameraHooks.beginCameraPosition();}
  }
}
