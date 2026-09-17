package ru.givler.mbo.magic.spell;

import ru.givler.mbo.entity.magic.EntityArcaneArrow;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class ArcaneArrowExecutor implements SpellExecutor {
  private final EntityArcaneArrow.Kind kind;
  private final float speed;

  public ArcaneArrowExecutor(EntityArcaneArrow.Kind kind, float speed) {
    this.kind = kind;
    this.speed = speed;
  }

  @Override
  public SpellResult cast(SpellContext context) {
    if (!context.world().isRemote)
      context
          .world()
          .spawnEntityInWorld(
              new EntityArcaneArrow(
                  context.world(),
                  context.caster(),
                  kind,
                  speed * context.range(),
                  context.power()));
    if (kind == EntityArcaneArrow.Kind.DART)
      context
          .world()
          .playSoundAtEntity(
              context.caster(),
              "random.bow",
              0.5F,
              0.4F / (context.world().rand.nextFloat() * 0.4F + 0.8F));
    if (kind == EntityArcaneArrow.Kind.FORCE) {
      context.world().playSoundAtEntity(context.caster(), "mbo:aura", 1.0F, 1.4F);
      context.world().playSoundAtEntity(context.caster(), "mbo:magic", 1.0F, 1.2F);
    }
    if (kind == EntityArcaneArrow.Kind.ICE_LANCE)
      context
          .world()
          .playSoundAtEntity(
              context.caster(), "mbo:ice", 1.0F, context.world().rand.nextFloat() * 0.4F + 0.8F);
    if (kind == EntityArcaneArrow.Kind.LIGHTNING)
      context
          .world()
          .playSoundAtEntity(
              context.caster(),
              "mbo:electricitya",
              1.0F,
              context.world().rand.nextFloat() * 0.3F + 1.3F);
    context.caster().swingItem();
    return SpellResult.SUCCESS;
  }
}
