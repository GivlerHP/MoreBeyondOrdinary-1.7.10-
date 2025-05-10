package ru.givler.mbo.registry;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.potion.Potion;
import ru.givler.mbo.potion.Hex;
import ru.givler.mbo.potion.Vampirism;
import ru.givler.mbo.potion.MeleeDamage;
import ru.givler.mbo.potion.Dodge;
import ru.givler.mbo.potion.Phoenix;

public class PotionRegistry {

    public static Potion MeleeDamage;
    public static Potion Vampirism;
    public static Potion Dodge;
    public static Potion Hex;
    public static Potion Phoenix;

    public static void preLoad(FMLPreInitializationEvent event) {
        MeleeDamage = new MeleeDamage(26, false, 0xFF0000);
        Potion.potionTypes[26] = MeleeDamage;
        Vampirism = new Vampirism (27, false, 0x800000);
        Potion.potionTypes[27] = Vampirism;
        Dodge = new Dodge(28, false, 0x9ACD32);
        Potion.potionTypes[28] = Dodge;
        Hex = new Hex(29, false, 0x4B0082 );
        Potion.potionTypes[29] = Hex;
        Phoenix = new Phoenix(30, false, 0x4B0082 );
        Potion.potionTypes[30] = Phoenix;
    }
}

