package ru.givler.mbo.particles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.World;

/** Lightweight particle for effects which use Minecraft's particle sheet. */
@SideOnly(Side.CLIENT)
public final class ParticleSpell extends EntityFX {
  private final boolean gravity;

  public ParticleSpell(
      World world,
      double x,
      double y,
      double z,
      double vx,
      double vy,
      double vz,
      ParticleSettings settings,
      int texture) {
    super(world, x, y, z, vx, vy, vz);
    motionX = vx;
    motionY = vy;
    motionZ = vz;
    particleMaxAge = settings.lifetime;
    particleRed = settings.red;
    particleGreen = settings.green;
    particleBlue = settings.blue;
    particleScale = settings.scale;
    gravity = settings.gravity;
    noClip = true;
    setParticleTextureIndex(texture);
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
    if (gravity) motionY -= 0.02D;
    moveEntity(motionX, motionY, motionZ);
    motionX *= 0.92D;
    motionY *= 0.92D;
    motionZ *= 0.92D;
    if (particleAge * 2 > particleMaxAge)
      setAlphaF(2F * (particleMaxAge - particleAge) / particleMaxAge);
  }
}
