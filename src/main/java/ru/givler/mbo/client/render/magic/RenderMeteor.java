package ru.givler.mbo.client.render.magic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.magic.EntityMeteor;
import ru.givler.mbo.registry.BlockRegistry;

@SideOnly(Side.CLIENT)
public class RenderMeteor extends Render {
  private final RenderBlocks sandRenderBlocks = new RenderBlocks();

  public RenderMeteor() {
    this.shadowSize = 0.5F;
  }

  public void doRenderMeteor(
      EntityMeteor meteor, double x, double y, double z, float yaw, float partialTick) {
    World world = meteor.worldObj;
    Block block = BlockRegistry.MeteorBlock;
    GL11.glPushMatrix();
    GL11.glTranslatef((float) x, (float) y + 0.5F, (float) z);
    this.bindEntityTexture(meteor);
    GL11.glDisable(GL11.GL_LIGHTING);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

    meteor.width = 0.98F;
    meteor.height = 0.98F;

    this.sandRenderBlocks.setRenderBoundsFromBlock(block);
    this.sandRenderBlocks.renderBlockSandFalling(
        block,
        world,
        MathHelper.floor_double(meteor.posX),
        MathHelper.floor_double(meteor.posY),
        MathHelper.floor_double(meteor.posZ),
        0);

    GL11.glEnable(GL11.GL_LIGHTING);
    GL11.glPopMatrix();
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return TextureMap.locationBlocksTexture;
  }

  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
    this.doRenderMeteor((EntityMeteor) entity, x, y, z, yaw, partialTick);
  }
}
