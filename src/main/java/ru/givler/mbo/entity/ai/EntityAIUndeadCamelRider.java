package ru.givler.mbo.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
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
    camel.getNavigator().clearPathEntity();
  }

  @Override
  public void updateTask() {
    EntityLivingBase target = ((EntityMBOHusk) camel.riddenByEntity).getAttackTarget();
    if (target == null || !target.isEntityAlive()) {
      camel.getNavigator().clearPathEntity();
      return;
    }
    camel.getLookHelper().setLookPositionWithEntity(target, 30F, 30F);
    if (--pathTicks <= 0) {
      camel.getNavigator().tryMoveToEntityLiving(target, 1D);
      pathTicks = 10;
    }
  }
}
