package ru.givler.mbo.client.render.magic;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.magic.EntityLightningArc;

/** Renders the full alpha-textured lightning bolt on two crossed planes. */
public final class RenderLightningArc extends Render {
  private static final ResourceLocation[] TEXTURES = new ResourceLocation[16];

  static {
    for (int i = 0; i < TEXTURES.length; i++) {
      TEXTURES[i] = new ResourceLocation("mbo", "textures/entity/magic/arc_" + i + ".png");
    }
  }

  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
    EntityLightningArc arc = (EntityLightningArc) entity;
    bindTexture(TEXTURES[arc.textureIndex()]);
    double dx = arc.startX() - arc.posX;
    double dy = arc.startY() - arc.posY;
    double dz = arc.startZ() - arc.posZ;
    double horizontal = Math.sqrt(dx * dx + dz * dz);
    if (horizontal < 0.001D) horizontal = 0.001D;
    double width = 0.22D;
    double px = dz / horizontal * width;
    double pz = -dx / horizontal * width;

    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    GL11.glPushMatrix();
    GL11.glTranslated(x, y, z);
    GL11.glDisable(GL11.GL_LIGHTING);
    GL11.glDisable(GL11.GL_CULL_FACE);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GL11.glColor4f(1, 1, 1, 1);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

    Tessellator t = Tessellator.instance;
    t.startDrawingQuads();
    t.addVertexWithUV(-px, -width, -pz, 1, 1);
    t.addVertexWithUV(px, width, pz, 1, 0);
    t.addVertexWithUV(dx + px, dy + width, dz + pz, 0, 0);
    t.addVertexWithUV(dx - px, dy - width, dz - pz, 0, 1);
    t.draw();

    t.startDrawingQuads();
    t.addVertexWithUV(px, 0, pz, 1, 1);
    t.addVertexWithUV(-px, 0, -pz, 1, 0);
    t.addVertexWithUV(dx - px, dy, dz - pz, 0, 0);
    t.addVertexWithUV(dx + px, dy, dz + pz, 0, 1);
    t.draw();

    GL11.glPopMatrix();
    GL11.glPopAttrib();
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return TEXTURES[((EntityLightningArc) entity).textureIndex()];
  }
}
