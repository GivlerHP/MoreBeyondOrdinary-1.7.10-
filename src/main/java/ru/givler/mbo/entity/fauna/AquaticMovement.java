package ru.givler.mbo.entity.fauna;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

/** Shared steering without snapping across the -180/180 degree boundary. */
public final class AquaticMovement {
  private AquaticMovement() {}

  public static float approachYaw(float current, float target, float maximumStep) {
    return current
        + MathHelper.clamp_float(
            MathHelper.wrapAngleTo180_float(target - current), -maximumStep, maximumStep);
  }

  public static void face(EntityLivingBase animal, double x, double z, float maximumStep) {
    if (x * x + z * z < .000001) return;
    float target = (float) (Math.atan2(-x, z) * 180 / Math.PI);
    animal.rotationYaw = approachYaw(animal.rotationYaw, target, maximumStep);
    animal.renderYawOffset = animal.rotationYaw;
    animal.rotationYawHead = animal.rotationYaw;
  }
}
