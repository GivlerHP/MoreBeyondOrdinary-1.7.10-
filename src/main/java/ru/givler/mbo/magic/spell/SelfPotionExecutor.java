package ru.givler.mbo.magic.spell;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

/** Shared implementation for instant self-buffs with spell-scaled duration. */
public final class SelfPotionExecutor implements SpellExecutor {
  private final Potion potion;
  private final Potion secondary;
  private final int duration;
  private final int amplifier;
  private final float red;
  private final float green;
  private final float blue;
  private final String sound;
  private final boolean rejectWhenActive;

  public SelfPotionExecutor(
      Potion potion,
      int duration,
      int amplifier,
      float red,
      float green,
      float blue,
      String sound,
      boolean rejectWhenActive) {
    this(potion, null, duration, amplifier, red, green, blue, sound, rejectWhenActive);
  }

  public SelfPotionExecutor(
      Potion potion,
      Potion secondary,
      int duration,
      int amplifier,
      float red,
      float green,
      float blue,
      String sound,
      boolean rejectWhenActive) {
    this.potion = potion;
    this.secondary = secondary;
    this.duration = duration;
    this.amplifier = amplifier;
    this.red = red;
    this.green = green;
    this.blue = blue;
    this.sound = sound;
    this.rejectWhenActive = rejectWhenActive;
  }

  @Override
  public SpellResult cast(SpellContext context) {
    if (rejectWhenActive && context.caster().isPotionActive(potion)) return SpellResult.BLOCKED;
    if (context.world().isRemote) return SpellResult.SUCCESS;
    int ticks = Math.max(1, (int) (duration * context.duration()));
    context.caster().addPotionEffect(new PotionEffect(potion.id, ticks, amplifier, true));
    if (secondary != null)
      context.caster().addPotionEffect(new PotionEffect(secondary.id, ticks, 0, true));
    SpellEffects.sparkleBurst(context.caster(), 10, red, green, blue);
    context
        .world()
        .playSoundAtEntity(
            context.caster(), sound, 0.7F, 1.0F + context.world().rand.nextFloat() * 0.4F);
    return SpellResult.SUCCESS;
  }
}
