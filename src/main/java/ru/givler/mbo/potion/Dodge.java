package ru.givler.mbo.potion;

public class Dodge extends PotionBasic {

    public Dodge(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.dodge");
    }
}
