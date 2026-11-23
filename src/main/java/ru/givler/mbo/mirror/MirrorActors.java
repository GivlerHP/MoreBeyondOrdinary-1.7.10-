package ru.givler.mbo.mirror;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;

public final class MirrorActors {
    private MirrorActors() {}

    public static boolean isVisibleKind(EntityLivingBase entity) {
        return entity instanceof EntityPlayer || entity instanceof EntityAnimal
            || entity instanceof EntityWaterMob || entity instanceof EntityAmbientCreature;
    }
}
