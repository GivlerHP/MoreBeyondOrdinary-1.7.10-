package ru.givler.mbo.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;

/** Follow the current target instead of limiting melee revenge to players. */
public final class EntityAISkeletonMeleeAttack extends EntityAIAttackOnCollide {
  private final EntityCreature skeleton;
  private final double speed;
  private int attackCooldown, pathCooldown;

  public EntityAISkeletonMeleeAttack(
      EntityCreature skeleton, Class targetClass, double speed, boolean memory) {
    super(skeleton, speed, memory);
    this.skeleton = skeleton;
    this.speed = speed;
  }

  @Override
  public boolean shouldExecute() {
    return (skeleton.getHeldItem() == null
            || !EntityAISkeletonBowAttack.isBow(skeleton.getHeldItem().getItem()))
        && super.shouldExecute();
  }

  @Override
  public boolean continueExecuting() {
    return (skeleton.getHeldItem() == null
            || !EntityAISkeletonBowAttack.isBow(skeleton.getHeldItem().getItem()))
        && super.continueExecuting();
  }

  @Override
  public void updateTask() {
    EntityLivingBase target = skeleton.getAttackTarget();
    if (target == null || !target.isEntityAlive()) return;
    skeleton.getLookHelper().setLookPositionWithEntity(target, 30F, 30F);
    if (--pathCooldown <= 0) {
      skeleton.getNavigator().tryMoveToEntityLiving(target, speed);
      pathCooldown = 4 + skeleton.getRNG().nextInt(7);
    }
    if (attackCooldown > 0) attackCooldown--;
    double reach = skeleton.width * 2D;
    double distance = skeleton.getDistanceSq(target.posX, target.boundingBox.minY, target.posZ);
    if (attackCooldown == 0
        && distance <= reach * reach + target.width
        && skeleton.getEntitySenses().canSee(target)) {
      attackCooldown = 20;
      skeleton.swingItem();
      skeleton.attackEntityAsMob(target);
    }
  }
}
