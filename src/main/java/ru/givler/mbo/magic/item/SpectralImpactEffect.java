package ru.givler.mbo.magic.item;

import net.minecraft.entity.EntityLivingBase;

/** A reusable effect applied when a spectral weapon or projectile hits a living target. */
public interface SpectralImpactEffect {
  String getId();

  void apply(EntityLivingBase attacker, EntityLivingBase target);
}
