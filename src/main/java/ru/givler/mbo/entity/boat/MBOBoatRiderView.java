package ru.givler.mbo.entity.boat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public final class MBOBoatRiderView {
  private MBOBoatRiderView() {}

  public static void clampView(Entity rider) {
    if (!(rider.ridingEntity instanceof EntityMBOBoat)) return;
    EntityMBOBoat boat = (EntityMBOBoat) rider.ridingEntity;
    if (boat.riddenByEntity != rider) return;
    float yaw = boat.rotationYaw + MathHelper.clamp_float(
        MathHelper.wrapAngleTo180_float(rider.rotationYaw - boat.rotationYaw), -90F, 90F);
    rider.prevRotationYaw += MathHelper.wrapAngleTo180_float(yaw - rider.rotationYaw);
    rider.rotationYaw = yaw;
    if (rider instanceof EntityLivingBase) {
      EntityLivingBase living = (EntityLivingBase) rider;
      living.rotationYawHead = yaw;
      living.renderYawOffset = boat.rotationYaw;
    }
  }
}
