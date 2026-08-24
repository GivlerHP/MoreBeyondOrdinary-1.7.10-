package ru.givler.mbo.client.render;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

/** Common renderer for flat equipped weapon sprites. Client side only by usage. */
public final class RenderWeapon implements IItemRenderer {
    private final float scale;
    private final float translateX;
    private final float translateY;
    private final float translateZ;

    public RenderWeapon(float scale, float translateX, float translateY, float translateZ) {
        this.scale = scale;
        this.translateX = translateX;
        this.translateY = translateY;
        this.translateZ = translateZ;
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
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        GL11.glPushMatrix();
        GL11.glTranslatef(translateX, translateY, translateZ);
        GL11.glScalef(scale, scale, scale);

        IIcon icon = stack.getItem().getIcon(stack, 0);
        ItemRenderer.renderItemIn2D(Tessellator.instance, icon.getMaxU(), icon.getMinV(),
                icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.05F);
        GL11.glPopMatrix();
    }
}
