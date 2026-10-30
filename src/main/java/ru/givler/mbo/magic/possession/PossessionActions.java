package ru.givler.mbo.magic.possession;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.*;
import ru.givler.mbo.entity.fauna.*;
import ru.givler.mbo.magic.possession.PossessionManager.Session;

/** Cosmetic actions deliberately bypass feeding, drops, combat and block modification. */
public final class PossessionActions {
  private PossessionActions(){}
  public static void action(Session s) {
    EntityLivingBase a=s.animal;
    if(a instanceof EntitySheep){a.worldObj.setEntityState(a,(byte)10);return;}
    if(a instanceof EntityMBOCamel){EntityMBOCamel c=(EntityMBOCamel)a;if(c.sitting()&&c.canStand()||!c.sitting())c.setSitting(!c.sitting());return;}
    if(a instanceof EntityHorse){((EntityHorse)a).setRearing(true);s.actionTicks=30;return;}
    if(a instanceof EntityMBOPolarBear||a instanceof EntityMBOGoat||a instanceof EntityMBOFrog){s.pose=1;s.actionTicks=40;}
    else if(a instanceof EntityMBOPanda||a instanceof EntityMBOFox||a instanceof EntityMBOAxolotl){s.pose=s.pose==0?1:0;}
    else if(a instanceof EntityMBOPufferfish){s.pose=s.pose==0?1:0;}
    else if(a instanceof EntityMBOGlowSquid) {
      for(int i=0;i<12;i++)((net.minecraft.world.WorldServer)a.worldObj).func_147487_a("happyVillager",a.posX,a.posY+.3,a.posZ,1,.4,.4,.4,0);
      a.playSound("mbo:entity.glow_squid.squirt",1,1);
    }
    if(a instanceof EntityMBOFrog)a.playSound("mbo:entity.frog.ambient",1,1);
    pose(s);
  }
  public static void pose(Session s) {
    EntityLivingBase a=s.animal;
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
    if(a instanceof EntityBat)((EntityBat)a).setIsBatHanging(false);
  }
  public static void dash(Session s) {
    EntityMBOCamel c=(EntityMBOCamel)s.animal;
    if(s.dashCooldown>0||c.sitting()||c.transitioning())return;
    double angle=Math.toRadians(c.rotationYaw);
    c.motionX=-Math.sin(angle)*c.getCamelDashSpeed();c.motionZ=Math.cos(angle)*c.getCamelDashSpeed();c.motionY=.42;
    s.dashCooldown=55;c.beginDash();
  }
  public static void reset(Session s){s.pose=0;s.actionTicks=0;pose(s);if(s.animal instanceof EntityHorse)((EntityHorse)s.animal).setRearing(false);}
}
