package ru.givler.mbo.potion;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.MoreBeyondOrdinary;

public class MeleeDamage extends PotionBasic {

    private static final ResourceLocation potionIcon = new ResourceLocation(MoreBeyondOrdinary.MODID, "textures/gui/damage_boost_icon.png");

    public MeleeDamage(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.melee_damage");
        this.func_111184_a(SharedMonsterAttributes.attackDamage, "bcd3b21e-544b-4f93-a957-5a32d29262ef", 0.20D, 1);
    }

    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
        mc.renderEngine.bindTexture(potionIcon);
        this.drawTexturedRect(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
    }
}
