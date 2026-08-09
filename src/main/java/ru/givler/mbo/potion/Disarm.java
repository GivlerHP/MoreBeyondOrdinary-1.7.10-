package ru.givler.mbo.potion;

public class Disarm extends PotionBasic {

    public Disarm(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.disarm");
    }
}
