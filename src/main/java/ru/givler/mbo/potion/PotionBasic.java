package ru.givler.mbo.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.MoreBeyondOrdinary;

public class PotionBasic extends Potion {

    public PotionBasic(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
    }

    @Override
    public void performEffect(EntityLivingBase entitylivingbase, int strength) {
    }

    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
    }

    public static void drawTexturedRect(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight)
    {
        float f = 1F / (float)textureWidth;
        float f1 = 1F / (float)textureHeight;
        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(x), (double)(y + height), 0, (double)((float)(u) * f), (double)((float)(v + height) * f1));
        tessellator.addVertexWithUV((double)(x + width), (double)(y + height), 0, (double)((float)(u + width) * f), (double)((float)(v + height) * f1));
        tessellator.addVertexWithUV((double)(x + width), (double)(y), 0, (double)((float)(u + width) * f), (double)((float)(v) * f1));
        tessellator.addVertexWithUV((double)(x), (double)(y), 0, (double)((float)(u) * f), (double)((float)(v) * f1));
        tessellator.draw();
    }
    public int getPotionId() {
        return this.id;
    }
}
