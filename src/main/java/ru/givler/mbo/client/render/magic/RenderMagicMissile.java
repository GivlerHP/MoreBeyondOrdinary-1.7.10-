package ru.givler.mbo.client.render.magic;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Renders the missile as crossed textured planes aligned with its flight. */
public final class RenderMagicMissile extends Render {
  private static final ResourceLocation TEXTURE =
      new ResourceLocation("mbo", "textures/entity/magic/force_arrow.png");

  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
    bindEntityTexture(entity);
    GL11.glPushMatrix();
    GL11.glTranslated(x, y, z);
    GL11.glRotatef(
        entity.prevRotationYaw
            + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks
            - 90.0F,
        0.0F,
        1.0F,
        0.0F);
    GL11.glRotatef(
        entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks,
        0.0F,
        0.0F,
        1.0F);
    GL11.glDisable(GL11.GL_LIGHTING);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);

    Tessellator buffer = Tessellator.instance;
    for (int plane = 0; plane < 4; plane++) {
      GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
      buffer.startDrawingQuads();
      buffer.addVertexWithUV(-0.675D, -0.225D, 0.0D, 0.0D, 0.0D);
      buffer.addVertexWithUV(0.225D, -0.225D, 0.0D, 0.5D, 0.0D);
      buffer.addVertexWithUV(0.225D, 0.225D, 0.0D, 0.5D, 0.28125D);
      buffer.addVertexWithUV(-0.675D, 0.225D, 0.0D, 0.0D, 0.28125D);
      buffer.draw();
    }

    GL11.glEnable(GL11.GL_LIGHTING);
    GL11.glPopMatrix();
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return TEXTURE;
  }
}
