package ru.givler.mbo.client.render.magic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/** Renders temporary equipment as a thin translucent item in either hand view. */
public final class RenderSpectralItem implements IItemRenderer {
  @Override
  public boolean handleRenderType(ItemStack stack, ItemRenderType type) {
    return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON;
  }

  @Override
  public boolean shouldUseRenderHelper(
      ItemRenderType type, ItemStack stack, ItemRendererHelper helper) {
    return false;
  }

  @Override
  public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
    Minecraft minecraft = Minecraft.getMinecraft();
    IIcon icon = minecraft.thePlayer.getItemIcon(stack, 0);
    if (icon == null) return;
    GL11.glPushMatrix();
    GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.8F);
    TextureManager textures = minecraft.getTextureManager();
    textures.bindTexture(textures.getResourceLocation(stack.getItemSpriteNumber()));
    ItemRenderer.renderItemIn2D(
        Tessellator.instance,
        icon.getMaxU(),
        icon.getMinV(),
        icon.getMinU(),
        icon.getMaxV(),
        icon.getIconWidth(),
        icon.getIconHeight(),
        0.0625F);
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    GL11.glDisable(GL11.GL_BLEND);
    GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    GL11.glPopMatrix();
  }
}
