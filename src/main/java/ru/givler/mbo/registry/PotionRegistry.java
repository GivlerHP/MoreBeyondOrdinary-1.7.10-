package ru.givler.mbo.registry;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.potion.Potion;
import ru.givler.mbo.config.PotionConfig;
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
    public static Potion Luck;


    public static void preLoad(FMLPreInitializationEvent event) {

        PotionConfig.load(event.getModConfigurationDirectory());

        MeleeDamage = new MeleeDamage(PotionConfig.meleeDamageID, false, 0xFF0000);
        Potion.potionTypes[PotionConfig.meleeDamageID] = MeleeDamage;
        Vampirism = new Vampirism (PotionConfig.vampirismID, false, 0x800000);
        Potion.potionTypes[PotionConfig.vampirismID] = Vampirism;
        Dodge = new Dodge(PotionConfig.dodgeID, false, 0x9ACD32);
        Potion.potionTypes[PotionConfig.dodgeID] = Dodge;
        Hex = new Hex(PotionConfig.hexID, false, 0x4B0082);
        Potion.potionTypes[PotionConfig.hexID] = Hex;
        Phoenix = new Phoenix(PotionConfig.phoenixID, false, 0xD2691E);
        Potion.potionTypes[PotionConfig.phoenixID] = Phoenix;
        SixthSense = new SixthSense(PotionConfig.sixthSenseID, false, 0x6B8E23);
        Potion.potionTypes[PotionConfig.sixthSenseID] = SixthSense;
        Magnetism = new Magnetism(PotionConfig.magnetismID, false, 0x8A2BE2);
        Potion.potionTypes[PotionConfig.magnetismID] = Magnetism;
        BashStun = new BashStun(PotionConfig.bashStunID, true, 0x8B4513);
        Potion.potionTypes[PotionConfig.bashStunID] = BashStun;
        ApplyStun = new ApplyStun(PotionConfig.applyStunID, false, 0xA52A2A);
        Potion.potionTypes[PotionConfig.applyStunID] = ApplyStun;
        Vulnerability = new Vulnerability(PotionConfig.vulnerabilityID, true, 0x708090);
        Potion.potionTypes[PotionConfig.vulnerabilityID] = Vulnerability;
        DodgeHit = new DodgeHit(PotionConfig.dodgeHitID, true, 0x808000);
        Potion.potionTypes[PotionConfig.dodgeHitID] = DodgeHit;
        Disarm = new Disarm(PotionConfig.disarmID, true, 0xF8F8FF);
        Potion.potionTypes[PotionConfig.disarmID] = Disarm;
        Thorns = new Thorns(PotionConfig.thornsID, false, 0x00BFFF);
        Potion.potionTypes[PotionConfig.thornsID] = Thorns;
        Curse = new Curse(PotionConfig.curseID, true, 0x5B1E31);
        Potion.potionTypes[PotionConfig.curseID] = Curse;
        Luck = new Luck(PotionConfig.luckID, false, 0x3CB371);
        Potion.potionTypes[PotionConfig.luckID] = Luck;
    }
}

