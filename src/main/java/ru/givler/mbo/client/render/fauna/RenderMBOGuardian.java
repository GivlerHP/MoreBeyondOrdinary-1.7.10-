package ru.givler.mbo.client.render.fauna;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.client.model.fauna.ModelMBOGuardian;
import ru.givler.mbo.entity.fauna.EntityMBOGuardian;

public final class RenderMBOGuardian extends RenderLiving {
  public RenderMBOGuardian() {
    super(new ModelMBOGuardian(), .5F);
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return new ResourceLocation("mbo", "textures/entity/guardian/guardian.png");
  }

  @Override
  public void doRender(
      EntityLiving entity, double x, double y, double z, float yaw, float partial) {
    super.doRender(entity, x, y, z, yaw, partial);
    EntityMBOGuardian guardian = (EntityMBOGuardian) entity;
    Entity target = entity.worldObj.getEntityByID(guardian.beamTarget());
    if (target == null) return;
    double tx = target.lastTickPosX + (target.posX - target.lastTickPosX) * partial,
        ty =
            target.lastTickPosY
                + (target.posY - target.lastTickPosY) * partial
                + target.height * .5,
        tz = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * partial;
    double sx = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial,
        sy =
            entity.lastTickPosY
                + (entity.posY - entity.lastTickPosY) * partial
                + entity.getEyeHeight(),
        sz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial;
    double dx = tx - sx,
        dy = ty - sy,
        dz = tz - sz,
        length = Math.sqrt(dx * dx + dy * dy + dz * dz);
    if (length < .001) return;
    GL11.glPushAttrib(
        GL11.GL_ENABLE_BIT
            | GL11.GL_CURRENT_BIT
            | GL11.GL_TEXTURE_BIT
            | GL11.GL_COLOR_BUFFER_BIT
            | GL11.GL_DEPTH_BUFFER_BIT);
    GL11.glPushMatrix();
    GL11.glTranslated(x, y + entity.getEyeHeight(), z);
    GL11.glRotated(Math.atan2(dx, dz) * 180 / Math.PI, 0, 1, 0);
    GL11.glRotated(-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * 180 / Math.PI, 1, 0, 0);
    GL11.glDisable(GL11.GL_LIGHTING);
    GL11.glDisable(GL11.GL_CULL_FACE);
    GL11.glDisable(GL11.GL_BLEND);
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    bindTexture(new ResourceLocation("mbo", "textures/entity/guardian/guardian_beam.png"));
    float oldX = OpenGlHelper.lastBrightnessX, oldY = OpenGlHelper.lastBrightnessY;
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    float charge = guardian.beamCharge() * guardian.beamCharge();
    double scroll = -(entity.ticksExisted + partial) * .05;
    Tessellator mesh = Tessellator.instance;
    mesh.startDrawingQuads();
    mesh.setColorOpaque_F(.25F + .75F * charge, .13F + .75F * charge, .5F - .25F * charge);
    for (int i = 0; i < 8; i++) {
      double a = i * Math.PI / 4,
          b = (i + 1) * Math.PI / 4,
          spin = (entity.ticksExisted + partial) * .05;
      double ax = Math.cos(a + spin) * .2,
          ay = Math.sin(a + spin) * .2,
          bx = Math.cos(b + spin) * .2,
          by = Math.sin(b + spin) * .2;
      mesh.addVertexWithUV(ax, ay, 0, 0, scroll);
      mesh.addVertexWithUV(bx, by, 0, 1, scroll);
      mesh.addVertexWithUV(bx, by, length, 1, scroll + length * .5);
      mesh.addVertexWithUV(ax, ay, length, 0, scroll + length * .5);
    }
    mesh.draw();
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, oldX, oldY);
    GL11.glPopMatrix();
    GL11.glPopAttrib();
  }
}
