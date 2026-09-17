package ru.givler.mbo.magic.spell;

import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class HealExecutor implements SpellExecutor {
  private final float amount;

  public HealExecutor() {
    this(4.0F);
  }

  public HealExecutor(float amount) {
    this.amount = amount;
  }

  @Override
  public SpellResult cast(SpellContext context) {
    if (context.caster().getHealth() >= context.caster().getMaxHealth()) return SpellResult.PASS;
    if (!context.world().isRemote) {
      context.caster().heal(amount * context.power());
      SpellEffects.sparkleBurst(context.caster(), 10, 1.0F, 1.0F, 0.3F);
      context
          .world()
          .playSoundAtEntity(
              context.caster(), "mbo:heal", 0.7F, 1.0F + context.world().rand.nextFloat() * 0.4F);
    }
    return SpellResult.SUCCESS;
  }
}
