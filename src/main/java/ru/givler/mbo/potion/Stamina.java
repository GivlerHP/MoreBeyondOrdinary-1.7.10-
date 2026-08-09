package ru.givler.mbo.potion;

public class Stamina extends PotionBasic {

    public Stamina(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.stamina");
    }
}
