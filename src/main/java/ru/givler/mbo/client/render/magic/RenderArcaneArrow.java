package ru.givler.mbo.client.render.magic;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.magic.EntityArcaneArrow;

/** Cross-plane renderer selecting a texture from the arrow's synced kind. */
public final class RenderArcaneArrow extends Render {
  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) {
    bindEntityTexture(entity);
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
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
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GL11.glColor4f(1, 1, 1, 1);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    if (((EntityArcaneArrow) entity).kind() == EntityArcaneArrow.Kind.FORCE) {
      renderForceArrow(entity, partial);
    } else {
      renderCrossedArrow(((EntityArcaneArrow) entity).kind());
    }
    GL11.glPopMatrix();
    GL11.glPopAttrib();
  }

  private static void renderCrossedArrow(EntityArcaneArrow.Kind kind) {
    float length = kind == EntityArcaneArrow.Kind.ICE_LANCE ? 12F : 8F;
    GL11.glScalef(0.05625F, 0.05625F, 0.05625F);
    Tessellator t = Tessellator.instance;
    for (int i = 0; i < 4; i++) {
      GL11.glRotatef(90, 1, 0, 0);
      t.startDrawingQuads();
      t.addVertexWithUV(-length, -2, 0, 0, 1);
      t.addVertexWithUV(length, -2, 0, 1, 1);
      t.addVertexWithUV(length, 2, 0, 1, 0);
      t.addVertexWithUV(-length, 2, 0, 0, 0);
      t.draw();
    }
  }

  private static void renderForceArrow(Entity entity, float partial) {
    final float pixel = 1.0F / 32.0F;
    final float scale = 0.05625F * 0.8F;
    Tessellator t = Tessellator.instance;

    GL11.glRotatef(180, 0, 1, 0);
    GL11.glRotatef(45, 1, 0, 0);
    GL11.glScalef(scale, scale, scale);
    GL11.glTranslatef(-4, 0, 0);

    // Square energy head from the lower-left 7x7 region of the texture.
    t.startDrawingQuads();
    t.addVertexWithUV(-5, 3.5, -3.5, 0, 25 * pixel);
    t.addVertexWithUV(-5, 3.5, 3.5, 7 * pixel, 25 * pixel);
    t.addVertexWithUV(-5, -3.5, 3.5, 7 * pixel, 1);
    t.addVertexWithUV(-5, -3.5, -3.5, 0, 1);
    t.draw();

    // Five animated translucent wave fronts behind the head.
    double phase = (entity.ticksExisted + partial) % 3.0D / 3.0D;
    for (int i = 0; i < 5; i++) {
      double age = i + phase;
      double width = 2.0D + (Math.sqrt(age * 2.0D) - 0.6D) * 2.0D;
      double px = -10.0D + age * 4.0D;
      GL11.glColor4f(1, 1, 1, 1.0F - i * 0.2F);
      t.startDrawingQuads();
      t.addVertexWithUV(px, -width, -width, 16 * pixel, 0);
      t.addVertexWithUV(px, -width, width, 1, 0);
      t.addVertexWithUV(px, width, width, 1, 16 * pixel);
      t.addVertexWithUV(px, width, -width, 16 * pixel, 16 * pixel);
      t.draw();
      t.startDrawingQuads();
      t.addVertexWithUV(px, width, -width, 16 * pixel, 0);
      t.addVertexWithUV(px, width, width, 1, 0);
      t.addVertexWithUV(px, -width, width, 1, 16 * pixel);
      t.addVertexWithUV(px, -width, -width, 16 * pixel, 16 * pixel);
      t.draw();
    }

    GL11.glColor4f(1, 1, 1, 1);
    // Four crossed longitudinal fins using the 14x7 shaft region.
    for (int i = 0; i < 4; i++) {
      GL11.glRotatef(90, 1, 0, 0);
      t.startDrawingQuads();
      t.addVertexWithUV(-10, -4, 0, 0, 0);
      t.addVertexWithUV(10, -4, 0, 14 * pixel, 0);
      t.addVertexWithUV(10, 4, 0, 14 * pixel, 7 * pixel);
      t.addVertexWithUV(-10, 4, 0, 0, 7 * pixel);
      t.draw();
    }
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    String name;
    switch (((EntityArcaneArrow) entity).kind()) {
      case FORCE:
        name = "force_arrow";
        break;
      case ICE_LANCE:
        name = "ice_lance";
        break;
      case LIGHTNING:
        name = "lightning_arrow";
        break;
      default:
        name = "dart";
    }
    return new ResourceLocation("mbo", "textures/entity/magic/" + name + ".png");
  }
}
