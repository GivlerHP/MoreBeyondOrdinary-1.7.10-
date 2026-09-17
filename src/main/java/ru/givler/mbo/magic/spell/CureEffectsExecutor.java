package ru.givler.mbo.magic.spell;

import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class CureEffectsExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    if (context.caster().getActivePotionEffects().isEmpty()) return SpellResult.PASS;
    if (!context.world().isRemote) {
      context.caster().clearActivePotions();
      SpellEffects.sparkleBurst(context.caster(), 10, 0.6F, 0.6F, 1.0F);
      context
          .world()
          .playSoundAtEntity(
              context.caster(), "mbo:heal", 0.7F, 1.0F + context.world().rand.nextFloat() * 0.4F);
    }
    return SpellResult.SUCCESS;
  }
}
