package ru.givler.mbo.client.render;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.tileentity.TileEntityCampfire;

public final class RenderCampfireFood extends TileEntitySpecialRenderer {
    private final EntityItem[] renderedFood = new EntityItem[4];
    private static final double[][] POSITIONS = {
            {.9375, .45, .1875}, {.8125, .45, .9375},
            {.0625, .45, .8125}, {.1875, .45, .0625}
    };

    @Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        TileEntityCampfire campfire = (TileEntityCampfire) tile;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = campfire.getFood(i);
            if (stack == null) continue;
            if (renderedFood[i] == null) {
                renderedFood[i] = new EntityItem(tile.getWorldObj());
                renderedFood[i].hoverStart = 0;
            } else {
                renderedFood[i].setWorld(tile.getWorldObj());
            }
            EntityItem entity = renderedFood[i];
            entity.setEntityItemStack(stack);
            entity.age = 0;
            GL11.glPushMatrix();
            try {
                RenderItem.renderInFrame = true;
                GL11.glColor4f(1, 1, 1, 1);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glTranslated(x + POSITIONS[i][0], y + POSITIONS[i][1], z + POSITIONS[i][2]);
                GL11.glRotatef(180, 0, 1, 1);
                GL11.glRotatef(i * -90, 0, 0, 1);
                GL11.glRotatef(270, 0, 0, 1);
                GL11.glScalef(.625F, .625F, .625F);
                RenderManager.instance.renderEntityWithPosYaw(entity, 0, 0, 0, 0, 0);
            } finally {
                RenderItem.renderInFrame = false;
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glPopMatrix();
            }
        }
    }
}
