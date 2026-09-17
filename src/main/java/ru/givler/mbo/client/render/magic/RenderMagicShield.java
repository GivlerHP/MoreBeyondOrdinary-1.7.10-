package ru.givler.mbo.client.render.magic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.magic.EntityMagicShield;

/** Recessed translucent ward geometry, matching the original shield silhouette. */
public final class RenderMagicShield extends Render {
  private static final ResourceLocation FORCE =
      new ResourceLocation("mbo", "textures/entity/magic/shield.png");
  private static final ResourceLocation SHADOW =
      new ResourceLocation("mbo", "textures/entity/magic/shadow_ward.png");

  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) {
    EntityMagicShield shield = (EntityMagicShield) entity;
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    GL11.glPushMatrix();
    GL11.glDisable(GL11.GL_CULL_FACE);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_SRC_ALPHA);
    GL11.glShadeModel(GL11.GL_SMOOTH);
    if (Minecraft.getMinecraft().renderViewEntity != shield.owner()
        || Minecraft.getMinecraft().gameSettings.thirdPersonView != 0)
      GL11.glDisable(GL11.GL_LIGHTING);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    GL11.glTranslated(x, y + .3D, z);
    GL11.glRotatef(-shield.rotationYaw, 0, 1, 0);
    GL11.glRotatef(shield.rotationPitch, 1, 0, 0);
    bindTexture(getEntityTexture(shield));
    renderShield(Tessellator.instance);
    GL11.glPopMatrix();
    GL11.glPopAttrib();
  }

  private static void renderShield(Tessellator t) {
    double ow = .6, oh = .7, iw = .3, ih = .4, d = .2;
    t.startDrawing(5);
    edge(t, -ow, oh - .3, -d, -iw, ih, 0, 0, 0, 0, .2);
    edge(t, -ow + .3, oh, -d, -iw, ih, 0, 0, 0, 0, .2);
    edge(t, ow - .3, oh, -d, iw, ih, 0, 1, 0, 1, .2);
    edge(t, ow, oh - .3, -d, iw, ih, 0, 1, 0, 1, .2);
    edge(t, ow, -oh + .3, -d, iw, -ih, 0, 1, 1, 1, .8);
    edge(t, ow - .3, -oh, -d, iw, -ih, 0, 1, 1, 1, .8);
    edge(t, -ow + .3, -oh, -d, -iw, -ih, 0, 0, 1, 0, .8);
    edge(t, -ow, -oh + .3, -d, -iw, -ih, 0, 0, 1, 0, .8);
    edge(t, -ow, oh - .3, -d, -iw, ih, 0, 0, 0, 0, .2);
    t.draw();
    t.startDrawing(5);
    t.setColorOpaque(200, 200, 255);
    t.addVertexWithUV(-iw, ih, 0, 0, .2);
    t.addVertexWithUV(iw, ih, 0, 1, .2);
    t.addVertexWithUV(-iw, -ih, 0, 0, .8);
    t.addVertexWithUV(iw, -ih, 0, 1, .8);
    t.draw();
  }

  private static void edge(
      Tessellator t,
      double ox,
      double oy,
      double oz,
      double ix,
      double iy,
      double iz,
      double ou,
      double ov,
      double iu,
      double iv) {
    t.setColorRGBA(0, 0, 0, 255);
    t.addVertexWithUV(ox, oy, oz, ou, ov);
    t.setColorOpaque(200, 200, 255);
    t.addVertexWithUV(ix, iy, iz, iu, iv);
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return ((EntityMagicShield) entity).kind() == EntityMagicShield.Kind.SHADOW ? SHADOW : FORCE;
  }
}
