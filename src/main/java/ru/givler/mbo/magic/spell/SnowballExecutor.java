package ru.givler.mbo.magic.spell;

import net.minecraft.entity.projectile.EntitySnowball;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class SnowballExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    if (!context.world().isRemote) {
      context.world().spawnEntityInWorld(new EntitySnowball(context.world(), context.caster()));
      context
          .world()
          .playSoundAtEntity(
              context.caster(),
              "random.bow",
              0.5F,
              0.4F / (context.world().rand.nextFloat() * 0.4F + 0.8F));
      context.caster().swingItem();
    }
    return SpellResult.SUCCESS;
  }
}
