package ru.givler.mbo.entity.magic;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** Vanilla-flight arrow carrying the id of an MBO spectral impact effect. */
public class EntitySpectralArrow extends EntityArrow {
  private String impactEffectId = "none";

  public EntitySpectralArrow(World world) {
    super(world);
  }

  public EntitySpectralArrow(
      World world, EntityLivingBase shooter, float velocity, String effectId) {
    super(world, shooter, velocity);
    this.impactEffectId = effectId == null ? "none" : effectId;
  }

  public String getImpactEffectId() {
    return impactEffectId;
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setString("MBO_SpectralEffect", impactEffectId);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    impactEffectId = tag.getString("MBO_SpectralEffect");
    if (impactEffectId.isEmpty()) impactEffectId = "none";
  }
}
