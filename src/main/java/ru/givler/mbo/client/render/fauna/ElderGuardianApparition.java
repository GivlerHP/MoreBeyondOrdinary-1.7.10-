package ru.givler.mbo.client.render.fauna;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.client.model.fauna.ModelMBOGuardian;
import ru.givler.mbo.entity.fauna.EntityMBOElderGuardian;

/** Camera-relative, translucent guardian apparition, lasting the vanilla 30 ticks. */
public final class ElderGuardianApparition extends EntityFX {
  private final ModelMBOGuardian model = new ModelMBOGuardian();
  private final EntityMBOElderGuardian guardian;
  private final Entity viewer;

  public ElderGuardianApparition(World world, Entity viewer) {
    super(world, viewer.posX, viewer.posY, viewer.posZ);
    this.viewer = viewer;
    guardian = new EntityMBOElderGuardian(world);
    particleMaxAge = EntityMBOElderGuardian.APPARITION_TICKS;
  }

  @Override
  public int getFXLayer() {
    return 3;
  }

  @Override
  public void onUpdate() {
    if (++particleAge >= particleMaxAge) setDead();
  }

  @Override
  public void renderParticle(
      Tessellator mesh, float partial, float rx, float rz, float ryz, float rxy, float rxz) {
    float progress = (particleAge + partial) / particleMaxAge;
    float alpha = .05F + .5F * (float) Math.sin(progress * Math.PI);
    float oldX = OpenGlHelper.lastBrightnessX, oldY = OpenGlHelper.lastBrightnessY;
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    GL11.glPushMatrix();
    try {
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GL11.glAlphaFunc(GL11.GL_GREATER, 1F / 255F);
      GL11.glDisable(GL11.GL_LIGHTING);
      GL11.glDisable(GL11.GL_CULL_FACE);
      GL11.glDisable(GL11.GL_DEPTH_TEST);
      GL11.glDepthMask(false);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
      Minecraft.getMinecraft()
          .getTextureManager()
          .bindTexture(new ResourceLocation("mbo", "textures/entity/guardian/guardian_elder.png"));
      GL11.glColor4f(1, 1, 1, alpha);
      GL11.glRotatef(180 - viewer.rotationYaw, 0, 1, 0);
      GL11.glRotatef(60 - 150 * progress - viewer.rotationPitch, 1, 0, 0);
      GL11.glScalef(.42553192F, -.42553192F, -.42553192F);
      GL11.glTranslatef(0, -.56F, 3.5F);
      GL11.glScalef(2.35F, 2.35F, 2.35F);
      model.render(guardian, 0, 0, particleAge + partial, 0, 0, .0625F);
    } finally {
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, oldX, oldY);
      GL11.glPopMatrix();
      GL11.glPopAttrib();
    }
  }
}
