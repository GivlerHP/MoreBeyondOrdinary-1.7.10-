package ru.givler.mbo.particles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/** A piece of terrain which keeps orbiting the moving centre of a tornado. */
@SideOnly(Side.CLIENT)
public final class ParticleTornado extends EntityDiggingFX {
  private double angle;
  private final double radius;
  private final double speed;
  private final double centreVelocityX;
  private final double centreVelocityZ;
  private final boolean fullBrightness;

  public ParticleTornado(
      World world,
      int lifetime,
      double centreX,
      double centreZ,
      double radius,
      double y,
      double velocityX,
      double velocityZ,
      Block block,
      int metadata) {
    super(world, 0, 0, 0, 0, 0, 0, block, metadata, 1);
    this.angle = rand.nextDouble() * Math.PI * 2;
    this.radius = Math.max(0.1D, radius);
    this.speed = rand.nextDouble() * 2 + 1;
    this.centreVelocityX = velocityX;
    this.centreVelocityZ = velocityZ;
    this.particleMaxAge = lifetime;
    this.noClip = true;
    this.fullBrightness = block.getLightValue() > 0;
    setPosition(
        centreX - Math.cos(angle) * this.radius, y, centreZ + Math.sin(angle) * this.radius);
    if (block != Blocks.grass) setParticleIcon(block.getIcon(1, metadata));
    if (!fullBrightness) {
      particleRed *= 0.75F;
      particleGreen *= 0.75F;
      particleBlue *= 0.75F;
    }
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
    double omega = Math.PI * 2 / 20 - speed / (20 * radius);
    angle += omega;
    motionX = radius * omega * Math.sin(angle) + centreVelocityX;
    motionZ = radius * omega * Math.cos(angle) + centreVelocityZ;
    moveEntity(motionX, 0, motionZ);
    if (particleAge > particleMaxAge / 2) {
      setAlphaF(1F - (particleAge - particleMaxAge / 2F) / particleMaxAge);
    }
  }

  @Override
  public int getBrightnessForRender(float partial) {
    return fullBrightness ? 15728880 : super.getBrightnessForRender(partial);
  }

  @Override
  public float getBrightness(float partial) {
    return fullBrightness ? 1F : super.getBrightness(partial);
  }
}
