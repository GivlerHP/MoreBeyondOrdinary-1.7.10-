package ru.givler.mbo.potion;

public class DodgeHit extends PotionBasic {

    public DodgeHit(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.dodge_hit");
    }
}
