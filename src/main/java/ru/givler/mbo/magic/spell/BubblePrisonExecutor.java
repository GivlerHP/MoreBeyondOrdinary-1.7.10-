package ru.givler.mbo.magic.spell;

import net.minecraft.entity.EntityLivingBase;
import ru.givler.mbo.entity.magic.EntityMagicBubble;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

/** Shared targeting and floating-prison behaviour for Bubble and Entrapment. */
public final class BubblePrisonExecutor implements SpellExecutor {
  private final boolean dark;

  public BubblePrisonExecutor(boolean dark) {
    this.dark = dark;
  }

  @Override
  public SpellResult cast(SpellContext context) {
    EntityLivingBase target =
        context.target() != null
            ? context.target()
            : SpellTargeting.livingInSight(context.caster(), 10 * context.range());
    if (target == null || target.ridingEntity instanceof EntityMagicBubble) return SpellResult.PASS;
    if (!context.world().isRemote) {
      target.attackEntityFrom(
          new net.minecraft.util.EntityDamageSource(
                  dark ? "mbo.entrapment" : "mbo.bubble", context.caster())
              .setMagicDamage(),
          dark ? context.power() : context.power());
      EntityMagicBubble bubble =
          new EntityMagicBubble(
              context.world(),
              target,
              context.caster(),
              Math.max(1, (int) (200 * context.duration())),
              dark,
              context.power());
      context.world().spawnEntityInWorld(bubble);
      target.mountEntity(bubble);
    } else {
      for (int i = 1; i < (int) (25 * context.range()); i += 2) {
        double f = i / 20.0D;
        double x = context.caster().posX + (target.posX - context.caster().posX) * f;
        double y =
            context.caster().posY
                + context.caster().getEyeHeight()
                + (target.posY
                        + target.height / 2
                        - context.caster().posY
                        - context.caster().getEyeHeight())
                    * f;
        double z = context.caster().posZ + (target.posZ - context.caster().posZ) * f;
        context
            .world()
            .spawnParticle(dark ? "portal" : "splash", x, y - (dark ? .5D : 0), z, 0, 0, 0);
        if (dark)
          ru.givler.mbo.MoreBeyondOrdinary.proxy.spawnParticle(
              ru.givler.mbo.particles.EnumParticleType.DARK_MAGIC,
              context.world(),
              x,
              y,
              z,
              0,
              0,
              0,
              new ru.givler.mbo.particles.ParticleSettings(24, .1F, 0, 0, .8F, false));
      }
    }
    context
        .world()
        .playSoundAtEntity(
            context.caster(),
            dark ? "mob.wither.shoot" : "game.neutral.swim",
            1.0F,
            0.9F + context.world().rand.nextFloat() * 0.2F);
    context.caster().swingItem();
    return SpellResult.SUCCESS;
  }
}
