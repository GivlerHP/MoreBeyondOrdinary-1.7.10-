package ru.givler.mbo.entity.fauna;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import ru.givler.mbo.core.WaterloggingEntityHooks;

/** Shared steering without snapping across the -180/180 degree boundary. */
public final class AquaticMovement {
  private AquaticMovement() {}

  public static AxisAlignedBB waterBox(AxisAlignedBB box) {
    return box.contract(.001, .001, .001);
  }

  public static boolean updateWaterContact(Entity animal) {
    boolean wet =
        animal.worldObj.handleMaterialAcceleration(
                waterBox(animal.boundingBox), Material.water, animal)
            || WaterloggingEntityHooks.touchesWaterlogged(animal);
    if (wet) {
      animal.fallDistance = 0;
      animal.extinguish();
    }
    return wet;
  }

  /** Rendered body angle is independent of vanilla's body helper and packet arrival cadence. */
  public static final class ClientTurn {
    private boolean initialized;
    private float bodyYaw;

    public void update(EntityLivingBase animal) {
      if (!animal.worldObj.isRemote) return;
      if (!initialized) {
        bodyYaw = animal.rotationYaw;
        initialized = true;
      }
      float previous = bodyYaw;
      bodyYaw = approachYaw(bodyYaw, animal.rotationYaw, 3F);
      animal.prevRenderYawOffset = previous;
      animal.renderYawOffset = bodyYaw;
      animal.prevRotationYawHead = previous;
      animal.rotationYawHead = bodyYaw;
    }
  }

  public static float approachYaw(float current, float target, float maximumStep) {
    return current
        + MathHelper.clamp_float(
            MathHelper.wrapAngleTo180_float(target - current), -maximumStep, maximumStep);
  }

  /** Turn toward the goal first, then propel along the body's forward axis. */
  public static void swim(
      EntityLivingBase animal, double x, double z, double acceleration, float maximumStep) {
    if (x * x + z * z < .000001) return;
    face(animal, x, z, maximumStep);
    double radians = animal.rotationYaw * Math.PI / 180;
    double forwardX = -Math.sin(radians), forwardZ = Math.cos(radians);
    double speed = Math.max(0, animal.motionX * forwardX + animal.motionZ * forwardZ);
    float target = (float) (Math.atan2(-x, z) * 180 / Math.PI);
    double alignment =
        Math.max(
            0,
            Math.cos(MathHelper.wrapAngleTo180_float(target - animal.rotationYaw) * Math.PI / 180));
    speed = (speed + acceleration) * (.8 + .2 * alignment);
    animal.motionX = forwardX * speed;
    animal.motionZ = forwardZ * speed;
  }

  public static void face(EntityLivingBase animal, double x, double z, float maximumStep) {
    if (x * x + z * z < .000001) return;
    float target = (float) (Math.atan2(-x, z) * 180 / Math.PI);
    animal.rotationYaw = approachYaw(animal.rotationYaw, target, maximumStep);
    animal.renderYawOffset = animal.rotationYaw;
    animal.rotationYawHead = animal.rotationYaw;
  }
}
