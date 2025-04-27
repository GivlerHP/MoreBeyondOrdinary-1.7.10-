package ru.givler.mbo.registry;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.potion.Potion;
import ru.givler.mbo.potion.DamageBoost;

public class PotionRegistry {

    public static void preLoad(FMLInitializationEvent event) {
        Potion damageBoostPotion = new DamageBoost(30, false, 0xFF0000);
        Potion.potionTypes[30] = damageBoostPotion;
    }
}

