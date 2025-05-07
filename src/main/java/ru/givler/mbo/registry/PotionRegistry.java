package ru.givler.mbo.registry;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.potion.Potion;
import ru.givler.mbo.potion.Vampirism;
import ru.givler.mbo.potion.MeleeDamage;
import ru.givler.mbo.potion.Dodge;

public class PotionRegistry {

    public static void preLoad(FMLInitializationEvent event) {
        Potion MeleeDamage = new MeleeDamage(26, false, 0xFF0000);
        Potion.potionTypes[26] = MeleeDamage;
        Potion Vampirism = new Vampirism (27, false, 0x800000);
        Potion.potionTypes[27] = Vampirism;
        Potion Dodge = new Dodge(28, false, 0x9ACD32);
        Potion.potionTypes[28] = Dodge;
    }
}

