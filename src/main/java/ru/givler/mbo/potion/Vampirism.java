package ru.givler.mbo.potion;

public class Vampirism extends PotionBasic {

    public Vampirism(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.vampirism");
    }
}
