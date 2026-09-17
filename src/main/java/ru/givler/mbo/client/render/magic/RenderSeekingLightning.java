package ru.givler.mbo.client.render.magic;

import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.magic.EntitySeekingLightning;

public final class RenderSeekingLightning extends Render {
  private static final ResourceLocation TEXTURE =
      new ResourceLocation("mbo", "textures/entity/magic/lightning_disc.png");

  @Override
  public void doRender(Entity e, double x, double y, double z, float yaw, float partial) {
    if (((EntitySeekingLightning) e).kind() != EntitySeekingLightning.Kind.DISC) return;
    bindEntityTexture(e);
    GL11.glPushMatrix();
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glDisable(GL11.GL_LIGHTING);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    GL11.glTranslated(x, y, z);
    GL11.glRotatef(-90, 1, 0, 0);
    GL11.glRotatef(e.ticksExisted * 8, 0, 0, 1);
    GL11.glScalef(2, 2, 2);
    Tessellator t = Tessellator.instance;
    t.startDrawingQuads();
    t.addVertexWithUV(-.5, -.5, .01, 0, 1);
    t.addVertexWithUV(.5, -.5, .01, 1, 1);
    t.addVertexWithUV(.5, .5, .01, 1, 0);
    t.addVertexWithUV(-.5, .5, .01, 0, 0);
    t.draw();
    GL11.glEnable(GL11.GL_LIGHTING);
    GL11.glDisable(GL11.GL_BLEND);
    GL11.glPopMatrix();
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity e) {
    return TEXTURE;
  }
}
