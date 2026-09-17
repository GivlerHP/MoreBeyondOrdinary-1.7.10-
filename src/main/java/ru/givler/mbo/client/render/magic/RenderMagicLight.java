package ru.givler.mbo.client.render.magic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.tileentity.TileEntityTemporaryMagicBlock;

/** Full-bright aura and rotating rays around a temporary magical light. */
public final class RenderMagicLight extends TileEntitySpecialRenderer {
  private static final ResourceLocation RAY =
      new ResourceLocation("mbo", "textures/entity/magic/light_ray.png");
  private static final ResourceLocation AURA =
      new ResourceLocation("mbo", "textures/entity/magic/light_aura.png");

  @Override
  public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partial) {
    if (!(tile instanceof TileEntityTemporaryMagicBlock)
        || tile.getWorldObj() == null
        || tile.getWorldObj().getBlock(tile.xCoord, tile.yCoord, tile.zCoord)
            != BlockRegistry.TemporaryLight) return;
    TileEntityTemporaryMagicBlock light = (TileEntityTemporaryMagicBlock) tile;
    float age = light.age() + partial, scale = 1;
    if (age < 10) scale = age / 10F;
    if (light.lifetime() - age < 10) scale = Math.max(0, (light.lifetime() - age) / 10F);
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    GL11.glPushMatrix();
    GL11.glTranslated(x + .5, y + .5, z + .5);
    GL11.glScalef(scale, scale, scale);
    GL11.glDisable(GL11.GL_CULL_FACE);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glShadeModel(GL11.GL_SMOOTH);
    GL11.glDisable(GL11.GL_LIGHTING);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    RenderHelper.disableStandardItemLighting();
    Tessellator t = Tessellator.instance;
    GL11.glPushMatrix();
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    float pitch =
        Minecraft.getMinecraft().gameSettings.thirdPersonView == 2
            ? RenderManager.instance.playerViewX
            : -RenderManager.instance.playerViewX;
    GL11.glRotatef(180 - RenderManager.instance.playerViewY, 0, 1, 0);
    GL11.glRotatef(pitch, 1, 0, 0);
    bindTexture(AURA);
    t.startDrawingQuads();
    t.addVertexWithUV(-.6, .6, 0, 0, 0);
    t.addVertexWithUV(.6, .6, 0, 1, 0);
    t.addVertexWithUV(.6, -.6, 0, 1, 1);
    t.addVertexWithUV(-.6, -.6, 0, 0, 1);
    t.draw();
    GL11.glPopMatrix();
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_SRC_ALPHA);
    bindTexture(RAY);
    long seed = tile.xCoord * 73428767L ^ tile.yCoord * 912931L ^ tile.zCoord * 42349L;
    java.util.Random random = new java.util.Random(seed);
    for (int i = 0; i < 30; i++) {
      int a = random.nextInt(20), b = random.nextInt(20), slice = 20 + a;
      GL11.glPushMatrix();
      GL11.glRotatef(31 * a, 1, 0, 0);
      GL11.glRotatef(31 * b, 0, 0, 1);
      double x1 = .5 * Math.sin((age + 40 * i) * Math.PI / 180),
          z1 = .5 * Math.cos((age + 40 * i) * Math.PI / 180),
          x2 = .5 * Math.sin((age + 40 * i - slice) * Math.PI / 180),
          z2 = .5 * Math.cos((age + 40 * i - slice) * Math.PI / 180);
      t.startDrawing(5);
      t.setColorOpaque_I(0xffffff);
      t.addVertexWithUV(0, 0, 0, 0, 0);
      t.addVertexWithUV(0, 0, 0, 0, 1);
      t.setColorRGBA(0, 0, 0, 255);
      t.addVertexWithUV(x1, 0, z1, 1, 0);
      t.addVertexWithUV(x2, 0, z2, 1, 1);
      t.draw();
      GL11.glPopMatrix();
    }
    RenderHelper.enableStandardItemLighting();
    GL11.glPopMatrix();
    GL11.glPopAttrib();
  }
}
