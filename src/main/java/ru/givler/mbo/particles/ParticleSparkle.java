package ru.givler.mbo.particles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/** Animated, full-bright sparkle from the retained magic particle atlas. */
@SideOnly(Side.CLIENT)
public final class ParticleSparkle extends EntityFX {
  private static final ResourceLocation TEXTURE =
      new ResourceLocation("mbo", "textures/particle/sparkle_particles.png");
  private final boolean gravity;

  public ParticleSparkle(
      World world,
      double x,
      double y,
      double z,
      double motionX,
      double motionY,
      double motionZ,
      int maxAge,
      float red,
      float green,
      float blue) {
    this(
        world,
        x,
        y,
        z,
        motionX,
        motionY,
        motionZ,
        new ParticleSettings(maxAge, red, green, blue, 0.75F, false));
  }

  public ParticleSparkle(
      World world,
      double x,
      double y,
      double z,
      double motionX,
      double motionY,
      double motionZ,
      ParticleSettings settings) {
    super(world, x, y, z);
    this.motionX = motionX;
    this.motionY = motionY;
    this.motionZ = motionZ;
    this.particleScale *= settings.scale;
    this.particleMaxAge = settings.lifetime;
    this.particleRed = settings.red;
    this.particleGreen = settings.green;
    this.particleBlue = settings.blue;
    this.gravity = settings.gravity;
    this.noClip = true;
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
    moveEntity(motionX, motionY, motionZ);
    motionX *= 0.98D;
    motionY *= 0.98D;
    motionZ *= 0.98D;
    if (gravity) motionY -= 0.05D;
    if (particleAge > particleMaxAge / 2) {
      setAlphaF(1.0F - (particleAge - particleMaxAge / 2.0F) / particleMaxAge);
    }
    setParticleTextureIndex(particleAge * 11 / particleMaxAge);
  }

  @Override
  public void setParticleTextureIndex(int index) {
    particleTextureIndexX = index % 4;
    particleTextureIndexY = index / 4;
  }

  @Override
  public int getFXLayer() {
    return 3;
  }

  @Override
  public void renderParticle(
      Tessellator buffer,
      float partialTicks,
      float rotationX,
      float rotationXZ,
      float rotationZ,
      float rotationYZ,
      float rotationXY) {
    float previousLightX = OpenGlHelper.lastBrightnessX;
    float previousLightY = OpenGlHelper.lastBrightnessY;
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    GL11.glPushMatrix();
    GL11.glDisable(GL11.GL_LIGHTING);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

    float minU = particleTextureIndexX / 4.0F;
    float maxU = minU + 0.24975F;
    float minV = particleTextureIndexY / 4.0F;
    float maxV = minV + 0.24975F;
    float size = 0.1F * particleScale;
    Entity camera = Minecraft.getMinecraft().thePlayer;
    interpPosX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks;
    interpPosY = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks;
    interpPosZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks;
    float drawX = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
    float drawY = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
    float drawZ = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

    buffer.startDrawingQuads();
    buffer.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha);
    buffer.addVertexWithUV(
        drawX - rotationX * size - rotationYZ * size,
        drawY - rotationXZ * size,
        drawZ - rotationZ * size - rotationXY * size,
        maxU,
        maxV);
    buffer.addVertexWithUV(
        drawX - rotationX * size + rotationYZ * size,
        drawY + rotationXZ * size,
        drawZ - rotationZ * size + rotationXY * size,
        maxU,
        minV);
    buffer.addVertexWithUV(
        drawX + rotationX * size + rotationYZ * size,
        drawY + rotationXZ * size,
        drawZ + rotationZ * size + rotationXY * size,
        minU,
        minV);
    buffer.addVertexWithUV(
        drawX + rotationX * size - rotationYZ * size,
        drawY - rotationXZ * size,
        drawZ + rotationZ * size - rotationXY * size,
        minU,
        maxV);
    buffer.draw();
    GL11.glPopMatrix();
    GL11.glPopAttrib();
    OpenGlHelper.setLightmapTextureCoords(
        OpenGlHelper.lightmapTexUnit, previousLightX, previousLightY);
  }

  @Override
  public int getBrightnessForRender(float partialTicks) {
    return 15728880;
  }

  @Override
  public float getBrightness(float partialTicks) {
    return 1.0F;
  }
}
