package ru.givler.mbo.magic.spell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;

/** Toggles rain in the overworld. */
public final class InvokeWeatherExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    if (!(context.caster() instanceof EntityPlayer)) return SpellResult.BLOCKED;
    EntityPlayer player = (EntityPlayer) context.caster();
    if (context.caster().dimension != 0) return SpellResult.PASS;
    if (!context.world().isRemote) {
      boolean clear = context.world().isRaining();
      context.world().getWorldInfo().setRaining(!clear);
      context
          .world()
          .getWorldInfo()
          .setRainTime(clear ? 0 : (300 + context.world().rand.nextInt(600)) * 20);
      player.addChatComponentMessage(
          new ChatComponentTranslation(
              clear ? "mbo.spell.invoke_weather.sun" : "mbo.spell.invoke_weather.rain"));
    } else {
      for (int i = 0; i < 10; i++)
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.SPARKLE,
            context.world(),
            context.caster().posX + context.world().rand.nextFloat() * 2.0F - 1.0F,
            context.caster().posY
                + context.caster().getEyeHeight()
                - 0.5F
                + context.world().rand.nextFloat(),
            context.caster().posZ + context.world().rand.nextFloat() * 2.0F - 1.0F,
            0,
            0.1D,
            0,
            new ParticleSettings(
                48 + context.world().rand.nextInt(12), 0.5F, 0.7F, 1.0F, 1.0F, false));
    }
    context.world().playSoundAtEntity(context.caster(), "ambient.weather.thunder", 0.5F, 1.0F);
    context.caster().swingItem();
    return SpellResult.SUCCESS;
  }
}
