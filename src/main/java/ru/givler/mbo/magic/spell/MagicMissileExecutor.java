package ru.givler.mbo.magic.spell;

import ru.givler.mbo.entity.magic.EntityMagicMissile;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

/** Launches a short-lived arcane projectile from the caster's view. */
public final class MagicMissileExecutor implements SpellExecutor {
  private static final float BASE_SPEED = 2.0F;

  @Override
  public SpellResult cast(SpellContext context) {
    if (!context.world().isRemote) {
      float speed = BASE_SPEED * positiveOrOne(context.range());
      float damage = EntityMagicMissile.BASE_DAMAGE * positiveOrOne(context.power());
      EntityMagicMissile missile =
          context.target() == null
              ? new EntityMagicMissile(context.world(), context.caster(), speed, damage)
              : new EntityMagicMissile(
                  context.world(), context.caster(), context.target(), speed, damage);
      context.world().spawnEntityInWorld(missile);
    }

    context.caster().swingItem();
    context
        .world()
        .playSoundAtEntity(
            context.caster(), "mbo:magic", 1.0F, 1.2F + context.world().rand.nextFloat() * 0.4F);
    return SpellResult.SUCCESS;
  }

  private static float positiveOrOne(float value) {
    return value > 0.0F ? value : 1.0F;
  }
}
