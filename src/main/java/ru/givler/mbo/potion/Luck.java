package ru.givler.mbo.potion;

import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.MoreBeyondOrdinary;

public class Luck extends PotionBasic {

    private static final ResourceLocation potionIcon = new ResourceLocation(MoreBeyondOrdinary.MODID, "textures/gui/luck_icon.png");

    public Luck(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.miner_luck");
    }

    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
        mc.renderEngine.bindTexture(potionIcon);
        this.drawTexturedRect(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
    }
}
