package ru.givler.mbo.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.Block;
import ru.givler.mbo.block.BlockModels;

public final class DecorSleepHooks {
 private DecorSleepHooks(){}
 public static boolean isDecorSleeping(net.minecraft.entity.EntityLivingBase entity) {
  if(!(entity instanceof EntityPlayer))return false;
  EntityPlayer player=(EntityPlayer)entity;
  if(!player.isPlayerSleeping()||player.playerLocation==null)return false;
  net.minecraft.util.ChunkCoordinates p=player.playerLocation;
  Block block=player.worldObj.getBlock(p.posX,p.posY,p.posZ);
  return block instanceof BlockModels&&((BlockModels)block).isTemporaryBed();
 }
 public static double sleepingLabelDrop(double vanilla,net.minecraft.entity.EntityLivingBase entity) {
  return isDecorSleeping(entity)?0:vanilla;
 }
 public static void keepSleepingPosition(EntityPlayer player) {
  if(!player.isPlayerSleeping()||player.playerLocation==null)return;
  net.minecraft.util.ChunkCoordinates p=player.playerLocation;
  Block block=player.worldObj.getBlock(p.posX,p.posY,p.posZ);
  if(!(block instanceof BlockModels)||!((BlockModels)block).isTemporaryBed())return;
  afterSleep(player,p.posX,p.posY,p.posZ,EntityPlayer.EnumStatus.OK);
  player.motionX=player.motionY=player.motionZ=0;player.fallDistance=0;
  player.prevPosX=player.lastTickPosX=player.posX;
  player.prevPosY=player.lastTickPosY=player.posY;
  player.prevPosZ=player.lastTickPosZ=player.posZ;
 }
 public static EntityPlayer.EnumStatus afterSleep(EntityPlayer player,int x,int y,int z,EntityPlayer.EnumStatus status) {
  Block block=player.worldObj.getBlock(x,y,z);
  if(status==EntityPlayer.EnumStatus.OK&&block instanceof BlockModels&&((BlockModels)block).isTemporaryBed()) {
   player.getEntityData().setBoolean("mboTemporarySleep",true);
   BlockModels bed=(BlockModels)block;float[] head=bed.getSleepingHeadPosition();
   double px=player.posX,pz=player.posZ;
   if(head!=null){double[] rotated=ru.givler.mbo.handler.DecorInteractions.rotate(head[0]-.5,head[1]-.5,player.worldObj.getBlockMetadata(x,y,z));px=x+.5+rotated[0];pz=z+.5+rotated[1];}
   // Match vanilla's head-to-body render anchor to the bed direction on both sides.
   // Metadata alone is not the sleeping direction of a sideways decorative bed.
   int direction=bed.getBedDirection(player.worldObj,x,y,z)&3;
   player.field_71079_bU=direction==1?1.8F:direction==3?-1.8F:0;
   player.field_71089_bV=direction==0?-1.8F:direction==2?1.8F:0;
   player.field_71082_cx=0;
   player.setPosition(px,y+bed.getSleepingHeight()+.375,pz);
  } else if(status==EntityPlayer.EnumStatus.OK) {
   // A logout while sleeping can leave Forge entity data after vanilla clears sleeping.
   player.getEntityData().removeTag("mboTemporarySleep");
  }
  return status;
 }
 public static boolean setSpawnOnWake(EntityPlayer player,boolean requested) {
  if(player.getEntityData().getBoolean("mboTemporarySleep")){player.getEntityData().removeTag("mboTemporarySleep");return false;}
  return requested;
 }
}
