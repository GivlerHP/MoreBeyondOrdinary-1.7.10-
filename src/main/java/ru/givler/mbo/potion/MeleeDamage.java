package ru.givler.mbo.potion;

import net.minecraft.entity.SharedMonsterAttributes;

public class MeleeDamage extends PotionBasic {

    public MeleeDamage(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.melee_damage");
        this.func_111184_a(SharedMonsterAttributes.attackDamage, "bcd3b21e-544b-4f93-a957-5a32d29262ef", 0.20D, 1);
    }
}
