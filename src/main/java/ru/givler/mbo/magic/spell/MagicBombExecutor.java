package ru.givler.mbo.magic.spell;

import ru.givler.mbo.entity.magic.EntityMagicBomb;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class MagicBombExecutor implements SpellExecutor {
  private final EntityMagicBomb.Kind kind;

  public MagicBombExecutor(EntityMagicBomb.Kind kind) {
    this.kind = kind;
  }

  @Override
  public SpellResult cast(SpellContext context) {
    if (!context.world().isRemote) {
      context
          .world()
          .spawnEntityInWorld(
              new EntityMagicBomb(
                  context.world(), context.caster(), kind, context.power(), context.area()));
      if (kind == EntityMagicBomb.Kind.DARKNESS)
        context
            .world()
            .playSoundAtEntity(
                context.caster(),
                "mob.wither.shoot",
                1,
                0.4F / (context.world().rand.nextFloat() * 0.4F + 0.8F));
    }
    if (kind != EntityMagicBomb.Kind.DARKNESS)
      context
          .world()
          .playSoundAtEntity(
              context.caster(),
              "random.bow",
              0.5F,
              0.4F / (context.world().rand.nextFloat() * 0.4F + 0.8F));
    context.caster().swingItem();
    return SpellResult.SUCCESS;
  }
}
