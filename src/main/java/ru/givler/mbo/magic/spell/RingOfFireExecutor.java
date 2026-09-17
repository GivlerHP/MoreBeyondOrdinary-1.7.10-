package ru.givler.mbo.magic.spell;

import ru.givler.mbo.entity.magic.EntityGroundMagicEffect;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class RingOfFireExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    if (!context.caster().onGround) return SpellResult.PASS;
    if (!context.world().isRemote) {
      context
          .world()
          .spawnEntityInWorld(
              new EntityGroundMagicEffect(
                  context.world(),
                  context.caster().posX,
                  context.caster().posY,
                  context.caster().posZ,
                  context.caster(),
                  EntityGroundMagicEffect.Kind.FIRE_RING,
                  Math.max(1, (int) (600 * context.duration())),
                  context.power()));
    }
    context.world().playSoundAtEntity(context.caster(), "fire.fire", 1.5F, 0.8F);
    context.caster().swingItem();
    return SpellResult.SUCCESS;
  }
}
