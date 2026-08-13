package ru.givler.mbo.core;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.registry.PotionRegistry;

public final class LootingPotionHooks {
    private LootingPotionHooks() { }

    public static int addPotionLevel(int enchantmentLevel, EntityLivingBase entity) {
        if (entity == null || PotionRegistry.Looting == null
                || !entity.isPotionActive(PotionRegistry.Looting)) {
            return enchantmentLevel;
        }
        PotionEffect effect = entity.getActivePotionEffect(PotionRegistry.Looting);
        return enchantmentLevel + effect.getAmplifier() + 1;
    }
}
