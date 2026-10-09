package ru.givler.mbo.entity.monster;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFleeSun;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import ru.givler.mbo.entity.fauna.EntityMBOCamel;
import ru.givler.mbo.entity.fauna.EntityMBOCamelSeat;

public final class EntityMBOParched extends EntityMBOVariantSkeleton {
  @Override
  public void setPositionAndRotation2(
      double x, double y, double z, float yaw, float pitch, int ticks) {
    if (ridingEntity instanceof EntityMBOCamelSeat) {
      rotationYaw = yaw;
      rotationPitch = pitch;
      ridingEntity.updateRiderPosition();
    } else super.setPositionAndRotation2(x, y, z, yaw, pitch, ticks);
  }

  @Override
  public void attackEntityWithRangedAttack(EntityLivingBase target, float strength) {
    if (ridingEntity instanceof EntityMBOCamelSeat) {
      EntityMBOCamel camel = ((EntityMBOCamelSeat) ridingEntity).getCamel();
      if (camel != null && camel.riddenByEntity != null) {
        Vec3 start = Vec3.createVectorHelper(posX, posY + getEyeHeight() - .1D, posZ);
        Vec3 end =
            Vec3.createVectorHelper(
                target.posX, target.boundingBox.minY + target.height / 3D, target.posZ);
        if (camel.riddenByEntity.boundingBox.expand(.4D, .4D, .4D).calculateIntercept(start, end)
            != null) return;
      }
    }
    super.attackEntityWithRangedAttack(target, strength);
  }

  public EntityMBOParched(World world) {
    super(world);
    List<EntityAIBase> sunlight = new ArrayList<EntityAIBase>();
    for (Object raw : tasks.taskEntries) {
      EntityAIBase task = ((EntityAITaskEntry) raw).action;
      if (task instanceof EntityAIFleeSun || task instanceof EntityAIRestrictSun)
        sunlight.add(task);
    }
    for (EntityAIBase task : sunlight) tasks.removeTask(task);
  }

  @Override
  public String variant() {
    return "parched";
  }

  @Override
  protected double variantHealth() {
    return 16D;
  }

  @Override
  public PotionEffect arrowEffect() {
    return new PotionEffect(Potion.weakness.id, 600);
  }

  @Override
  public int bowCooldown(boolean hard) {
    return hard ? 50 : 70;
  }

  @Override
  public boolean isPotionApplicable(PotionEffect effect) {
    return effect.getPotionID() != Potion.weakness.id && super.isPotionApplicable(effect);
  }
}
