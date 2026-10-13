package ru.givler.mbo.movingplatform;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import ru.givler.mbo.block.model.BlockDecorCage;
import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.tileentity.TileEntityModelCollision;
import sun.misc.Unsafe;

/** Hollow cage snapshots, relocated ownership and continuous passenger waiting. */
public final class PlatformDecorSmoke {
 static class TestWorld extends World {
  Map<String,TileEntity> tiles;Block cage;int closedSounds;
  TestWorld(){super(null,"test",(net.minecraft.world.WorldSettings)null,(net.minecraft.world.WorldProvider)null,null);}
  protected int func_152379_p(){return 0;}
  protected IChunkProvider createChunkProvider(){return null;}
  public Entity getEntityByID(int id){return null;}
  public TileEntity getTileEntity(int x,int y,int z){return tiles.get(x+","+y+","+z);}
  public Block getBlock(int x,int y,int z){return cage;}
  public int getBlockMetadata(int x,int y,int z){return facing;}
  public void markBlockForUpdate(int x,int y,int z){}
  public void markTileEntityChunkModified(int x,int y,int z,TileEntity tile){}
  public boolean func_147470_e(AxisAlignedBB box){return false;}
  public void setTileEntity(int x,int y,int z,TileEntity tile){tiles.put(x+","+y+","+z,tile);tile.setWorldObj(this);}
  public List getEntitiesWithinAABB(Class type,AxisAlignedBB box) {
   List<Entity> result=new ArrayList<>();for(Object raw:playerEntities){Entity e=(Entity)raw;if(type.isInstance(e)&&e.boundingBox.intersectsWith(box))result.add(e);}return result;
  }
  int facing;
  public List getCollidingBoundingBoxes(Entity entity,AxisAlignedBB query){return MovingPlatformTickHandler.addDecorCollisions(new ArrayList<>(),this,entity,query);}
  public void playSoundEffect(double x,double y,double z,String sound,float volume,float pitch){closedSounds++;}
 }
 static class Player extends EntityPlayerMP {
  UUID id=UUID.randomUUID();Player(){super(null,null,null,null);}
  public UUID getUniqueID(){return id;}
  double lowestY=Double.POSITIVE_INFINITY,transportY;
  public void setPosition(double x,double y,double z){lowestY=Math.min(lowestY,y);super.setPosition(x,y,z);}
  public void moveEntity(double x,double y,double z){transportY=y;setPosition(posX+x,posY+y,posZ+z);}
 }
 static class TestPlatform extends EntityMovingPlatform {
  int starts;TestPlatform(World world){super(world);}
  public boolean start(boolean toB,EntityPlayer player){starts++;return true;}
 }
 static class Walker extends Entity {
  Walker(World world){super(world);setSize(.6F,1.8F);}
  protected void entityInit(){}
  protected void readEntityFromNBT(NBTTagCompound tag){}
  protected void writeEntityToNBT(NBTTagCompound tag){}
  protected void func_145775_I(){}
  protected boolean canTriggerWalking(){return false;}
  public boolean isSneaking(){return false;}
  public boolean isWet(){return false;}
 }
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static void set(Object o,Class<?> type,String name,Object value)throws Exception{Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 public static void main(String[] args)throws Exception {
  Field access=Unsafe.class.getDeclaredField("theUnsafe");access.setAccessible(true);Unsafe u=(Unsafe)access.get(null);
  TileEntity.addMapping(ModelTileBase.class,"mbo_platform_model_test");TileEntity.addMapping(TileEntityModelCollision.class,"mbo_platform_helper_test");
  BlockDecorCage cage=new BlockDecorCage();
  Object registry=cpw.mods.fml.common.registry.GameData.getBlockRegistry();
  Method register=registry.getClass().getDeclaredMethod("addObjectRaw",int.class,String.class,Object.class);register.setAccessible(true);register.invoke(registry,4000,"mbo:platform_test_cage",cage);
  TestWorld world=(TestWorld)u.allocateInstance(TestWorld.class);world.tiles=new HashMap<>();world.cage=cage;
  set(world,World.class,"playerEntities",new ArrayList<>());
  set(world,World.class,"provider",new net.minecraft.world.WorldProviderSurface());world.rand=new Random(0);
  set(world,World.class,"theProfiler",new net.minecraft.profiler.Profiler());
  ModelTileBase tile=new ModelTileBase("instruments_of_torture/iron_cage","instruments_of_torture/iron_cage");
  tile.setWorldObj(world);world.tiles.put("0,0,0",tile);
  PlatformBlock root=new PlatformBlock(0,0,0,cage,0);root.captureCollision(world,0,0,0);
  check(root.collisionBoxes.length==5,"closed cage snapshot walls missing");
  PlatformBlock copy=PlatformBlock.read(root.write());
  check(copy!=null&&copy.collisionBoxes.length==5&&copy.tileData!=null,"snapshot persistence lost model or hollow collision");
  copy.restoreTile(world,10,20,30);check(world.getTileEntity(10,20,30) instanceof ModelTileBase,"model tile not restored at destination");
  ru.givler.mbo.block.model.BlockModelCollision helperBlock=new ru.givler.mbo.block.model.BlockModelCollision();
  register.invoke(registry,4001,"mbo:platform_test_helper",helperBlock);
  TileEntityModelCollision helper=new TileEntityModelCollision();helper.yCoord=1;helper.setWorldObj(world);
  helper.configure(0,0,0,cage.boxesForCell(1,0));world.tiles.put("0,1,0",helper);
  PlatformBlock helperSnapshot=new PlatformBlock(0,1,0,helperBlock,0);helperSnapshot.captureCollision(world,0,1,0);
  PlatformBlock helperCopy=PlatformBlock.read(helperSnapshot.write());helperCopy.restoreTile(world,10,20,30);
  TileEntityModelCollision restored=(TileEntityModelCollision)world.getTileEntity(10,21,30);
  check(restored.getOwnerX()==10&&restored.getOwnerY()==20&&restored.getOwnerZ()==30,"arrival helper still points at departure coordinates");
  check(helperCopy.collisionBoxes.length==5,"upper hollow cage shapes lost in NBT");
  tile.cageOpen=true;root.captureCollision(world,0,0,0);check(root.collisionBoxes.length==4,"open cage doorway filled during capture");
  TestPlatform platform=new TestPlatform(world);platform.getBlocks().add(root);
  set(platform,EntityMovingPlatform.class,"configured",true);set(platform,EntityMovingPlatform.class,"sizeY",2);
  set(platform,EntityMovingPlatform.class,"returnMode",4);set(platform,EntityMovingPlatform.class,"delayTicks",3);
  Method tick=EntityMovingPlatform.class.getDeclaredMethod("tickPlayerWait");tick.setAccessible(true);
  Player player=(Player)u.allocateInstance(Player.class);player.id=UUID.randomUUID();player.worldObj=world;
  set(player,Entity.class,"boundingBox",AxisAlignedBB.getBoundingBox(.2,0,.2,.8,1.8,.8));player.posX=player.posZ=.5;
  world.playerEntities.add(player);
  tick.invoke(platform);tick.invoke(platform);check(platform.starts==0,"platform left before waiting period");
  player.posX=2;tick.invoke(platform);player.posX=.5;tick.invoke(platform);tick.invoke(platform);
  check(platform.starts==0,"leaving zone failed to reset continuous wait");
  tick.invoke(platform);check(platform.starts==0&&!tile.cageOpen&&world.closedSounds==1,"door did not start closing before departure");
  for(int i=0;i<9;i++)tick.invoke(platform);
  check(platform.starts==0,"platform departs before half-second closing animation finishes");
  tick.invoke(platform);check(platform.starts==1,"closed cage did not depart after animation");
  for(int i=0;i<30;i++)tick.invoke(platform);
  check(platform.starts==1,"passenger staying inside triggers return trip");
  NBTTagCompound passengerSave=platform.writePlatformTag();
  check(passengerSave.getTagList("PassengersMustExit",10).tagCount()==1,"exit requirement is not saved");
  EntityMovingPlatform reloaded=new EntityMovingPlatform(null);reloaded.readPlatformTag(passengerSave);
  check(reloaded.writePlatformTag().getTagList("PassengersMustExit",10).tagCount()==1,"reload forgets passenger exit requirement");
  player.posX=2;tick.invoke(platform);player.posX=.5;
  tick.invoke(platform);tick.invoke(platform);check(platform.starts==1,"re-entry skips waiting time");
  tick.invoke(platform);check(platform.starts==2,"exit and re-entry does not rearm platform");
  player.posX=.5;player.boundingBox.offset(0,2,0);check(!platform.isPlayerInWaitingZone(player),"player on cage roof triggers boarding");
  player.boundingBox.offset(0,-2,0);tile.cageOpen=true;
  player.posX=2;tick.invoke(platform);player.posX=.5;
  player.boundingBox.offset(0,0,.5);tick.invoke(platform);tick.invoke(platform);tick.invoke(platform);
  check(platform.starts==2&&tile.cageOpen,"door closes through player in doorway");
  root.captureCollision(world,0,0,0);
  Method rebuild=EntityMovingPlatform.class.getDeclaredMethod("rebuildCollisionCache");rebuild.setAccessible(true);rebuild.invoke(platform);
  check(platform.getOuterBlocks().size()==4,"moving cage collision became solid envelope");
  for(PlatformBlock b:platform.getOuterBlocks())check(!(b.minX<.5&&b.maxX>.5&&b.minZ<.5&&b.maxZ>.5&&b.maxY>.5),"moving snapshot fills cage interior");
  platform.getBlocks().add(helperSnapshot);set(platform,EntityMovingPlatform.class,"virtualized",true);
  MovingPlatformTickHandler.track(platform);
  for(int facing=0;facing<4;facing++) {
   // Test the real vanilla movement algorithm against the platform at its raised position.
   world.facing=facing;
   tile.cageOpen=false;root.captureCollision(world,0,0,0);helperSnapshot.captureCollision(world,0,1,0);
   rebuild.invoke(platform);platform.setPosition(0,1,0);
   Walker walker=new Walker(world);walker.setPosition(.5,1,.5);
   double[][] directions={{0,1},{-1,0},{0,-1},{1,0}};
   double[] front=directions[facing];walker.moveEntity(front[0],0,front[1]);
   check(walker.posX>0&&walker.posX<1&&walker.posZ>0&&walker.posZ<1,"closed moving cage permits exit at facing "+facing);
   for(int step=1;step<=10;step++) {
    platform.setPosition(0,1+step*.05,0);walker.moveEntity(0,.05,0);walker.moveEntity(front[0],0,front[1]);
    walker.moveEntity(0,-1,0);
    check(walker.posX>0&&walker.posX<1&&walker.posZ>0&&walker.posZ<1&&Math.abs(walker.boundingBox.minY-platform.posY)<.001,"passenger escapes or falls through ascending cage at facing "+facing);
   }
   platform.setPosition(0,1,0);
   tile.cageOpen=true;root.captureCollision(world,0,0,0);helperSnapshot.captureCollision(world,0,1,0);rebuild.invoke(platform);
   walker.setPosition(.5,1,.5);walker.moveEntity(front[0],0,front[1]);
   check(walker.posX<0||walker.posX>1||walker.posZ<0||walker.posZ>1,"open moving cage door is blocked at facing "+facing);
  }
  MovingPlatformTickHandler.untrack(platform);
  set(player,Entity.class,"boundingBox",AxisAlignedBB.getBoundingBox(.2,1,.2,.8,2.8,.8));player.posX=player.posZ=.5;player.posY=1;player.prevPosY=.95;
  player.lowestY=Double.POSITIVE_INFINITY;platform.prevPosY=.95;
  Map<UUID,Integer> carriers=new HashMap<>();carriers.put(player.id,platform.getEntityId());
  Method carry=MovingPlatformTickHandler.class.getDeclaredMethod("processPlayer",EntityPlayer.class,EntityMovingPlatform.class,Map.class);carry.setAccessible(true);carry.invoke(null,player,platform,carriers);
  check(player.lowestY>=1&&player.transportY==0,"passenger rewound to old floor and lifted twice");
  byte[] original;try(java.io.InputStream in=World.class.getResourceAsStream("World.class")) {
   java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] data=new byte[4096];int n;while((n=in.read(data))!=-1)out.write(data,0,n);original=out.toByteArray();
  }
  byte[] patched=new ru.givler.mbo.core.PlatformDecorCollisionTransformer().transform(World.class.getName(),World.class.getName(),original);
  org.objectweb.asm.tree.ClassNode node=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(patched).accept(node,0);int hooks=0;
  for(org.objectweb.asm.tree.MethodNode method:node.methods)if(method.name.equals("getCollidingBoundingBoxes")) {
   new org.objectweb.asm.tree.analysis.Analyzer<>(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,method);
   for(org.objectweb.asm.tree.AbstractInsnNode ins:method.instructions.toArray())if(ins instanceof org.objectweb.asm.tree.MethodInsnNode&&((org.objectweb.asm.tree.MethodInsnNode)ins).name.equals("addDecorCollisions"))hooks++;
  }
  check(hooks>0,"vanilla movement collision hook absent");
  PlatformClippingSmoke.check();
  System.out.println("PASS model snapshot/restore, hollow cage, wait reset, automatic close, doorway safety and existing platform checks");
 }
}
