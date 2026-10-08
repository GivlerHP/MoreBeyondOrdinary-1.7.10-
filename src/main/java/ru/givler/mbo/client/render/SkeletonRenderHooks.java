package ru.givler.mbo.client.render;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySkeleton;
import ru.givler.mbo.entity.ai.EntityAISkeletonBowAttack;
import ru.givler.mbo.entity.ai.SkeletonCombatState;

public final class SkeletonRenderHooks {
  private SkeletonRenderHooks() {}

  public static boolean prepare(ModelBiped model, Entity entity) {
    if (!(entity instanceof EntitySkeleton)) return true;
    EntitySkeleton skeleton = (EntitySkeleton) entity;
    boolean fighting = SkeletonCombatState.fighting(skeleton);
    model.aimedBow =
        fighting
            && skeleton.getHeldItem() != null
            && EntityAISkeletonBowAttack.isBow(skeleton.getHeldItem().getItem());
    // ModelZombie's raised melee arms would overwrite ModelBiped's bow aiming.
    return fighting && !model.aimedBow;
  }
}
