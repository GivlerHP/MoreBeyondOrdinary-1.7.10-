package ru.givler.mbo.particles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.World;

/** Snowflake orbiting a fixed storm centre. */
@SideOnly(Side.CLIENT)
public final class ParticleBlizzard extends ParticleTextured {
  private final double centreX, centreZ, radius, angularVelocity;
  private double angle;

  public ParticleBlizzard(World world, double x, double y, double z, ParticleSettings settings) {
    super(world, x, y, z, 0, 0, 0, settings, "snow_particles.png", 4, 4, false);
    centreX = x;
    centreZ = z;
    radius = Math.max(0.1D, settings.scale);
    angle = rand.nextDouble() * Math.PI * 2;
    double speed = rand.nextBoolean() ? rand.nextDouble() * 2 + 1 : -rand.nextDouble() * 2 - 1;
    angularVelocity =
        speed > 0
            ? Math.PI * 2 / 20 - speed / (20 * radius)
            : -Math.PI * 2 / 20 + speed / (20 * radius);
    setPosition(centreX - Math.cos(angle) * radius, y, centreZ + Math.sin(angle) * radius);
    particleScale = 0.6F;
  }

  @Override
  public void onUpdate() {
    prevPosX = posX;
    prevPosY = posY;
    prevPosZ = posZ;
    if (particleAge++ >= particleMaxAge) {
      setDead();
      return;
    }
    angle += angularVelocity;
    setPosition(centreX - Math.cos(angle) * radius, posY, centreZ + Math.sin(angle) * radius);
    if (particleAge > particleMaxAge / 2)
      setAlphaF(1F - (particleAge - particleMaxAge / 2F) / particleMaxAge);
  }
}
