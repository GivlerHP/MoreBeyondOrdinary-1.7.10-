package ru.givler.mbo.decor;

import java.lang.reflect.*;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.registry.ModelRegistry;
import ru.givler.mbo.tileentity.TileEntityModelCollision;
import sun.misc.Unsafe;
public class CollisionHelperLifecycleSmoke {
 static class TestWorld extends World {
  Block owner;boolean available,removed;int scheduled;
  TestWorld(){super(null,"test",(net.minecraft.world.WorldSettings)null,(net.minecraft.world.WorldProvider)null,null);}
  protected IChunkProvider createChunkProvider(){return null;}
  protected int func_152379_p(){return 0;}
  public Entity getEntityByID(int id){return null;}
  public Block getBlock(int x,int y,int z){return owner;}
  public boolean blockExists(int x,int y,int z){return available;}
  public boolean setBlockToAir(int x,int y,int z){removed=true;return true;}
  public void scheduleBlockUpdate(int x,int y,int z,Block block,int delay){scheduled=delay;}
 }
 public static void main(String[] args)throws Exception {
  Field access=Unsafe.class.getDeclaredField("theUnsafe");access.setAccessible(true);Unsafe unsafe=(Unsafe)access.get(null);
  TestWorld world=(TestWorld)unsafe.allocateInstance(TestWorld.class);
  BlockModels ordinary=(BlockModels)unsafe.allocateInstance(BlockModels.class);
  ModelRegistry.ModelTortureSuspendedCorpse=(BlockModels)unsafe.allocateInstance(BlockModels.class);
  TileEntityModelCollision tile=new TileEntityModelCollision();tile.configure(0,0,0,new float[]{0,0,0,1,1,1});
  tile.xCoord=0;tile.yCoord=2;tile.zCoord=0;tile.setWorldObj(world);
  tile.validate();if(world.scheduled!=1||tile.canUpdate())throw new AssertionError("load cleanup must be scheduled without continuous ticks");
  tile.checkOwner();if(world.removed||world.scheduled!=20)throw new AssertionError("unloaded owner chunk must not remove the helper");
  world.available=true;world.owner=ordinary;tile.checkOwner();if(world.removed)throw new AssertionError("valid helper removed");
  world.owner=ModelRegistry.ModelTortureSuspendedCorpse;tile.checkOwner();if(!world.removed)throw new AssertionError("obsolete upper helper remains");
  world.removed=false;tile.yCoord=1;tile.checkOwner();if(world.removed)throw new AssertionError("body selection helper removed");
  world.owner=null;tile.checkOwner();if(!world.removed)throw new AssertionError("orphan remains");
  System.out.println("Event-driven helper cleanup, chunk-boundary safety and old suspended-body helper removal passed.");
 }
}
