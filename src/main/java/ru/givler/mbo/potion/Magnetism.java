package ru.givler.mbo.potion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.main;

import java.util.List;

public class Magnetism extends Potion {

    private static final ResourceLocation potionIcon = new ResourceLocation(main.MODID, "textures/gui/magnetism_icon.png");

    public Magnetism(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.magnetism");
    }

    @Override
    public void performEffect(EntityLivingBase entity, int strength) {
        if (entity.worldObj.isRemote || !(entity instanceof net.minecraft.entity.player.EntityPlayer)) return;

        double radius = 3.0 + (strength * 2.5);
        List<EntityItem> items = entity.worldObj.getEntitiesWithinAABB(EntityItem.class,
                AxisAlignedBB.getBoundingBox(
                        entity.posX - radius, entity.posY - radius, entity.posZ - radius,
                        entity.posX + radius, entity.posY + radius, entity.posZ + radius));

        for (EntityItem item : items) {
            if (item.isDead || item.delayBeforeCanPickup > 0) continue;

            double dx = entity.posX - item.posX;
            double dy = entity.posY + 0.5 - item.posY;
            double dz = entity.posZ - item.posZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (dist < 0.5) continue;

            double pullSpeed = 0.55 + 0.05 * strength;
            item.motionX += (dx / dist) * pullSpeed;
            item.motionY += (dy / dist) * pullSpeed;
            item.motionZ += (dz / dist) * pullSpeed;
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 10 == 0; // Притягивать каждые 10 тиков (0.5 секунды)
    }

    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        mc.renderEngine.bindTexture(potionIcon);
        this.drawTexturedRect(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
    }

    public static void drawTexturedRect(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        float f = 1F / (float)textureWidth;
        float f1 = 1F / (float)textureHeight;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0, u * f, (v + height) * f1);
        tessellator.addVertexWithUV(x + width, y + height, 0, (u + width) * f, (v + height) * f1);
        tessellator.addVertexWithUV(x + width, y, 0, (u + width) * f, v * f1);
        tessellator.addVertexWithUV(x, y, 0, u * f, v * f1);
        tessellator.draw();
    }
}
