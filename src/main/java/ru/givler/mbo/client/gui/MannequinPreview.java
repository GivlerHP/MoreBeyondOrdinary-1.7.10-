package ru.givler.mbo.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;

final class MannequinPreview {
    private MannequinPreview() {}

    static void draw(Minecraft mc, EntityLivingBase entity, int x, int y, int w, int h, int scale,
                     int screenWidth, int screenHeight, float yaw, float pitch) {
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT | GL11.GL_ENABLE_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((x + 2) * mc.displayWidth / screenWidth,
                mc.displayHeight - (y + h - 2) * mc.displayHeight / screenHeight,
                (w - 4) * mc.displayWidth / screenWidth, (h - 4) * mc.displayHeight / screenHeight);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            int centerX = x + w / 2, centerY = y + h / 2;
            GL11.glTranslatef(centerX, centerY, 50);
            GL11.glRotatef(pitch, 1, 0, 0);
            GL11.glRotatef(yaw, 0, 1, 0);
            GL11.glTranslatef(-centerX, -centerY, -50);
            GuiInventory.func_147046_a(centerX, centerY + scale, scale, 0, 0, entity);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }
}
