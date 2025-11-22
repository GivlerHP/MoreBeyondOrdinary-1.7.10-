package ru.givler.mbo.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.MoreBeyondOrdinary;

import java.util.List;

public class Magnetism extends PotionBasic {

    private static final ResourceLocation potionIcon = new ResourceLocation(MoreBeyondOrdinary.MODID, "textures/gui/magnetism_icon.png");

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
        return duration % 10 == 0;
    }

    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
        mc.renderEngine.bindTexture(potionIcon);
        this.drawTexturedRect(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
    }

}
