package ru.givler.mbo.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.main;

public class DamageBoost extends Potion {

    private static final ResourceLocation potionIcon = new ResourceLocation(main.MODID, "textures/gui/damage_boost_icon.png");

    public DamageBoost(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.damage_boost");
        // Применяем эффект: увеличение урона на 15% за уровень
        this.func_111184_a(SharedMonsterAttributes.attackDamage, "bcd3b21e-544b-4f93-a957-5a32d29262ef", 0.15D, 1);
    }

    @Override
    public void performEffect(EntityLivingBase entitylivingbase, int strength) {
        // Этот метод можно использовать для выполнения дополнительного эффекта.
        // В данном случае, эффект урона применится автоматически через атрибуты.
    }

    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
        // Рисуем иконку эффекта в инвентаре
        mc.renderEngine.bindTexture(potionIcon);
        this.drawTexturedRect(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
    }

    // Рисуем иконку на экране
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
}
