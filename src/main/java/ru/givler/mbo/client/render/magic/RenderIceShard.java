package ru.givler.mbo.client.render.magic;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Cross-plane shard aligned to its flight direction. */
public final class RenderIceShard extends Render {
  private static final ResourceLocation TEXTURE =
      new ResourceLocation("mbo", "textures/entity/magic/ice_shard.png");

  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) {
    bindEntityTexture(entity);
    GL11.glPushMatrix();
    GL11.glTranslated(x, y, z);
    GL11.glRotatef(
        entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partial - 90,
        0,
        1,
        0);
    GL11.glRotatef(
        entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partial,
        0,
        0,
        1);
    GL11.glDisable(GL11.GL_LIGHTING);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    GL11.glScalef(0.05625F, 0.05625F, 0.05625F);
    Tessellator t = Tessellator.instance;
    for (int i = 0; i < 4; i++) {
      GL11.glRotatef(90, 1, 0, 0);
      t.startDrawingQuads();
      t.addVertexWithUV(-8, -2, 0, 0, 0);
      t.addVertexWithUV(8, -2, 0, 0.5, 0);
      t.addVertexWithUV(8, 2, 0, 0.5, 0.15625);
      t.addVertexWithUV(-8, 2, 0, 0, 0.15625);
      t.draw();
    }
    GL11.glEnable(GL11.GL_LIGHTING);
    GL11.glPopMatrix();
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return TEXTURE;
  }
}
