package ru.givler.mbo.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockModels;

/** A transient seat on static decor. Vanilla sneaking dismounts normally. */
public final class EntityDecorSeat extends Entity {
 private int emptyTicks;
 public EntityDecorSeat(World world){super(world);setSize(0,0);noClip=true;setInvisible(true);}
 public EntityDecorSeat(World world,int x,int y,int z,int index,double px,double py,double pz) {
  this(world);dataWatcher.updateObject(17,x);dataWatcher.updateObject(18,y);dataWatcher.updateObject(19,z);dataWatcher.updateObject(20,index);setPosition(px,py,pz);
 }
 protected void entityInit(){for(int i=17;i<=20;i++)dataWatcher.addObject(i,Integer.valueOf(0));}
 public boolean belongsTo(int x,int y,int z){return dataWatcher.getWatchableObjectInt(17)==x&&dataWatcher.getWatchableObjectInt(18)==y&&dataWatcher.getWatchableObjectInt(19)==z;}
 public int seatIndex(){return dataWatcher.getWatchableObjectInt(20);}
 public void onUpdate() {
  if(!worldObj.isRemote) {
   net.minecraft.block.Block block=worldObj.getBlock(dataWatcher.getWatchableObjectInt(17),dataWatcher.getWatchableObjectInt(18),dataWatcher.getWatchableObjectInt(19));
   if(!(block instanceof BlockModels)||((BlockModels)block).getSeatingPositions()==null
      ||(riddenByEntity==null&&++emptyTicks>5)){if(riddenByEntity!=null)riddenByEntity.mountEntity(null);setDead();return;}
  }
  motionX=motionY=motionZ=0;updateRiderPosition();
 }
 public void setPositionAndRotation2(double x,double y,double z,float yaw,float pitch,int ticks){setPosition(x,y,z);}
 // Vanilla players add yOffset-.5 when mounted; the seated hip is .75 above the feet.
 public double getMountedYOffset(){return -.125;}
 public boolean canBeCollidedWith(){return false;}
 public boolean writeToNBTOptional(NBTTagCompound tag){return false;}
 public boolean writeMountToNBT(NBTTagCompound tag){return false;}
 protected void readEntityFromNBT(NBTTagCompound tag){setDead();}
 protected void writeEntityToNBT(NBTTagCompound tag){}
}
