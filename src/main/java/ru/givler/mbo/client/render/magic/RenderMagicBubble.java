package ru.givler.mbo.client.render.magic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.magic.EntityMagicBubble;

public final class RenderMagicBubble extends Render {
  private static final ResourceLocation PARTICLES =
      new ResourceLocation("textures/particle/particles.png");
  private static final ResourceLocation DARK =
      new ResourceLocation("mbo", "textures/entity/magic/dark_orb.png");

  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
    EntityMagicBubble bubble = (EntityMagicBubble) entity;
    GL11.glPushMatrix();
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GL11.glTranslatef(
        (float) x,
        (float) y + (entity.riddenByEntity == null ? 0 : entity.riddenByEntity.height / 2),
        (float) z);
    bindTexture(bubble.isDark() ? DARK : PARTICLES);
    if (bubble.isDark()) {
      GL11.glDisable(GL11.GL_LIGHTING);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    }
    float pitch =
        Minecraft.getMinecraft().gameSettings.thirdPersonView == 2
            ? renderManager.playerViewX
            : -renderManager.playerViewX;
    GL11.glRotatef(180 - renderManager.playerViewY, 0, 1, 0);
    GL11.glRotatef(pitch, 1, 0, 0);
    GL11.glScalef(3, 3, 3);
    double u0 = bubble.isDark() ? 0 : 1.0D / 128;
    double u1 = bubble.isDark() ? 1 : 8.0D / 128;
    double v0 = bubble.isDark() ? 0 : 17.0D / 128;
    double v1 = bubble.isDark() ? 1 : 24.0D / 128;
    Tessellator t = Tessellator.instance;
    t.startDrawingQuads();
    t.addVertexWithUV(-0.5, -0.5, 0, u0, v1);
    t.addVertexWithUV(0.5, -0.5, 0, u1, v1);
    t.addVertexWithUV(0.5, 0.5, 0, u1, v0);
    t.addVertexWithUV(-0.5, 0.5, 0, u0, v0);
    t.draw();
    if (bubble.isDark()) GL11.glEnable(GL11.GL_LIGHTING);
    GL11.glDisable(GL11.GL_BLEND);
    GL11.glPopMatrix();
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return null;
  }
}
