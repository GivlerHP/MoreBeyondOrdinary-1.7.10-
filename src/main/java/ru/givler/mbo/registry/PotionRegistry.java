package ru.givler.mbo.registry;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.potion.Potion;
import ru.givler.mbo.potion.Hex;
import ru.givler.mbo.potion.Vampirism;
import ru.givler.mbo.potion.MeleeDamage;
import ru.givler.mbo.potion.Dodge;
import ru.givler.mbo.potion.Phoenix;
import ru.givler.mbo.potion.SixthSense;
import ru.givler.mbo.potion.Magnetism;
import ru.givler.mbo.potion.BashStun;
import ru.givler.mbo.potion.ApplyStun;
import ru.givler.mbo.potion.Vulnerability;
import ru.givler.mbo.potion.DodgeHit;

public class PotionRegistry {

    public static Potion MeleeDamage;
    public static Potion Vampirism;
    public static Potion Dodge;
    public static Potion Hex;
    public static Potion Phoenix;
    public static Potion SixthSense;
    public static Potion Magnetism;
    public static Potion BashStun;
    public static Potion ApplyStun;
    public static Potion Vulnerability;
    public static Potion DodgeHit;


    public static void preLoad(FMLPreInitializationEvent event) {
        MeleeDamage = new MeleeDamage(26, false, 0xFF0000);
        Potion.potionTypes[26] = MeleeDamage;
        Vampirism = new Vampirism (27, false, 0x800000);
        Potion.potionTypes[27] = Vampirism;
        Dodge = new Dodge(28, false, 0x9ACD32);
        Potion.potionTypes[28] = Dodge;
        Hex = new Hex(29, false, 0x4B0082);
        Potion.potionTypes[29] = Hex;
        Phoenix = new Phoenix(30, false, 0xD2691E);
        Potion.potionTypes[30] = Phoenix;
        SixthSense = new SixthSense(50, false, 0x6B8E23);
        Potion.potionTypes[50] = SixthSense;
        Magnetism = new Magnetism(51, false, 0x8A2BE2);
        Potion.potionTypes[51] = Magnetism;
        BashStun = new BashStun(52, true, 0x8B4513);
        Potion.potionTypes[52] = BashStun;
        ApplyStun = new ApplyStun(53, false, 0xA52A2A);
        Potion.potionTypes[53] = ApplyStun;
        Vulnerability = new Vulnerability(54, true, 0x708090);
        Potion.potionTypes[54] = Vulnerability;
        DodgeHit = new DodgeHit(55, true, 0x808000);
        Potion.potionTypes[55] = DodgeHit;


    }
}

