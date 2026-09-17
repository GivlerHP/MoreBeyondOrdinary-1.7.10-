package ru.givler.mbo.client.render.magic;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class RenderIceSpike extends Render {
  private static final ResourceLocation T =
      new ResourceLocation("mbo", "textures/entity/magic/ice_spike.png");

  @Override
  public void doRender(Entity e, double x, double y, double z, float a, float p) {
    bindTexture(T);
    GL11.glPushMatrix();
    GL11.glTranslated(x, y, z);
    GL11.glDisable(GL11.GL_LIGHTING);
    int light = e.getBrightnessForRender(p);
    OpenGlHelper.setLightmapTextureCoords(
        OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
    GL11.glColor4f(1, 1, 1, 1);
    Tessellator t = Tessellator.instance;
    face(t, 0, .5, 0, -.5);
    face(t, .5, 0, -.5, 0);
    face(t, 0, -.5, 0, .5);
    face(t, -.5, 0, .5, 0);
    GL11.glEnable(GL11.GL_LIGHTING);
    GL11.glPopMatrix();
  }

  private static void face(Tessellator t, double x1, double z1, double x2, double z2) {
    t.startDrawingQuads();
    t.addVertexWithUV(x1, 0, z1, 1, 1);
    t.addVertexWithUV(x1, 1, z1, 1, 0);
    t.addVertexWithUV(x2, 1, z2, 0, 0);
    t.addVertexWithUV(x2, 0, z2, 0, 1);
    t.draw();
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity e) {
    return T;
  }
}
