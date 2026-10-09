package ru.givler.mbo.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;
import ru.givler.mbo.entity.fauna.EntityMBOCamelHusk;
import ru.givler.mbo.entity.monster.EntityMBOHusk;

/** The mount follows the hostile rider's target through the normal horse navigator. */
public final class EntityAIUndeadCamelRider extends EntityAIBase {
  private final EntityMBOCamelHusk camel;
  private int pathTicks;

  public EntityAIUndeadCamelRider(EntityMBOCamelHusk camel) {
    this.camel = camel;
    setMutexBits(3);
  }

  @Override
  public boolean shouldExecute() {
    return camel.riddenByEntity instanceof EntityMBOHusk;
  }

  @Override
  public void resetTask() {
    pathTicks = 0;
    camel.setSprinting(false);
    camel.getNavigator().clearPathEntity();
  }

  @Override
  public void updateTask() {
    EntityLivingBase target = ((EntityMBOHusk) camel.riddenByEntity).getAttackTarget();
    if (target == null || !target.isEntityAlive()) {
      camel.getNavigator().clearPathEntity();
      camel.setSprinting(false);
      return;
    }
    camel.setSprinting(true);
    camel.updateCamelWalkingSpeed(true);
    if (camel.sitting()) {
      if (camel
          .worldObj
          .getCollidingBoundingBoxes(
              camel, camel.boundingBox.addCoord(0, 1.43D, 0).contract(.001D, .001D, .001D))
          .isEmpty()) camel.setSitting(false);
      return;
    }
    if (camel.transitioning()) return;
    camel.getLookHelper().setLookPositionWithEntity(target, 30F, 30F);
    tryCharge(target);
    if (--pathTicks <= 0) {
      camel.getNavigator().tryMoveToEntityLiving(target, 1.25D);
      pathTicks = 10;
    }
  }

  /** A short forward attack using ordinary movement/collisions, never teleportation. */
  public boolean tryCharge(EntityLivingBase target) {
    if (!camel.onGround
        || camel.dashCooldown() > 0
        || camel.sitting()
        || camel.transitioning()
        || !camel.getEntitySenses().canSee(target)) return false;
    double dx = target.posX - camel.posX, dz = target.posZ - camel.posZ;
    double distance = Math.sqrt(dx * dx + dz * dz);
    if (distance < 3D
        || distance > 8D
        || Math.abs(target.boundingBox.minY - camel.boundingBox.minY) > 1D) return false;
    dx /= distance;
    dz /= distance;
    double angle = camel.rotationYaw * Math.PI / 180D;
    if (-Math.sin(angle) * dx + Math.cos(angle) * dz < .96D) return false;
    if (!clearChargePath(dx, dz)) return false;
    camel.motionX = dx * camel.getCamelDashSpeed() * .65D;
    camel.motionZ = dz * camel.getCamelDashSpeed() * .65D;
    camel.motionY = .25D;
    camel.onGround = false;
    camel.setHorseJumping(true);
    camel.beginDash();
    camel.velocityChanged = true;
    return true;
  }

  public boolean clearChargePath(double dx, double dz) {
    int y = MathHelper.floor_double(camel.boundingBox.minY) - 1;
    for (double step = .5D; step <= 5D; step += .5D) {
      if (!camel
          .worldObj
          .getCollidingBoundingBoxes(
              camel, camel.boundingBox.getOffsetBoundingBox(dx * step, .25D, dz * step))
          .isEmpty()) return false;
      for (int side = -1; side <= 1; side++) {
        double offset = side * camel.width * .45D;
        int x = MathHelper.floor_double(camel.posX + dx * step + dz * offset);
        int z = MathHelper.floor_double(camel.posZ + dz * step - dx * offset);
        if (!camel.worldObj.blockExists(x, y, z)
            || !camel.worldObj.getBlock(x, y, z).getMaterial().isSolid()) return false;
      }
    }
    return true;
  }
}
