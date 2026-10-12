package ru.givler.mbo.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.entity.EntityDecorSeat;

public final class DecorInteractions {
 private DecorInteractions(){}
 public static boolean sleep(BlockModels block,World world,int x,int y,int z,EntityPlayer player) {
  if(world.isRemote)return true;
  for(Object object:world.playerEntities) {
   EntityPlayer other=(EntityPlayer)object;
   if(other.isPlayerSleeping()&&other.playerLocation!=null&&other.playerLocation.posX==x
      &&other.playerLocation.posY==y&&other.playerLocation.posZ==z){player.addChatMessage(new ChatComponentTranslation("tile.bed.occupied"));return true;}
  }
  EntityPlayer.EnumStatus status=player.sleepInBedAt(x,y,z);
  if(status!=EntityPlayer.EnumStatus.OK)player.addChatMessage(new ChatComponentTranslation(
   status==EntityPlayer.EnumStatus.NOT_SAFE?"tile.bed.notSafe":"tile.bed.noSleep"));
  return true;
 }
 public static double[] rotate(double x,double z,int facing) {
  switch(facing&3){case 0:return new double[]{-x,-z};case 1:return new double[]{z,-x};case 3:return new double[]{-z,x};default:return new double[]{x,z};}
 }
 public static boolean sit(BlockModels block,World world,int x,int y,int z,EntityPlayer player,float hitX,float hitZ) {
  if(world.isRemote)return true;
  if(player.ridingEntity!=null)return true;
  float[][] seats=block.getSeatingPositions();boolean[] occupied=new boolean[seats.length];
  for(Object object:world.getEntitiesWithinAABB(EntityDecorSeat.class,AxisAlignedBB.getBoundingBox(x-3,y-2,z-3,x+4,y+3,z+4))) {
   EntityDecorSeat seat=(EntityDecorSeat)object;
   if(!seat.isDead&&seat.belongsTo(x,y,z)&&seat.riddenByEntity!=null&&seat.seatIndex()>=0&&seat.seatIndex()<occupied.length)occupied[seat.seatIndex()]=true;
  }
  int selected=-1;double nearest=Double.POSITIVE_INFINITY;
  for(int i=0;i<seats.length;i++)if(!occupied[i]) {
   double[] p=rotate(seats[i][0]-.5,seats[i][2]-.5,world.getBlockMetadata(x,y,z));
   double distance=Math.pow(.5+p[0]-hitX,2)+Math.pow(.5+p[1]-hitZ,2);
   if(distance<nearest){nearest=distance;selected=i;}
  }
  if(selected>=0) {
   double[] p=rotate(seats[selected][0]-.5,seats[selected][2]-.5,world.getBlockMetadata(x,y,z));
   EntityDecorSeat seat=new EntityDecorSeat(world,x,y,z,selected,x+.5+p[0],y+seats[selected][1],z+.5+p[1]);
   if(world.spawnEntityInWorld(seat))player.mountEntity(seat);
  }
  return true;
 }
}
