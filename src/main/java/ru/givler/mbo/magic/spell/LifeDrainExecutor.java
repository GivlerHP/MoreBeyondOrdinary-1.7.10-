package ru.givler.mbo.magic.spell;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntityDamageSource;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;

public final class LifeDrainExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    EntityLivingBase target = context.target();
    if (target == null)
      target = SpellTargeting.livingInSight(context.caster(), 10.0D * context.range());
    if (target == null) return SpellResult.PASS;
    if (context.world().isRemote) {
      drawDrain(context, target);
    } else if (context.ticksInUse() % 12 == 0) {
      boolean hurt =
          target.attackEntityFrom(
              new EntityDamageSource("mbo.lifeDrain", context.caster()).setMagicDamage(),
              2.0F * context.power());
      if (hurt) context.caster().heal(1.0F);
    }
    if (context.ticksInUse() % 18 == 0) {
      if (context.ticksInUse() == 0)
        context.world().playSoundAtEntity(context.caster(), "mbo:darkaura", 1.0F, 0.6F);
      context.world().playSoundAtEntity(context.caster(), "mbo:crackle", 2.0F, 1.0F);
    }
    return SpellResult.SUCCESS;
  }

  private static void drawDrain(SpellContext context, EntityLivingBase target) {
    double sx = context.caster().posX, sy = context.caster().posY + context.caster().getEyeHeight();
    double sz = context.caster().posZ;
    double dx = target.posX - sx,
        dy = target.posY + target.height * 0.5D - sy,
        dz = target.posZ - sz;
    double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
    int steps = Math.max(1, (int) (length * 2.0D));
    for (int i = 1; i <= steps; i++) {
      double f = i / (double) steps;
      double x = sx + dx * f, y = sy + dy * f, z = sz + dz * f;
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.DARK_MAGIC,
          context.world(),
          x,
          y,
          z,
          0,
          0,
          0,
          new ParticleSettings(12, 0.1F, 0, 0, 0.8F, false));
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.SPARKLE,
          context.world(),
          x,
          y,
          z,
          -0.05D * dx / Math.max(length, 0.001D),
          -0.05D * dy / Math.max(length, 0.001D),
          -0.05D * dz / Math.max(length, 0.001D),
          new ParticleSettings(8 + context.world().rand.nextInt(6), 0.5F, 0, 0, 0.7F, false));
    }
  }
}
