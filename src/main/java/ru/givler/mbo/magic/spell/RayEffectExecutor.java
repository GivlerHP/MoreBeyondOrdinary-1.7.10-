package ru.givler.mbo.magic.spell;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntityDamageSource;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;
import ru.givler.mbo.potion.SyncedPotionEffects;

/** Shared implementation for short rays which damage and afflict one visible target. */
public final class RayEffectExecutor implements SpellExecutor {
  public enum Kind {
    POISON,
    WITHER,
    ARCANE_JAMMER
  }

  private final Kind kind;
  private final Potion potion;
  private final int duration;
  private final int amplifier;

  public RayEffectExecutor(Kind kind, Potion potion, int duration, int amplifier) {
    this.kind = kind;
    this.potion = potion;
    this.duration = duration;
    this.amplifier = amplifier;
  }

  @Override
  public SpellResult cast(SpellContext context) {
    EntityLivingBase target = context.target();
    if (target == null)
      target = SpellTargeting.livingInSight(context.caster(), 10.0D * context.range());
    if (target == null) return SpellResult.PASS;

    if (context.world().isRemote) {
      drawRay(context, target);
      return SpellResult.SUCCESS;
    }
    if (kind == Kind.POISON
        && (target.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD
            || target instanceof EntitySpider)) return SpellResult.SUCCESS;

    if (kind != Kind.ARCANE_JAMMER) {
      target.attackEntityFrom(
          new EntityDamageSource("mbo.magic", context.caster()).setMagicDamage(), context.power());
    }
    SyncedPotionEffects.apply(
        target,
        new PotionEffect(
            potion.id, Math.max(1, (int) (duration * context.duration())), amplifier, true));
    context.caster().swingItem();
    context
        .world()
        .playSoundAtEntity(
            context.caster(), sound(), 0.8F, 0.9F + context.world().rand.nextFloat() * 0.3F);
    return SpellResult.SUCCESS;
  }

  private void drawRay(SpellContext context, EntityLivingBase target) {
    double sx = context.caster().posX;
    double sy = context.caster().posY + context.caster().getEyeHeight();
    double sz = context.caster().posZ;
    double dx = target.posX - sx;
    double dy = target.posY + target.height * 0.5D - sy;
    double dz = target.posZ - sz;
    double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
    int steps = Math.max(1, (int) (length * 2.0D));
    float r = kind == Kind.POISON ? 0.1F : kind == Kind.WITHER ? 0.1F : 0.9F;
    float g = kind == Kind.POISON ? 0.4F : kind == Kind.WITHER ? 0.0F : 0.3F;
    float b = kind == Kind.POISON ? 0.0F : kind == Kind.WITHER ? 0.05F : 0.7F;
    for (int i = 1; i <= steps; i++) {
      double f = i / (double) steps;
      double x = sx + dx * f;
      double y = sy + dy * f;
      double z = sz + dz * f;
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.SPARKLE,
          context.world(),
          x,
          y,
          z,
          0,
          0,
          0,
          new ParticleSettings(12 + context.world().rand.nextInt(8), r, g, b, 0.7F, false));
      if ((i & 1) == 0)
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.DARK_MAGIC,
            context.world(),
            x,
            y,
            z,
            0,
            0,
            0,
            new ParticleSettings(12, r, g, b, 0.8F, false));
    }
  }

  private String sound() {
    if (kind == Kind.WITHER) return "mob.wither.hurt";
    if (kind == Kind.ARCANE_JAMMER) return "mbo:effect";
    return "mbo:ice";
  }
}
