package ru.givler.mbo.decor;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.block.model.BlockDecorCage;
import ru.givler.mbo.core.DecorSleepHooks;
import ru.givler.mbo.core.DecorSleepTransformer;
import ru.givler.mbo.entity.EntityDecorSeat;
import ru.givler.mbo.handler.DecorInteractions;
import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.tileentity.TileEntityModelCollision;
import sun.misc.Unsafe;

/** Real collision queries, two-player seats and verification of the actual vanilla sleep bytecode. */
public final class DecorInteractionsSmoke {
 static class TestWorld extends CollisionHelperLifecycleSmoke.TestWorld {
  ModelTileBase tile;int meta;List<Entity> entities;
  public boolean func_147470_e(AxisAlignedBB box){return false;}
  public List getCollidingBoundingBoxes(Entity entity,AxisAlignedBB query) {
   List<AxisAlignedBB> result=new ArrayList<>();
   owner.addCollisionBoxesToList(this,0,0,0,query,result,entity);
   BlockDecorCage cage=(BlockDecorCage)owner;
   for(float[] b:cage.boxesForCell(1,meta))if(cage.includesModelCollisionBox(this,0,0,0,b)) {
    AxisAlignedBB box=AxisAlignedBB.getBoundingBox(b[0],1+b[1],b[2],b[3],1+b[4],b[5]);
    if(box.intersectsWith(query))result.add(box);
   }
   return result;
  }
  public TileEntity getTileEntity(int x,int y,int z){return tile;}
  public int getBlockMetadata(int x,int y,int z){return meta;}
  public List getEntitiesWithinAABB(Class type,AxisAlignedBB box){List<Entity> result=new ArrayList<>();for(Entity e:entities)if(type.isInstance(e)&&!e.isDead)result.add(e);return result;}
  public boolean spawnEntityInWorld(Entity e){entities.add(e);return true;}
 }
 static class Player extends EntityPlayerMP {
  Player(){super(null,null,null,null);}
  public void mountEntity(Entity entity){if(ridingEntity!=null)ridingEntity.riddenByEntity=null;ridingEntity=entity;if(entity!=null)entity.riddenByEntity=this;}
 }
 static class Walker extends Entity {
  Walker(World world){super(world);}
  protected void entityInit(){}
  protected void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound tag){}
  protected void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound tag){}
  protected void func_145775_I(){}
  protected boolean canTriggerWalking(){return false;}
  public boolean isSneaking(){return false;}
  public boolean isWet(){return false;}
 }
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static void field(Object object,Class type,String name,Object value)throws Exception{Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(object,value);}
 static void animate(ru.givler.mbo.client.model.decormodels.DecorBlockModel provider,ModelTileBase tile,int id,double tick) {
  software.bernie.geckolib3.core.event.predicate.AnimationEvent<ModelTileBase> event=new software.bernie.geckolib3.core.event.predicate.AnimationEvent<>(tile,0,0,0,false,Collections.emptyList());event.animationTick=tick;
  provider.getAnimationProcessor().tickAnimation(tile,id,tick,event,software.bernie.geckolib3.molang.MolangRegistrar.getParser(),true);
 }
 public static void main(String[] args)throws Exception {
  Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);Unsafe u=(Unsafe)f.get(null);
  TestWorld world=(TestWorld)u.allocateInstance(TestWorld.class);world.entities=new ArrayList<>();world.rand=new Random(0);field(world,World.class,"provider",new WorldProviderSurface());
  BlockDecorCage cage=(BlockDecorCage)u.allocateInstance(BlockDecorCage.class);field(cage,BlockModels.class,"collisionParts",new ArrayList<>());field(cage,BlockModels.class,"modelName","instruments_of_torture/iron_cage");
  cage.withMultipartCollisionBoxes(BlockDecorCage.closedShape());
  java.util.Map<String,float[][]> cells=ru.givler.mbo.block.model.CollisionShapeParts.cells(BlockDecorCage.closedShape());
  check(cells.size()==2&&cells.containsKey("0,0,0")&&cells.containsKey("0,1,0"),"cage occupies more than 1x1x2 blocks");
  world.owner=cage;world.tile=new ModelTileBase("test","test");world.tile.setWorldObj(world);
  field(world,World.class,"theProfiler",new net.minecraft.profiler.Profiler());
  TileEntityModelCollision obsolete=new TileEntityModelCollision();obsolete.configure(0,0,0,new float[]{0,0,0,1,.1875F,1});obsolete.yCoord=2;obsolete.setWorldObj(world);world.available=true;obsolete.checkOwner();
  check(world.removed,"legacy third cage helper was not removed");world.removed=false;obsolete.yCoord=1;obsolete.checkOwner();check(!world.removed,"valid upper cage helper was removed");
  for(int dz:new int[]{-1,1}) {
   obsolete.zCoord=dz;
   check(obsolete.getBoxes().length==0,"legacy front/back helper still collides");
   obsolete.checkOwner();check(world.removed,"legacy front/back helper was not removed");world.removed=false;
  }
  obsolete.zCoord=0;
  int[] frontSides={3,4,2,5};
  double[][] frontPositions={{.5,1.5},{-.5,.5},{.5,-.5},{1.5,.5}};
  for(int facing=0;facing<4;facing++) {
   double[] p=frontPositions[facing];
   check(BlockDecorCage.canOperateDoor(facing,frontSides[facing],p[0],0,p[1]),"front cannot open door");
   check(!BlockDecorCage.canOperateDoor(facing,frontSides[facing],1-p[0],0,1-p[1]),"back can open door");
   for(int side=0;side<6;side++) {
    check(BlockDecorCage.canOperateDoor(facing,side,.5,0,.5),"inside cannot open door");
    if(side!=frontSides[facing])check(!BlockDecorCage.canOperateDoor(facing,side,p[0],0,p[1]),"outside non-front face can open door");
   }
  }
  AxisAlignedBB all=AxisAlignedBB.getBoundingBox(-2,-2,-2,3,4,3);
  for(int meta=0;meta<4;meta++) {
   world.meta=meta;world.tile.cageOpen=false;List<AxisAlignedBB> closed=new ArrayList<>();cage.addCollisionBoxesToList(world,0,0,0,all,closed,null);
   check(closed.size()==5,"closed cage root walls");world.tile.cageOpen=true;List<AxisAlignedBB> open=new ArrayList<>();cage.addCollisionBoxesToList(world,0,0,0,all,open,null);
   check(open.size()==4,"only door must disappear for facing "+meta);
   double[] front=DecorInteractions.rotate(0,-.49,meta);AxisAlignedBB doorway=AxisAlignedBB.getBoundingBox(.49+front[0],.2,.49+front[1],.51+front[0],.8,.51+front[1]);
   check(closed.stream().anyMatch(b->b.intersectsWith(doorway))&&!open.stream().anyMatch(b->b.intersectsWith(doorway)),"door collision is on the wrong face for "+meta);
   check(!open.stream().anyMatch(b->b.intersectsWith(AxisAlignedBB.getBoundingBox(.2,.07,.2,.8,.99,.8))),"cage interior is solid");
   TileEntityModelCollision old=new TileEntityModelCollision();old.configure(0,0,0,new float[]{0,0,0,1,1,1});old.yCoord=1;old.setWorldObj(world);
   int kept=0;for(float[] b:old.getBoxes())if(cage.includesModelCollisionBox(world,0,0,0,b))kept++;
   check(old.getBoxes().length==5&&kept==4,"old upper collision NBT must become hollow with open door");
   AxisAlignedBB playerSpace=AxisAlignedBB.getBoundingBox(.2,1/32D,.2,.8,1/32D+1.8,.8);
   double[] entrance=DecorInteractions.rotate(0,-.52,meta);
   AxisAlignedBB enteringPlayer=AxisAlignedBB.getBoundingBox(.2+entrance[0],1/32D,.2+entrance[1],.8+entrance[0],1/32D+1.8,.8+entrance[1]);
   for(int dy=0;dy<2;dy++)for(float[] b:cage.boxesForCell(dy,meta))if(cage.includesModelCollisionBox(world,0,0,0,b)) {
    AxisAlignedBB box=AxisAlignedBB.getBoundingBox(b[0],dy+b[1],b[2],b[3],dy+b[4],b[5]);
    check(!box.intersectsWith(playerSpace),"standing player does not fit inside cage");
    check(!box.intersectsWith(enteringPlayer),"standing player cannot enter open cage");
    check(b[0]>=0&&b[2]>=0&&b[3]<=1&&b[5]<=1&&dy+b[4]<=2,"collision extends beyond cage");
   }
   check(cage.boxesForCell(2,meta).length==0,"obsolete third collision block remains");
   Walker walker=(Walker)u.allocateInstance(Walker.class);walker.worldObj=world;
   walker.width=.6F;walker.height=1.8F;walker.stepHeight=.6F;walker.onGround=true;
   double[] start=DecorInteractions.rotate(0,-1,meta);
   field(walker,Entity.class,"boundingBox",AxisAlignedBB.getBoundingBox(.2+start[0],0,.2+start[1],.8+start[0],1.8,.8+start[1]));
   walker.moveEntity(-start[0],0,-start[1]);
   check(Math.abs((walker.boundingBox.minX+walker.boundingBox.maxX)/2-.5)<.001&&Math.abs((walker.boundingBox.minZ+walker.boundingBox.maxZ)/2-.5)<.001,"walking from ground into open cage failed for facing "+meta);
   walker.moveEntity(0,-.1,0);
   check(Math.abs(walker.boundingBox.minY)<.001,"flush cage floor does not support player");
   world.tile.cageOpen=false;walker.moveEntity(start[0],0,start[1]);
   check(Math.abs((walker.boundingBox.minX+walker.boundingBox.maxX)/2-.5)<.3&&Math.abs((walker.boundingBox.minZ+walker.boundingBox.maxZ)/2-.5)<.3,"closed door allows escape");
   world.tile.cageOpen=true;walker.moveEntity(start[0],0,start[1]);
   check(Math.abs((walker.boundingBox.minX+walker.boundingBox.maxX)/2-.5)>.9||Math.abs((walker.boundingBox.minZ+walker.boundingBox.maxZ)/2-.5)>.9,"cannot exit open cage");
  }
  net.minecraft.nbt.NBTTagCompound tag=new net.minecraft.nbt.NBTTagCompound();tag.setString("model","test");tag.setString("texture","test");tag.setBoolean("cageOpen",true);
  ModelTileBase restored=new ModelTileBase();restored.readFromNBT(tag);check(restored.cageOpen,"saved cage opens on reload");
  BlockModels bench=(BlockModels)u.allocateInstance(BlockModels.class);bench.withSeats(new float[]{0,.5F,.5F},new float[]{1,.5F,.5F});
  for(int meta=0;meta<4;meta++) {
   world.meta=meta;world.owner=bench;world.entities.clear();Player a=(Player)u.allocateInstance(Player.class),b=(Player)u.allocateInstance(Player.class),c=(Player)u.allocateInstance(Player.class);
   a.worldObj=b.worldObj=c.worldObj=world;DecorInteractions.sit(bench,world,0,0,0,a,0,.5F);DecorInteractions.sit(bench,world,0,0,0,b,0,.5F);DecorInteractions.sit(bench,world,0,0,0,c,0,.5F);
   check(world.entities.size()==2&&a.ridingEntity!=b.ridingEntity&&c.ridingEntity==null,"bench must offer exactly two independent seats");
   check(Math.abs(a.ridingEntity.getDistanceToEntity(b.ridingEntity)-1)<.0001,"rotated seats have wrong spacing");
   a.yOffset=1.62F;field(a,Entity.class,"boundingBox",AxisAlignedBB.getBoundingBox(0,0,0,.6,1.8,.6));
   a.ridingEntity.updateRiderPosition();
   check(Math.abs(a.posY-a.yOffset+.75-.625)<.0001,"seated hip height differs from adjusted bench pose");
   a.mountEntity(null);DecorInteractions.sit(bench,world,0,0,0,c,0,.5F);check(c.ridingEntity!=null,"vacated bench seat cannot be reused");
  }
  Player sleeper=(Player)u.allocateInstance(Player.class);sleeper.worldObj=world;field(sleeper,Entity.class,"boundingBox",AxisAlignedBB.getBoundingBox(0,0,0,1,1,1));
  BlockModels bed=(BlockModels)u.allocateInstance(BlockModels.class);bed.withSleepingNoRespawn(.125F,0);world.owner=bed;
  DecorSleepHooks.afterSleep(sleeper,0,0,0,EntityPlayer.EnumStatus.NOT_SAFE);check(DecorSleepHooks.setSpawnOnWake(sleeper,true),"failed sleep altered respawn");
  DecorSleepHooks.afterSleep(sleeper,0,0,0,EntityPlayer.EnumStatus.OK);check(Math.abs(sleeper.posY-.5)<.0001,"bed sleep height");
  check(!DecorSleepHooks.setSpawnOnWake(sleeper,true),"temporary bed creates respawn");check(DecorSleepHooks.setSpawnOnWake(sleeper,true),"ordinary bed is affected after waking");
  bed.withSleepingNoRespawn(.3875F,1,.7F,.5625F);
  sleeper.playerLocation=new net.minecraft.util.ChunkCoordinates(0,0,0);field(sleeper,EntityPlayer.class,"sleeping",true);
  sleeper.posY=-1;sleeper.motionY=-.08;sleeper.prevPosY=-1;
  DecorSleepHooks.keepSleepingPosition(sleeper);
  check(Math.abs(sleeper.posY-.7625)<.0001&&sleeper.prevPosY==sleeper.posY&&sleeper.motionY==0,"suspended sleeper sinks or camera remains below mattress");
  sleeper.yOffset=.2F;
  sleeper.height=.2F;
  check(!ru.givler.mbo.swimming.SwimmingHooks.isSwimming(sleeper)&&!ru.givler.mbo.swimming.SwimmingHooks.wantsToSwim(sleeper),"sleep hitbox activates swimming");
  ru.givler.mbo.swimming.SwimmingHooks.updateSize(sleeper);
  check(sleeper.height==.2F,"swimming resizes sleeping hitbox");
  java.lang.reflect.Method pose=ru.givler.mbo.client.render.SwimmingRenderHooks.class.getDeclaredMethod("poseAmount",EntityPlayer.class,float.class);pose.setAccessible(true);
  check(((Float)pose.invoke(null,sleeper,1F))==0F,"sleep activates swimming animation");
  net.minecraft.client.model.ModelBiped sleepingModel=new net.minecraft.client.model.ModelBiped();
  sleepingModel.bipedHead.rotateAngleX=1; sleepingModel.bipedHead.rotateAngleY=1;
  ru.givler.mbo.client.render.SwimmingRenderHooks.animate(sleepingModel,sleeper,0,0);
  check(sleepingModel.bipedHead.rotateAngleX==0&&sleepingModel.bipedHead.rotateAngleY==0&&sleepingModel.bipedHeadwear.rotateAngleX==0,"sleeping head is tilted by look direction");
  check(DecorSleepHooks.sleepingLabelDrop(1.5,sleeper)==0,"decor sleeping name sinks below bed");
  float camera=.3F; // Original vanilla/MF2 sleeping-camera translation.
  double eyeY=sleeper.posY-(sleeper.yOffset-1.62F+1)-camera;
  check(camera==.3F&&Math.abs(eyeY-.8825)<.0001&&eyeY>.3875,"sleep camera lacks clearance above suspended mattress");
  field(sleeper,EntityPlayer.class,"sleeping",false);
  check(DecorSleepHooks.sleepingLabelDrop(1.5,sleeper)==1.5,"awake name label changed");
  for(int meta=0;meta<4;meta++) {
   world.meta=meta;bed.withSleepingNoRespawn(.125F,2,.5F,.3125F);DecorSleepHooks.afterSleep(sleeper,0,0,0,EntityPlayer.EnumStatus.OK);
   double[] head=DecorInteractions.rotate(0,-.1875,meta);check(Math.abs(sleeper.posX-.5-head[0])<.0001&&Math.abs(sleeper.posZ-.5-head[1])<.0001,"head is not on the rotated pillow");
   check(bed.getBedDirection(world,0,0,0)==((meta+2)&3),"sleep body direction");
  }
  BlockModels ordinary=(BlockModels)u.allocateInstance(BlockModels.class);ordinary.withSleepingNoRespawn(-1,0);world.owner=ordinary;DecorSleepHooks.afterSleep(sleeper,0,0,0,EntityPlayer.EnumStatus.OK);
  check(DecorSleepHooks.setSpawnOnWake(sleeper,true),"stale saved temporary-sleep flag disabled ordinary bed respawn");
  byte[] original;try(java.io.InputStream in=EntityPlayer.class.getResourceAsStream("EntityPlayer.class")){java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] data=new byte[4096];int n;while((n=in.read(data))>=0)out.write(data,0,n);original=out.toByteArray();}
  byte[] transformed=new DecorSleepTransformer().transform(EntityPlayer.class.getName(),EntityPlayer.class.getName(),original);
  ClassNode node=new ClassNode();new ClassReader(transformed).accept(node,0);int after=0,wake=0;
  for(MethodNode m:node.methods) {
   if(m.name.equals("sleepInBedAt")||m.name.equals("wakeUpPlayer"))new Analyzer<BasicValue>(new BasicVerifier()).analyze(node.name,m);
   for(AbstractInsnNode ins:m.instructions.toArray())if(ins instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)ins;if(call.owner.equals("ru/givler/mbo/core/DecorSleepHooks")){if(call.name.equals("afterSleep"))after++;else wake++;}}
  }
  check(after>0&&wake==1,"vanilla sleep/wake hooks missing");
  try(java.io.InputStream in=net.minecraft.client.renderer.entity.RendererLivingEntity.class.getResourceAsStream("RendererLivingEntity.class")) {
   java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] data=new byte[4096];int n;while((n=in.read(data))>=0)out.write(data,0,n);original=out.toByteArray();
  }
  transformed=new ru.givler.mbo.core.DecorSleepRenderTransformer().transform("net.minecraft.client.renderer.entity.RendererLivingEntity","net.minecraft.client.renderer.entity.RendererLivingEntity",original);
  node=new ClassNode();new ClassReader(transformed).accept(node,0);int cameraHooks=0;
  for(MethodNode m:node.methods)if(m.name.equals("func_96449_a")) {
   new Analyzer<BasicValue>(new BasicVerifier()).analyze(node.name,m);
   for(AbstractInsnNode ins:m.instructions.toArray())if(ins instanceof MethodInsnNode&&((MethodInsnNode)ins).name.equals("sleepingLabelDrop"))cameraHooks++;
  }
  check(cameraHooks==1,"real sleeping name label drop was not patched");
  org.lwjgl.opengl.Pbuffer buffer=new org.lwjgl.opengl.Pbuffer(96,96,new org.lwjgl.opengl.PixelFormat(8,24,0),null,null);buffer.makeCurrent();StaticDecorPixelsSmoke.fakeMinecraft();StaticDecorPixelsSmoke.texture();
  net.minecraft.util.ResourceLocation resource=new net.minecraft.util.ResourceLocation("mbo","geo/iron_cage.geo.json");
  software.bernie.geckolib3.geo.render.built.GeoModel model=software.bernie.geckolib3.file.GeoModelLoader.getInstance().loadModel(net.minecraft.client.Minecraft.getMinecraft().getResourceManager(),resource);
  software.bernie.geckolib3.GeckoLib.hasInitialized=true;
  software.bernie.geckolib3.resource.GeckoLibCache cache=software.bernie.geckolib3.resource.GeckoLibCache.getInstance();cache.getGeoModels().put(resource,model);
  net.minecraft.util.ResourceLocation clips=new net.minecraft.util.ResourceLocation("mbo","animations/instruments_of_torture/iron_cage.animation.json");
  cache.getAnimations().put(clips,software.bernie.geckolib3.file.AnimationFileLoader.getInstance().loadAllAnimations(software.bernie.geckolib3.molang.MolangRegistrar.getParser(),clips,net.minecraft.client.Minecraft.getMinecraft().getResourceManager()));
  ru.givler.mbo.client.model.decormodels.DecorBlockModel provider=(ru.givler.mbo.client.model.decormodels.DecorBlockModel)StaticDecorPixelsSmoke.RENDERER.getGeoModelProvider();provider.getModel(resource);
  StaticDecorPixelsSmoke.TILE.setWorldObj(world);world.owner=cage;StaticDecorPixelsSmoke.TILE.cageOpen=false;
  for(int tick=0;tick<3;tick++)animate(provider,StaticDecorPixelsSmoke.TILE,0,tick);
  byte[] closed=StaticDecorPixelsSmoke.picture(model,resource,0,.8F,false);
  StaticDecorPixelsSmoke.TILE.cageOpen=true;for(int tick=3;tick<20;tick++)animate(provider,StaticDecorPixelsSmoke.TILE,0,tick);
  check(Math.abs(Math.abs(provider.getAnimationProcessor().getBone("cage_door").getRotationY())-Math.PI/2)<.001,"real Gecko controller did not open the door");
  check(!Arrays.equals(closed,StaticDecorPixelsSmoke.picture(model,resource,0,.8F,false)),"Gecko keyframes do not change door image");
  StaticDecorPixelsSmoke.TILE.cageOpen=false;for(int tick=20;tick<40;tick++)animate(provider,StaticDecorPixelsSmoke.TILE,0,tick);
  check(Math.abs(provider.getAnimationProcessor().getBone("cage_door").getRotationY())<.001,"real Gecko controller did not close the door");
  StaticDecorPixelsSmoke.TILE.setWorldObj(null);buffer.destroy();
  System.out.println("PASS cage collision on four facings, legacy helper NBT, door animation, saved open state, two seats/occupancy/rotation/reuse, sleep height/no respawn and verified vanilla bytecode");
 }
}
