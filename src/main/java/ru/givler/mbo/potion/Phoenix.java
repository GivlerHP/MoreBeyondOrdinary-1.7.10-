package ru.givler.mbo.potion;

public class Phoenix extends PotionBasic {

    public Phoenix(int id, boolean isBadEffect, int liquidColor) {
        super(id, isBadEffect, liquidColor);
        this.setPotionName("potion.phoenix");
    }
}
