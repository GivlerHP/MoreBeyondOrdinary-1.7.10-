package ru.givler.mbo.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import ru.givler.mbo.entity.fauna.EntityMBOCamelHusk;
import ru.givler.mbo.entity.monster.EntityMBOHusk;

/** Aim melee at the victim's upper body instead of their feet below the mount. */
public final class EntityAIMountedHuskAttack extends EntityAIBase {
  private final EntityMBOHusk rider;
  private int cooldown;

  public EntityAIMountedHuskAttack(EntityMBOHusk rider) {
    this.rider = rider;
    setMutexBits(3);
  }

  @Override
  public boolean shouldExecute() {
    return rider.ridingEntity instanceof EntityMBOCamelHusk
        && rider.getAttackTarget() != null
        && rider.getAttackTarget().isEntityAlive();
  }

  @Override
  public void updateTask() {
    EntityLivingBase target = rider.getAttackTarget();
    if (target == null) return;
    rider.getLookHelper().setLookPositionWithEntity(target, 30F, 30F);
    if (cooldown > 0) cooldown--;
    double reach = rider.width * 2D;
    if (cooldown == 0
        && rider.getDistanceSq(target.posX, target.boundingBox.maxY, target.posZ)
            <= reach * reach + target.width
        && rider.getEntitySenses().canSee(target)) {
      cooldown = 20;
      rider.swingItem();
      rider.attackEntityAsMob(target);
    }
  }
}
