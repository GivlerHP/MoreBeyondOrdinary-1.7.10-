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

/** Shared animated billboard for MBO's spell particle atlases. */
@SideOnly(Side.CLIENT)
public class ParticleTextured extends EntityFX {
  private final ResourceLocation texture;
  private final int columns;
  private final int rows;
  private final boolean gravity;
  private final boolean animated;
  private final int variant;
  private final int staticFrame;
  private final boolean stationary;
  private final double drag;
  private final double gravityAcceleration;

  public ParticleTextured(
      World world,
      double x,
      double y,
      double z,
      double vx,
      double vy,
      double vz,
      ParticleSettings settings,
      String textureName,
      int columns,
      int rows,
      boolean animated) {
    super(world, x, y, z);
    motionX = vx;
    motionY = vy;
    motionZ = vz;
    particleMaxAge = settings.lifetime;
    particleRed = settings.red;
    particleGreen = settings.green;
    particleBlue = settings.blue;
    particleScale = settings.scale;
    gravity = settings.gravity;
    this.animated = animated;
    this.columns = columns;
    this.rows = rows;
    int populatedRows =
        ("ice_particles.png".equals(textureName) || "snow_particles.png".equals(textureName))
            ? 2
            : rows;
    if (animated) {
      variant = rand.nextInt(Math.max(1, populatedRows));
      staticFrame = 0;
    } else {
      int sprite = rand.nextInt(Math.max(1, columns * populatedRows));
      staticFrame = sprite % columns;
      variant = sprite / columns;
    }
    texture = new ResourceLocation("mbo", "textures/particle/" + textureName);
    stationary = "lightning_particles.png".equals(textureName);
    drag = "ice_particles.png".equals(textureName) ? 0.9800000190734863D : 1.0D;
    gravityAcceleration = "ice_particles.png".equals(textureName) ? 0.05D : 0.0D;
    noClip = true;
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
    if (!stationary) {
      moveEntity(motionX, motionY, motionZ);
      motionX *= drag;
      motionY *= drag;
      motionZ *= drag;
      if (gravity) motionY -= gravityAcceleration;
    }
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
    Minecraft.getMinecraft().renderEngine.bindTexture(texture);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    int frame =
        animated ? Math.min(columns - 1, particleAge * columns / particleMaxAge) : randFrame();
    float minU = frame / (float) columns;
    float maxU = (frame + 0.999F) / columns;
    float minV = variant / (float) rows;
    float maxV = (variant + 0.999F) / rows;
    float size = 0.1F * particleScale;
    Entity camera = Minecraft.getMinecraft().thePlayer;
    interpPosX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks;
    interpPosY = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks;
    interpPosZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks;
    float px = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
    float py = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
    float pz = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);
    buffer.startDrawingQuads();
    buffer.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha);
    buffer.addVertexWithUV(
        px - rotationX * size - rotationYZ * size,
        py - rotationXZ * size,
        pz - rotationZ * size - rotationXY * size,
        maxU,
        maxV);
    buffer.addVertexWithUV(
        px - rotationX * size + rotationYZ * size,
        py + rotationXZ * size,
        pz - rotationZ * size + rotationXY * size,
        maxU,
        minV);
    buffer.addVertexWithUV(
        px + rotationX * size + rotationYZ * size,
        py + rotationXZ * size,
        pz + rotationZ * size + rotationXY * size,
        minU,
        minV);
    buffer.addVertexWithUV(
        px + rotationX * size - rotationYZ * size,
        py - rotationXZ * size,
        pz + rotationZ * size - rotationXY * size,
        minU,
        maxV);
    buffer.draw();
    GL11.glPopMatrix();
    GL11.glPopAttrib();
    OpenGlHelper.setLightmapTextureCoords(
        OpenGlHelper.lightmapTexUnit, previousLightX, previousLightY);
  }

  private int randFrame() {
    return staticFrame;
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
