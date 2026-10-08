package ru.givler.mbo.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.world.EnumDifficulty;

/** 26.3 bow-goal timing and strafing, adapted to the 1.7.10 movement controls. */
public final class EntityAISkeletonBowAttack extends EntityAIArrowAttack {
  private final EntitySkeleton skeleton;
  private final double speed;
  private final float radiusSquared;
  private int seeTime;
  private int attackTime = -1;
  private int drawTime = -1;
  private int strafingTime = -1;
  private int pathTime;
  private boolean clockwise;
  private boolean backwards;
  private EntityLivingBase previousTarget;

  public EntityAISkeletonBowAttack(
      IRangedAttackMob mob, double speed, int min, int max, float radius) {
    super(mob, speed, min, max, radius);
    skeleton = (EntitySkeleton) mob;
    this.speed = speed;
    radiusSquared = radius * radius;
    setMutexBits(3);
  }

  public static boolean isBow(Item item) {
    return item instanceof ItemBow;
  }

  @Override
  public boolean shouldExecute() {
    EntityLivingBase target = skeleton.getAttackTarget();
    return target != null
        && target.isEntityAlive()
        && skeleton.getHeldItem() != null
        && isBow(skeleton.getHeldItem().getItem());
  }

  @Override
  public boolean continueExecuting() {
    return shouldExecute();
  }

  @Override
  public void startExecuting() {
    SkeletonMoveHelper.install(skeleton);
  }

  @Override
  public void resetTask() {
    seeTime = 0;
    attackTime = drawTime = strafingTime = -1;
    pathTime = 0;
    previousTarget = null;
    skeleton.moveStrafing = 0F;
    if (skeleton.getMoveHelper() instanceof SkeletonMoveHelper)
      ((SkeletonMoveHelper) skeleton.getMoveHelper()).cancelStrafe();
  }

  @Override
  public void updateTask() {
    if (!shouldExecute()) return;
    EntityLivingBase target = skeleton.getAttackTarget();
    if (target != previousTarget) {
      seeTime = 0;
      drawTime = strafingTime = -1;
      pathTime = 0;
      previousTarget = target;
    }
    double distance = skeleton.getDistanceSq(target.posX, target.boundingBox.minY, target.posZ);
    boolean visible = skeleton.getEntitySenses().canSee(target);
    if (visible != (seeTime > 0)) seeTime = 0;
    if (skeleton.getMoveHelper() instanceof SkeletonMoveHelper
        && ((SkeletonMoveHelper) skeleton.getMoveHelper()).consumeBlockedStrafe()) {
      seeTime = 0;
      pathTime = 0;
    }
    seeTime += visible ? 1 : -1;

    if (distance <= radiusSquared && seeTime >= 20) {
      skeleton.getNavigator().clearPathEntity();
      strafingTime++;
    } else {
      if (--pathTime <= 0) {
        skeleton.getNavigator().tryMoveToEntityLiving(target, speed);
        pathTime = 10;
      }
      strafingTime = -1;
    }
    if (strafingTime >= 20) {
      if (skeleton.getRNG().nextFloat() < .3F) clockwise = !clockwise;
      if (skeleton.getRNG().nextFloat() < .3F) backwards = !backwards;
      strafingTime = 0;
    }
    if (strafingTime >= 0) {
      if (distance > radiusSquared * .75F) backwards = false;
      else if (distance < radiusSquared * .25F) backwards = true;
      if (skeleton.getMoveHelper() instanceof SkeletonMoveHelper)
        ((SkeletonMoveHelper) skeleton.getMoveHelper())
            .strafe(backwards ? -.5F : .5F, clockwise ? .5F : -.5F, target);
      else if (--pathTime <= 0) {
        // Retain a specialized movement controller supplied by another mod.
        double dx = target.posX - skeleton.posX, dz = target.posZ - skeleton.posZ;
        double length = Math.max(1D, Math.sqrt(dx * dx + dz * dz));
        double side = clockwise ? 1D : -1D, forward = backwards ? -1D : 1D;
        skeleton
            .getNavigator()
            .tryMoveToXYZ(
                skeleton.posX + (dx * forward + dz * side) / length,
                skeleton.posY,
                skeleton.posZ + (dz * forward - dx * side) / length,
                speed * .5D);
        pathTime = 10;
      }
    }
    skeleton.getLookHelper().setLookPositionWithEntity(target, 30F, 30F);
    if (drawTime >= 0) {
      drawTime++;
      if (!visible && seeTime < -60) drawTime = -1;
      else if (visible && drawTime >= 20) {
        drawTime = -1;
        // Keep vanilla/Forge/MF2 enchantments, projectile creation and champion damage hooks.
        skeleton.attackEntityWithRangedAttack(target, 1F);
        attackTime = skeleton.worldObj.difficultySetting == EnumDifficulty.HARD ? 20 : 40;
      }
    } else if (--attackTime <= 0 && seeTime >= -60) drawTime = 0;
  }
}
