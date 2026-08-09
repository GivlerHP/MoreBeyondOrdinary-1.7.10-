package ru.givler.mbo.potion;

public class Hex extends PotionBasic {

    public Hex(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.hex");
    }
}
