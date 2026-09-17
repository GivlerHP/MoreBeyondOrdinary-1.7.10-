package ru.givler.mbo.client.render.magic;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.magic.EntityGroundMagicEffect;

/** Draws the texture of a stationary spell directly over the terrain. */
public final class RenderGroundMagicEffect extends Render {
  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
    EntityGroundMagicEffect effect = (EntityGroundMagicEffect) entity;
    float scale = effect.kind() == EntityGroundMagicEffect.Kind.FIRE_RING ? 5.0F : 2.0F;
    if (effect.kind() == EntityGroundMagicEffect.Kind.DECAY) {
      scale *=
          Math.min(
              1.0F,
              Math.max(0.0F, (effect.lifetime() - effect.ticksExisted - partialTicks) / 50.0F));
    }
    bindEntityTexture(entity);
    GL11.glPushMatrix();
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    GL11.glTranslated(x, y + 0.015D, z);
    GL11.glDisable(GL11.GL_LIGHTING);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    Tessellator t = Tessellator.instance;
    double half = scale * 0.5D;
    t.startDrawingQuads();
    t.addVertexWithUV(-half, 0, half, 0, 1);
    t.addVertexWithUV(half, 0, half, 1, 1);
    t.addVertexWithUV(half, 0, -half, 1, 0);
    t.addVertexWithUV(-half, 0, -half, 0, 0);
    t.draw();
    GL11.glPopAttrib();
    GL11.glPopMatrix();
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    EntityGroundMagicEffect effect = (EntityGroundMagicEffect) entity;
    String name =
        effect.kind() == EntityGroundMagicEffect.Kind.FIRE_RING
            ? "ring_of_fire"
            : "decay_" + effect.textureIndex();
    return new ResourceLocation("mbo", "textures/entity/magic/" + name + ".png");
  }
}
