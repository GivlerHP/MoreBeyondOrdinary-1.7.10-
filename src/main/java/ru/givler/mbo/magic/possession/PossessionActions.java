package ru.givler.mbo.magic.possession;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.util.DamageSource;
import ru.givler.mbo.entity.fauna.*;
import ru.givler.mbo.magic.possession.PossessionManager.Session;

/** Non-combat animal abilities. Sheep grazing follows vanilla terrain and wool rules. */
public final class PossessionActions {
  public static final int SHEEP_MEAL_LIMIT=6;
  private PossessionActions(){}
  static boolean movementBlocked(Session s) {
    EntityLivingBase animal=s.animal;
    if(s.eatTicks>0)return true;
    if(animal instanceof EntityMBOCamel&&(((EntityMBOCamel)animal).sitting()||((EntityMBOCamel)animal).transitioning()))return true;
    if(s.pose>0&&(animal instanceof EntityMBOPanda||animal instanceof EntityMBOFox||animal instanceof EntityMBOAxolotl||animal instanceof EntityMBOGoat))return true;
    // Horse's native predicate blocks AI whenever a saddle has a rider. A passenger
    // must not block possession; eating/rearing still retain their native locks.
    if(animal instanceof EntityHorse){EntityHorse horse=(EntityHorse)animal;return horse.isEatingHaystack()||horse.isRearing()&&!horse.isHorseJumping();}
    return animal instanceof PossessionAnimal&&((PossessionAnimal)animal).mbo$movementBlocked()
        &&!(animal instanceof EntityHorse&&((EntityHorse)animal).isHorseJumping());
  }
  public static float jumpCharge(int ticks) {
    return ticks<=0?0:ticks<10?ticks*.1F:.8F+.2F/(ticks-9);
  }
  static int updateCharge(Session s,boolean blocked) {
    if(blocked){s.horseCharge=0;s.jumpHeld=s.jump;return -1;}
    int release=-1;
    if(s.jump&&s.animal.onGround)s.horseCharge=Math.min(1000,s.horseCharge+1);
    else if(!s.jump&&s.jumpHeld&&s.animal.onGround&&s.horseCharge>0)release=(int)(jumpCharge(s.horseCharge)*100);
    if(!s.jump||!s.animal.onGround)s.horseCharge=0;
    s.jumpHeld=s.jump;return release;
  }
  /** Scope steering to the native horse travel call; never actually mount or move the mage. */
  static void travelHorse(Session s,float strafe,float forward,int charge) {
    EntityHorse horse=(EntityHorse)s.animal;
    net.minecraft.entity.Entity oldRider=horse.riddenByEntity;
    boolean saddle=horse.isHorseSaddled(),tame=horse.isTame();
    float oldForward=s.player.moveForward,oldStrafe=s.player.moveStrafing,oldYaw=s.player.rotationYaw,oldPitch=s.player.rotationPitch;
    try {
      s.steering=true;
      horse.riddenByEntity=s.player;horse.setHorseSaddled(true);horse.setHorseTamed(true);
      s.player.moveForward=forward*PossessionManager.walkingInput(s.sprint);s.player.moveStrafing=strafe*PossessionManager.walkingInput(s.sprint);
      s.player.rotationYaw=s.lookYaw;s.player.rotationPitch=s.lookPitch;
      if(charge>=0){horse.setJumpPower(charge);s.actionTicks=20;if(horse instanceof EntityMBOCamel)s.dashCooldown=((EntityMBOCamel)horse).dashCooldown();}
      horse.moveEntityWithHeading(strafe,forward);
    } finally {
      s.steering=false;
      horse.riddenByEntity=oldRider;horse.setHorseSaddled(saddle);horse.setHorseTamed(tame);
      s.player.moveForward=oldForward;s.player.moveStrafing=oldStrafe;s.player.rotationYaw=oldYaw;s.player.rotationPitch=oldPitch;
    }
  }
  public static void action(Session s) {
    EntityLivingBase a=s.animal;
    if(a instanceof EntityBat){batAction(s);return;}
    if(a instanceof EntitySheep){startGrazing(s);return;}
    if(a instanceof EntityMBOCamel){EntityMBOCamel c=(EntityMBOCamel)a;if(c.sitting()&&c.canStand()||!c.sitting())c.setSitting(!c.sitting());return;}
    if(a instanceof EntityHorse){((EntityHorse)a).setRearing(true);s.actionTicks=30;a.playSound(a instanceof EntityMBOZombieHorse?"mob.horse.zombie.idle":a instanceof EntityMBOSkeletonHorse?"mob.horse.skeleton.idle":"mob.horse.angry",1,1);return;}
    if(a instanceof EntityMBOPolarBear||a instanceof EntityMBOGoat||a instanceof EntityMBOFrog){s.pose=1;s.actionTicks=40;}
    else if(a instanceof EntityMBOPanda||a instanceof EntityMBOFox||a instanceof EntityMBOAxolotl){s.pose=s.pose==0?1:0;}
    else if(a instanceof EntityMBOPufferfish){s.pose=s.pose==0?1:0;}
    else if(a instanceof EntityMBOGlowSquid) {
      for(int i=0;i<12;i++)((net.minecraft.world.WorldServer)a.worldObj).func_147487_a("happyVillager",a.posX,a.posY+.3,a.posZ,1,.4,.4,.4,0);
      a.playSound("mbo:entity.glow_squid.squirt",1,1);
    }
    if(a instanceof EntityMBOFrog)a.playSound("mbo:entity.frog.ambient",1,1);
    if(a instanceof EntityMBOPolarBear)a.playSound("mbo:entity.polar_bear.warning",1,1);
    if(a instanceof EntityMBOGoat)a.playSound(((EntityMBOGoat)a).screaming()?"mbo:entity.goat.screaming.prepare_ram":"mbo:entity.goat.prepare_ram",1,1);
    if(a instanceof EntityMBOPanda)a.playSound("mbo:entity.panda.ambient",1,1);
    if(a instanceof EntityMBOFox)a.playSound(s.pose>0?"mbo:entity.fox.sleep":"mbo:entity.fox.ambient",1,1);
    if(a instanceof EntityMBOAxolotl)a.playSound(a.isInWater()?"mbo:entity.axolotl.idle_water":"mbo:entity.axolotl.idle_air",1,1);
    pose(s);
  }
  public static void pose(Session s) {
    EntityLivingBase a=s.animal;
    if(s.eatTicks>0&&--s.eatTicks==4)finishGrazing(s);
    if(a instanceof EntityMBOCamel) {
      EntityMBOCamel camel=(EntityMBOCamel)a;
      if(camel.poseTicks()<52)camel.getDataWatcher().updateObject(27,camel.poseTicks()+1);
      camel.getDataWatcher().updateObject(28,s.dashCooldown);
      camel.getEntityData().setInteger("MBOCamelDashTicks",s.dashCooldown);
    }
    if(s.actionTicks>0&&--s.actionTicks==0){s.pose=0;if(a instanceof EntityHorse)((EntityHorse)a).setRearing(false);}
    if(a instanceof EntityMBOPanda){a.getDataWatcher().updateObject(22,(byte)(s.pose>0?1:0));a.getDataWatcher().updateObject(23,0);}
    if(a instanceof EntityMBOFox)a.getDataWatcher().updateObject(21,(byte)(s.pose>0?1:0));
    if(a instanceof EntityMBOPolarBear)a.getDataWatcher().updateObject(20,(byte)(s.pose>0?1:0));
    if(a instanceof EntityMBOGoat) {
      a.getDataWatcher().updateObject(21,(byte)(s.pose>0?1:0));
    }
    if(a instanceof EntityMBOFrog)a.getDataWatcher().updateObject(21,(byte)(s.pose>0?2:0));
    if(a instanceof EntityMBOAxolotl)a.getDataWatcher().updateObject(21,s.pose>0?100:0);
    if(a instanceof EntityMBOPufferfish&&((EntityMBOPufferfish)a).puff()!=(s.pose>0?2:0))((EntityMBOPufferfish)a).setPuff(s.pose>0?2:0);
    if(a instanceof EntityBat)((EntityBat)a).setIsBatHanging(s.batHanging);
  }
  private static void releaseBat(Session s) {
    s.batTarget=s.batHanging=false;
    ((EntityBat)s.animal).setIsBatHanging(false);
    s.animal.motionX=s.animal.motionZ=0;s.animal.motionY=-.06;
  }
  static void batAction(Session s) {
    if(s.batHanging||s.batTarget){releaseBat(s);return;}
    EntityLivingBase bat=s.animal;
    net.minecraft.util.Vec3 start=net.minecraft.util.Vec3.createVectorHelper(bat.posX,bat.posY+bat.getEyeHeight(),bat.posZ);
    double yaw=Math.toRadians(s.lookYaw),pitch=Math.toRadians(s.lookPitch),cos=Math.cos(pitch);
    net.minecraft.util.Vec3 end=start.addVector(-Math.sin(yaw)*cos*4,-Math.sin(pitch)*4,Math.cos(yaw)*cos*4);
    net.minecraft.util.MovingObjectPosition hit=bat.worldObj.rayTraceBlocks(start,end);
    if(hit==null||hit.sideHit!=0||!bat.worldObj.getBlock(hit.blockX,hit.blockY,hit.blockZ).isNormalCube())return;
    s.batX=hit.blockX;s.batY=hit.blockY;s.batZ=hit.blockZ;
    double half=bat.width*.5D;
    s.batAnchorX=Math.max(s.batX+half,Math.min(s.batX+1-half,hit.hitVec.xCoord));
    s.batAnchorY=s.batY-bat.height;
    s.batAnchorZ=Math.max(s.batZ+half,Math.min(s.batZ+1-half,hit.hitVec.zCoord));
    net.minecraft.util.AxisAlignedBB box=bat.boundingBox.getOffsetBoundingBox(s.batAnchorX-bat.posX,s.batAnchorY-bat.posY,s.batAnchorZ-bat.posZ);
    if(bat.worldObj.getCollidingBoundingBoxes(bat,box).isEmpty()){s.batTarget=true;bat.playSound("mob.bat.idle",.5F,1);}
  }
  static boolean batTravel(Session s) {
    if(!s.batTarget&&!s.batHanging)return false;
    EntityBat bat=(EntityBat)s.animal;
    if(s.jump||!bat.worldObj.getBlock(s.batX,s.batY,s.batZ).isNormalCube()) {
      releaseBat(s);s.jump=false;return true;
    }
    if(s.batHanging) {
      bat.setPosition(s.batAnchorX,s.batAnchorY,s.batAnchorZ);
      bat.motionX=bat.motionY=bat.motionZ=0;bat.setIsBatHanging(true);return true;
    }
    double dx=s.batAnchorX-bat.posX,dy=s.batAnchorY-bat.posY,dz=s.batAnchorZ-bat.posZ;
    double distance=Math.sqrt(dx*dx+dy*dy+dz*dz),factor=distance==0?0:Math.min(1,.12/distance);
    double x=bat.posX,y=bat.posY,z=bat.posZ;
    bat.moveEntity(dx*factor,dy*factor,dz*factor);
    bat.motionX=bat.motionY=bat.motionZ=0;
    if(Math.abs(bat.posX-x-dx*factor)>1e-5||Math.abs(bat.posY-y-dy*factor)>1e-5||Math.abs(bat.posZ-z-dz*factor)>1e-5){releaseBat(s);return true;}
    if(distance<=.12) {
      s.batTarget=false;s.batHanging=true;bat.setIsBatHanging(true);
      bat.setPosition(s.batAnchorX,s.batAnchorY,s.batAnchorZ);
    }
    return true;
  }
  static boolean grassAt(EntityLivingBase a,int x,int y,int z) {
    return a.worldObj.getBlock(x,y,z)==Blocks.tallgrass&&a.worldObj.getBlockMetadata(x,y,z)==1
        ||a.worldObj.getBlock(x,y-1,z)==Blocks.grass;
  }
  static void startGrazing(Session s) {
    EntityLivingBase a=s.animal;if(s.eatTicks>0)return;
    int x=MathHelper.floor_double(a.posX),y=MathHelper.floor_double(a.boundingBox.minY),z=MathHelper.floor_double(a.posZ);
    if(!a.onGround||!grassAt(a,x,y,z)){a.playSound("mob.sheep.say",.6F,1);return;}
    s.eatX=x;s.eatY=y;s.eatZ=z;s.eatTicks=40;s.actionCooldown=40;
    a.worldObj.setEntityState(a,(byte)10);a.playSound("mob.sheep.say",.6F,1);
  }
  public static int sheepMeals(EntityLivingBase sheep) {
    net.minecraft.nbt.NBTTagCompound data=sheep.getEntityData();
    long elapsed=Math.max(0,sheep.worldObj.getTotalWorldTime()-data.getLong("MBOLastGrassMeal"));
    return (int)Math.max(0,data.getInteger("MBOGrassMeals")-Math.min(Integer.MAX_VALUE,elapsed/200));
  }
  static boolean recordMeal(EntitySheep sheep) {
    int meals=sheepMeals(sheep)+1;
    sheep.getEntityData().setInteger("MBOGrassMeals",meals);
    sheep.getEntityData().setLong("MBOLastGrassMeal",sheep.worldObj.getTotalWorldTime());
    return meals>=SHEEP_MEAL_LIMIT;
  }
  static void finishGrazing(Session s) {
    EntitySheep sheep=(EntitySheep)s.animal;int x=s.eatX,y=s.eatY,z=s.eatZ;
    if(MathHelper.floor_double(sheep.posX)!=x||MathHelper.floor_double(sheep.boundingBox.minY)!=y
        ||MathHelper.floor_double(sheep.posZ)!=z||!grassAt(sheep,x,y,z))return;
    if(sheep.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")) {
      Block block=sheep.worldObj.getBlock(x,y,z);
      if(block==Blocks.tallgrass&&sheep.worldObj.getBlockMetadata(x,y,z)==1)sheep.worldObj.setBlockToAir(x,y,z);
      else {sheep.worldObj.playAuxSFX(2001,x,y-1,z,Block.getIdFromBlock(Blocks.grass));sheep.worldObj.setBlock(x,y-1,z,Blocks.dirt,0,3);}
    }
    sheep.eatGrassBonus();sheep.playSound("dig.grass",1,1);
    if(recordMeal(sheep)) {
      if(sheep.worldObj instanceof net.minecraft.world.WorldServer)((net.minecraft.world.WorldServer)sheep.worldObj).func_147487_a("largeexplode",sheep.posX,sheep.posY+.4,sheep.posZ,1,0,0,0,0);
      sheep.playSound("random.explode",.7F,1.4F);
      sheep.attackEntityFrom(DamageSource.magic,sheep.getMaxHealth()*10);
    }
  }
  public static void reset(Session s){s.pose=0;s.actionTicks=0;s.eatTicks=0;s.batTarget=s.batHanging=false;pose(s);if(s.animal instanceof EntityHorse)((EntityHorse)s.animal).setRearing(false);}
}
