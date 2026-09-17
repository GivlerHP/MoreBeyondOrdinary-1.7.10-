package ru.givler.mbo.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;

public class Decay extends PotionBasic {
    public Decay(int id, boolean harmful, int colour) {
        super(id, harmful, colour);
        setPotionName("potion.decay");
        func_111184_a(SharedMonsterAttributes.movementSpeed,
                "739eec39-70e8-4eef-93fd-4d65cb010c31", -0.2D, 2);
    }

    @Override public boolean isReady(int duration, int amplifier) {
        int interval = 25 >> amplifier;
        return interval <= 0 || duration % interval == 0;
    }

    @Override public void performEffect(EntityLivingBase entity, int amplifier) {
        entity.attackEntityFrom(DamageSource.wither, 1.0F);
    }
}
