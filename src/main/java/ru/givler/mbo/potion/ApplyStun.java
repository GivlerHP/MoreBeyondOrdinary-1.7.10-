package ru.givler.mbo.potion;

public class ApplyStun extends PotionBasic {

    public ApplyStun(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.apply_stun");
    }
}

