package ru.givler.mbo.potion;

import net.minecraft.entity.SharedMonsterAttributes;

public class Frost extends PotionBasic {
    public Frost(int id, boolean harmful, int colour) {
        super(id, harmful, colour);
        setPotionName("potion.frost");
        func_111184_a(SharedMonsterAttributes.movementSpeed,
                "be4ca40a-62a8-4498-b795-9c557c615b82", -0.5D, 2);
    }
}
