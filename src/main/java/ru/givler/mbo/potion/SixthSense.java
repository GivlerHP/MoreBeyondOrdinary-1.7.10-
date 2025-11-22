package ru.givler.mbo.potion;

import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.MoreBeyondOrdinary;

public class SixthSense extends PotionBasic {

    private static final ResourceLocation potionIcon = new ResourceLocation(MoreBeyondOrdinary.MODID, "textures/gui/sixth_sense_icon.png");

    public static int potionId;

    public SixthSense(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.sixth_sense");
        potionId = id;
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }

    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
        mc.renderEngine.bindTexture(potionIcon);
        this.drawTexturedRect(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
    }
}
