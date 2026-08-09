package ru.givler.mbo.potion;

public class Luck extends PotionBasic {

    public Luck(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.miner_luck");
    }
}
