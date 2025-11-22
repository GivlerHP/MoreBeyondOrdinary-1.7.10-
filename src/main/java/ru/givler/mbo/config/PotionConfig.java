package ru.givler.mbo.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class PotionConfig {

    public static int meleeDamageID = 26;
    public static int vampirismID = 27;
    public static int dodgeID = 28;
    public static int hexID = 29;
    public static int phoenixID = 30;
    public static int sixthSenseID = 60;
    public static int magnetismID = 61;
    public static int bashStunID = 62;
    public static int applyStunID = 63;
    public static int vulnerabilityID = 64;
    public static int dodgeHitID = 65;
    public static int disarmID = 66;
    public static int thornsID = 67;
    public static int curseID = 68;
    public static int luckID = 69;

    public static void load(File configDir) {
        try {
            File cfgFile = new File(configDir, "MoreBeyondOrdinary/potions.cfg");
            Configuration cfg = new Configuration(cfgFile);
            cfg.load();

            meleeDamageID   = cfg.getInt("MeleeDamage", "potions", 26, 1, 255, "Potion ID");
            vampirismID     = cfg.getInt("Vampirism", "potions", 27, 1, 255, "Potion ID");
            dodgeID         = cfg.getInt("Dodge", "potions", 28, 1, 255, "Potion ID");
            hexID           = cfg.getInt("Hex", "potions", 29, 1, 255, "Potion ID");
            phoenixID       = cfg.getInt("Phoenix", "potions", 30, 1, 255, "Potion ID");
            sixthSenseID    = cfg.getInt("SixthSense", "potions", 60, 1, 255, "Potion ID");
            magnetismID     = cfg.getInt("Magnetism", "potions", 61, 1, 255, "Potion ID");
            bashStunID      = cfg.getInt("BashStun", "potions", 62, 1, 255, "Potion ID");
            applyStunID     = cfg.getInt("ApplyStun", "potions", 63, 1, 255, "Potion ID");
            vulnerabilityID = cfg.getInt("Vulnerability", "potions", 64, 1, 255, "Potion ID");
            dodgeHitID      = cfg.getInt("DodgeHit", "potions", 65, 1, 255, "Potion ID");
            disarmID        = cfg.getInt("Disarm", "potions", 66, 1, 255, "Potion ID");
            thornsID        = cfg.getInt("Thorns", "potions", 67, 1, 255, "Potion ID");
            curseID         = cfg.getInt("Curse", "potions", 68, 1, 255, "Potion ID");
            luckID         = cfg.getInt("Luck", "potions", 69, 1, 255, "Potion ID");

            if (cfg.hasChanged()) cfg.save();

        } catch (Exception e) {
            System.err.println("[MBO] Ошибка загрузки potions.cfg: " + e);
        }
    }
}
