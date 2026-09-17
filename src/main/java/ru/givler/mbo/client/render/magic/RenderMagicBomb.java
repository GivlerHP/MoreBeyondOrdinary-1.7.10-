package ru.givler.mbo.client.render.magic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.magic.EntityMagicBomb;

public final class RenderMagicBomb extends Render {
  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) {
    bindEntityTexture(entity);
    GL11.glPushMatrix();
    GL11.glTranslated(x, y, z);
    GL11.glScalef(0.55F, 0.55F, 0.55F);
    GL11.glDisable(GL11.GL_LIGHTING);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    GL11.glRotatef(180 - renderManager.playerViewY, 0, 1, 0);
    GL11.glRotatef(
        Minecraft.getMinecraft().gameSettings.thirdPersonView == 2
            ? renderManager.playerViewX
            : -renderManager.playerViewX,
        1,
        0,
        0);
    Tessellator t = Tessellator.instance;
    t.startDrawingQuads();
    t.addVertexWithUV(-0.5, -0.5, 0, 0, 1);
    t.addVertexWithUV(0.5, -0.5, 0, 1, 1);
    t.addVertexWithUV(0.5, 0.5, 0, 1, 0);
    t.addVertexWithUV(-0.5, 0.5, 0, 0, 0);
    t.draw();
    GL11.glEnable(GL11.GL_LIGHTING);
    GL11.glPopMatrix();
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    String name;
    switch (((EntityMagicBomb) entity).kind()) {
      case DARKNESS:
        name = "darkness_orb";
        break;
      case POISON:
        name = "poison_bomb";
        break;
      case SMOKE:
        name = "smoke_bomb";
        break;
      case SPARK:
        name = "spark_bomb";
        break;
      default:
        name = "firebomb";
    }
    return new ResourceLocation("mbo", "textures/entity/magic/" + name + ".png");
  }
}
