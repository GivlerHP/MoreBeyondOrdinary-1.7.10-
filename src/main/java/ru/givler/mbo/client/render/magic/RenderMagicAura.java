package ru.givler.mbo.client.render.magic;

import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.magic.EntityMagicAura;

public final class RenderMagicAura extends Render {
  private static final ResourceLocation T =
      new ResourceLocation("mbo", "textures/entity/magic/healing_aura.png");

  @Override
  public void doRender(Entity e, double x, double y, double z, float a, float p) {
    if (((EntityMagicAura) e).kind() != EntityMagicAura.Kind.HEALING) return;
    bindTexture(T);
    GL11.glPushMatrix();
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glDisable(GL11.GL_LIGHTING);
    GL11.glTranslated(x, y + .01, z);
    GL11.glRotatef(-90, 1, 0, 0);
    GL11.glRotatef(e.ticksExisted / 3F, 0, 0, 1);
    GL11.glScalef(5, 5, 5);
    Tessellator t = Tessellator.instance;
    t.startDrawingQuads();
    t.addVertexWithUV(-.5, -.5, 0, 0, 1);
    t.addVertexWithUV(.5, -.5, 0, 1, 1);
    t.addVertexWithUV(.5, .5, 0, 1, 0);
    t.addVertexWithUV(-.5, .5, 0, 0, 0);
    t.draw();
    GL11.glEnable(GL11.GL_LIGHTING);
    GL11.glDisable(GL11.GL_BLEND);
    GL11.glPopMatrix();
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity e) {
    return T;
  }
}
