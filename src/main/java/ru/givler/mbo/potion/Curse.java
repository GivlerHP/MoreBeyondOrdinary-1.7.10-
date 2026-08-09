package ru.givler.mbo.potion;

import net.minecraft.entity.SharedMonsterAttributes;

public class Curse extends PotionBasic {

    public Curse(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.curse");
        this.func_111184_a(SharedMonsterAttributes.maxHealth, "bcd3b21e-544b-4f93-a957-5a32d29262ef", -0.5D, 1);
    }
}
