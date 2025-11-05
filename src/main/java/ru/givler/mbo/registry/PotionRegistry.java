package ru.givler.mbo.registry;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.potion.Potion;
import ru.givler.mbo.potion.*;

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
    public static Potion Disarm;
    public static Potion Thorns;
    public static Potion Curse;


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
        SixthSense = new SixthSense(60, false, 0x6B8E23);
        Potion.potionTypes[60] = SixthSense;
        Magnetism = new Magnetism(61, false, 0x8A2BE2);
        Potion.potionTypes[61] = Magnetism;
        BashStun = new BashStun(62, true, 0x8B4513);
        Potion.potionTypes[62] = BashStun;
        ApplyStun = new ApplyStun(63, false, 0xA52A2A);
        Potion.potionTypes[63] = ApplyStun;
        Vulnerability = new Vulnerability(64, true, 0x708090);
        Potion.potionTypes[64] = Vulnerability;
        DodgeHit = new DodgeHit(65, true, 0x808000);
        Potion.potionTypes[65] = DodgeHit;
        Disarm = new Disarm(66, true, 0xF8F8FF);
        Potion.potionTypes[66] = Disarm;
        Thorns = new Thorns(67, false, 0x00BFFF);
        Potion.potionTypes[67] = Thorns;
        Curse = new Curse(68, true, 0x5B1E31);
        Potion.potionTypes[68] = Curse;
    }
}

