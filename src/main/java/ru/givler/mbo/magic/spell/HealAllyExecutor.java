package ru.givler.mbo.magic.spell;

import net.minecraft.entity.EntityLivingBase;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class HealAllyExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    EntityLivingBase target = context.target();
    if (target == null) {
      target = SpellTargeting.livingInSight(context.caster(), 10.0D * context.range());
    }
    if (target == null || target.getHealth() >= target.getMaxHealth()) return SpellResult.PASS;
    if (!context.world().isRemote) {
      target.heal(5.0F * context.power());
      SpellEffects.sparkleBurst(target, 10, 1.0F, 1.0F, 0.3F);
      context
          .world()
          .playSoundAtEntity(
              target, "mbo:heal", 0.7F, 1.0F + context.world().rand.nextFloat() * 0.4F);
      context.caster().swingItem();
    }
    return SpellResult.SUCCESS;
  }
}
