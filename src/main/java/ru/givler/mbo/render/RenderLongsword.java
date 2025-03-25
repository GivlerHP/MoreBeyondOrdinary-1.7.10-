package ru.givler.mbo.render;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;


public class RenderLongsword implements IItemRenderer {
    private final float scale;

    public RenderLongsword(float scale) {
        this.scale = scale;
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        switch (type) {
            case EQUIPPED_FIRST_PERSON:
                renderEquippedItem(item, (EntityLivingBase) data[1], false);
                break;
            case EQUIPPED:
                renderEquippedItem(item, (EntityLivingBase) data[1], false);
                break;
            default:
        }
    }

    private void renderEquippedItem(ItemStack stack, EntityLivingBase entity, boolean firstPerson) {
        GL11.glPushMatrix();
        float f = scale; // Это должно использовать переданный масштаб

        if (firstPerson) {

            GL11.glRotatef(0, 0, 0, 0);
        } else {
            f *= (entity instanceof EntityPlayer ? 1.0F : 1.0F); // Для отображения от третьего лица масштаб по умолчанию
            GL11.glTranslatef(-0.3F, -0.13F, 0.01F); // Корректировка положения
        }

        GL11.glScalef(f, f, f); // Применение масштаба
        IIcon icon = stack.getItem().getIcon(stack, 0);
        Tessellator tessellator = Tessellator.instance;
        ItemRenderer.renderItemIn2D(tessellator, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.05F);
        GL11.glPopMatrix();
    }
}
