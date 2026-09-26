package ru.givler.mbo.core;

import net.minecraft.entity.EntityLivingBase;

/** Matches the gradual air recovery used by modern Minecraft. */
public final class AirRefillHooks {
  private AirRefillHooks() {}

  public static int refill(EntityLivingBase entity) {
    return Math.min(300, entity.getAir() + 4);
  }
}
