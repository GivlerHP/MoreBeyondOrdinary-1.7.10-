package ru.givler.mbo.particles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.World;

/** Clairvoyance marker that repeatedly travels to the next path node. */
@SideOnly(Side.CLIENT)
public final class ParticlePath extends ParticleTextured {
  private final double originX, originY, originZ;

  public ParticlePath(
      World world,
      double x,
      double y,
      double z,
      double vx,
      double vy,
      double vz,
      ParticleSettings settings) {
    super(world, x, y, z, vx, vy, vz, settings, "path_particles.png", 1, 1, false);
    originX = x;
    originY = y;
    originZ = z;
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (!isDead && particleAge % 45 == 0) {
      setPosition(originX, originY, originZ);
      prevPosX = posX;
      prevPosY = posY;
      prevPosZ = posZ;
    }
    if (particleAge > particleMaxAge / 2)
      setAlphaF(Math.max(0, 1F - 2F * (particleAge - particleMaxAge / 2F) / particleMaxAge));
  }
}
