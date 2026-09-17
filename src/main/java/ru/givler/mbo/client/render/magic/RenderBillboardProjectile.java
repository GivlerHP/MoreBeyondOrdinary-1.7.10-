package ru.givler.mbo.client.render.magic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Full-bright camera-facing projectile sprite. */
public final class RenderBillboardProjectile extends Render {
  private final ResourceLocation texture;
  private final float scale;

  public RenderBillboardProjectile(ResourceLocation texture, float scale) {
    this.texture = texture;
    this.scale = scale;
  }

  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) {
    bindEntityTexture(entity);
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    GL11.glPushMatrix();
    GL11.glTranslated(x, y, z);
    GL11.glScalef(scale, scale, scale);
    GL11.glDisable(GL11.GL_LIGHTING);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GL11.glColor4f(1, 1, 1, 1);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    GL11.glRotatef(180.0F - renderManager.playerViewY, 0, 1, 0);
    float pitch =
        Minecraft.getMinecraft().gameSettings.thirdPersonView == 2
            ? renderManager.playerViewX
            : -renderManager.playerViewX;
    GL11.glRotatef(pitch, 1, 0, 0);
    Tessellator t = Tessellator.instance;
    t.startDrawingQuads();
    t.addVertexWithUV(-0.5, -0.25, 0, 0, 1);
    t.addVertexWithUV(0.5, -0.25, 0, 1, 1);
    t.addVertexWithUV(0.5, 0.75, 0, 1, 0);
    t.addVertexWithUV(-0.5, 0.75, 0, 0, 0);
    t.draw();
    GL11.glPopMatrix();
    GL11.glPopAttrib();
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return texture;
  }
}
