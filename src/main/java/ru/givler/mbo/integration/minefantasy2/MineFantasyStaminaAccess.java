package ru.givler.mbo.integration.minefantasy2;

import minefantasy.mf2.api.stamina.StaminaBar;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

/** Loaded reflectively only when MineFantasy 2 is installed. */
public final class MineFantasyStaminaAccess {
    private MineFantasyStaminaAccess() {}

    public static IAttributeInstance getMaxBonus(EntityLivingBase entity) {
        return StaminaBar.getMaxStaminaBonusAttribute(entity);
    }

    public static float getMaximum(EntityLivingBase entity) {
        return StaminaBar.getTotalMaxStamina(entity);
    }

    public static float getValue(EntityLivingBase entity) {
        return StaminaBar.getStaminaValue(entity);
    }

    public static void setValue(EntityLivingBase entity, float value) {
        StaminaBar.setStaminaValue(entity, value);
    }

    public static void removeInfinite(EntityLivingBase entity, float value) {
        StaminaBar.removeBuffStaminaInfinite(entity, value);
    }

    public static void modify(EntityLivingBase entity, float value) {
        StaminaBar.modifyStaminaValue(entity, value);
    }
}
