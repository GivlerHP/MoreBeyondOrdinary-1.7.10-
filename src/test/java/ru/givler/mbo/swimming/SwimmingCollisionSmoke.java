package ru.givler.mbo.swimming;

import java.lang.reflect.Field;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import sun.misc.Unsafe;

public final class SwimmingCollisionSmoke {
  private static final class TestBlock extends Block { TestBlock(Material material) { super(material); } }
  private static final Block STONE=new TestBlock(Material.rock), WATER=new TestBlock(Material.water);
  private static final class TestWorld extends World {
    TestWorld() { super(null,"swim",(net.minecraft.world.WorldSettings)null,(net.minecraft.world.WorldProvider)null,null); }
    protected int func_152379_p() { return 0; }
    protected IChunkProvider createChunkProvider() { return null; }
    public Entity getEntityByID(int id) { return null; }
    public boolean func_147470_e(AxisAlignedBB box) { return false; }
    public Block getBlock(int x,int y,int z) { return y < 64 ? STONE : WATER; }
    public boolean isMaterialInBB(AxisAlignedBB box,Material material) { return box.maxY > 64; }
    public List getCollidingBoundingBoxes(Entity entity,AxisAlignedBB box) {
      AxisAlignedBB floor=AxisAlignedBB.getBoundingBox(-100,0,-100,100,64,100);
      return floor.intersectsWith(box) ? Collections.singletonList(floor) : Collections.emptyList();
    }
  }
  private static final class Player extends EntityPlayerMP {
    Player() { super(null,null,null,null); }
    public boolean isPlayerSleeping() { return false; }
    public boolean isEntityAlive() { return true; }
    public boolean isSprinting() { return true; }
    public boolean isInsideOfMaterial(Material material) { return true; }
    protected void func_145775_I() { }
    public boolean isWet() { return true; }
    protected boolean canTriggerWalking() { return false; }
    public void playSound(String sound,float volume,float pitch) { }
  }
  public static void check() throws Exception {
    Field f=Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true); Unsafe u=(Unsafe)f.get(null);
    TestWorld world=(TestWorld)u.allocateInstance(TestWorld.class);
    Field profiler=World.class.getDeclaredField("theProfiler"); profiler.setAccessible(true);
    profiler.set(world,new net.minecraft.profiler.Profiler());
    for(float offset:new float[]{0F,1.62F}) for(double staleAnchor:new double[]{0D,-1.22D,1.22D}) {
      Player p=(Player)u.allocateInstance(Player.class);
      p.worldObj=world; p.capabilities=new PlayerCapabilities(); p.width=.6F; p.height=1.8F;
      p.yOffset=offset;
      Field bounds=Entity.class.getDeclaredField("boundingBox"); bounds.setAccessible(true);
      bounds.set(p,AxisAlignedBB.getBoundingBox(-.3,64,-.3,.3,65.8,.3));
      Field random=Entity.class.getDeclaredField("rand"); random.setAccessible(true); random.set(p,new Random(0));
      p.posY=64+offset+staleAnchor; p.prevPosY=p.lastTickPosY=p.posY; p.stepHeight=.5F;
      SwimmingHooks.updateSize(p);
      if(!SwimmingHooks.isSwimming(p) || p.boundingBox.minY !=64) throw new AssertionError("Resize moved feet");
      if(Math.abs(p.posY-p.yOffset+p.ySize-64)>1E-6) throw new AssertionError("Stale standing position anchor");
      double cameraY=p.posY-p.yOffset+SwimmingHooks.cameraEyeHeight(p);
      if(Math.abs(cameraY-64.4D)>1E-6) throw new AssertionError("Swimming camera is outside collision box");
      p.setPosition(p.posX,p.posY,p.posZ);
      if(Math.abs(p.boundingBox.minY-64)>1E-6) throw new AssertionError("Position update moved swimmer into floor");
      p.moveEntity(0,-1,0);
      if(p.boundingBox.minY <64) throw new AssertionError("Swimmer crossed a full block floor");
    }
    System.out.println("Swimming floor collision and feet anchor passed");
  }
}
