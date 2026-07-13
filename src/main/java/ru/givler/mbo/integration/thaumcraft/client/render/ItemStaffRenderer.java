package ru.givler.mbo.integration.thaumcraft.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class ItemStaffRenderer implements IItemRenderer {

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        EntityLivingBase entity = (EntityLivingBase) data[1];



        GL11.glTranslatef(0.9375F, 0.0625F, 0.0F);
        GL11.glRotatef(-315.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-0.09375F, 0.0625F, 0.0F);

        renderFlat2D(entity, stack);
    }

    private void renderFlat2D(EntityLivingBase entity, ItemStack stack) {
        Minecraft mc = Minecraft.getMinecraft();
        TextureManager tex = mc.getTextureManager();
        tex.bindTexture(tex.getResourceLocation(stack.getItemSpriteNumber()));

        IIcon icon = entity.getItemIcon(stack, 0);
        if (icon == null) return;

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        GL11.glTranslatef(0.0F, -0.1F, 0.0F);

        GL11.glTranslatef(0.16F, -0.3F, -0.10F);
        GL11.glScalef(1.5F, 1.5F, 1.5F);
        GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);

        ItemRenderer.renderItemIn2D(
                Tessellator.instance,
                icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(),
                icon.getIconWidth(), icon.getIconHeight(), 0.0625F);

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }
}