package ru.givler.mbo.client.render.magic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.magic.EntityBlackHole;

public final class RenderBlackHole extends Render {
  private static final ResourceLocation CORE =
      new ResourceLocation("mbo", "textures/entity/magic/black_hole.png");
  private static final ResourceLocation RAY =
      new ResourceLocation("mbo", "textures/entity/magic/dark_ray.png");

  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) {
    EntityBlackHole hole = (EntityBlackHole) entity;
    float age = hole.ticksExisted + partial;
    float scale = Math.min(1, age / 10.0F);
    scale = Math.min(scale, Math.max(0, (hole.lifetime() - age) / 10.0F));
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    GL11.glPushMatrix();
    GL11.glTranslated(x, y, z);
    GL11.glScalef(scale, scale, scale);
    GL11.glDisable(GL11.GL_CULL_FACE);
    GL11.glDisable(GL11.GL_LIGHTING);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GL11.glColor4f(1, 1, 1, 1);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    renderRays(age);
    renderCore();
    GL11.glPopMatrix();
    GL11.glPopAttrib();
  }

  private void renderRays(float age) {
    bindTexture(RAY);
    Tessellator t = Tessellator.instance;
    for (int i = 0; i < 30; i++) {
      double a = Math.toRadians(age * 2 + i * 137.5D);
      double tilt = Math.toRadians((i * 47) % 180 - 90);
      double radius = 3.0D;
      double x1 = Math.cos(a) * Math.cos(tilt) * radius;
      double y1 = Math.sin(tilt) * radius;
      double z1 = Math.sin(a) * Math.cos(tilt) * radius;
      double x2 = Math.cos(a + 0.35D) * Math.cos(tilt) * radius;
      double y2 = y1 + Math.sin(a * 0.7D) * 0.25D;
      double z2 = Math.sin(a + 0.35D) * Math.cos(tilt) * radius;
      t.startDrawingQuads();
      t.addVertexWithUV(0, -0.05, 0, 0, 0);
      t.addVertexWithUV(0, 0.05, 0, 0, 1);
      t.addVertexWithUV(x1, y1, z1, 1, 1);
      t.addVertexWithUV(x2, y2, z2, 1, 0);
      t.draw();
    }
  }

  private void renderCore() {
    bindTexture(CORE);
    GL11.glPushMatrix();
    GL11.glRotatef(180 - renderManager.playerViewY, 0, 1, 0);
    float pitch =
        Minecraft.getMinecraft().gameSettings.thirdPersonView == 2
            ? renderManager.playerViewX
            : -renderManager.playerViewX;
    GL11.glRotatef(pitch, 1, 0, 0);
    Tessellator t = Tessellator.instance;
    t.startDrawingQuads();
    t.addVertexWithUV(-0.4, 0.4, 0, 0, 0);
    t.addVertexWithUV(0.4, 0.4, 0, 1, 0);
    t.addVertexWithUV(0.4, -0.4, 0, 1, 1);
    t.addVertexWithUV(-0.4, -0.4, 0, 0, 1);
    t.draw();
    GL11.glPopMatrix();
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return CORE;
  }
}
