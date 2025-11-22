package ru.givler.mbo.potion;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.MoreBeyondOrdinary;

public class Curse extends PotionBasic {

    private static final ResourceLocation potionIcon = new ResourceLocation(MoreBeyondOrdinary.MODID, "textures/gui/curse_icon.png");

    public Curse(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.curse");
        this.func_111184_a(SharedMonsterAttributes.maxHealth, "bcd3b21e-544b-4f93-a957-5a32d29262ef", -0.5D, 1);
    }

    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
        mc.renderEngine.bindTexture(potionIcon);
        this.drawTexturedRect(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
    }
}