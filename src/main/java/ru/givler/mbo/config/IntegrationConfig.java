package ru.givler.mbo.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class IntegrationConfig {

    public static boolean enableBoPDoors = true;
    public static boolean enableBoPTrapdoors = true;
    public static boolean enableVanillaDoors = true;
    public static boolean enableVanillaTrapdoors = true;

    public static void load(File configDir) {
        try {
            File cfgFile = new File(configDir, "MoreBeyondOrdinary/integration.cfg");
            Configuration cfg = new Configuration(cfgFile);
            cfg.load();

            enableBoPDoors = cfg.getBoolean("EnableBoPDoors", "biomesoplenty", true,
                    "Добавлять двери из Biomes O' Plenty");
            enableBoPTrapdoors = cfg.getBoolean("EnableBoPTrapdoors", "biomesoplenty", true,
                    "Добавлять люки из Biomes O' Plenty");

            enableVanillaDoors = cfg.getBoolean("EnableVanillaDoors", "vanilla", true,
                    "Добавлять ванильные двери (ель, береза, джунгли, акация, темный дуб)");
            enableVanillaTrapdoors = cfg.getBoolean("EnableVanillaTrapdoors", "vanilla", true,
                    "Добавлять ванильные люки (ель, береза, джунгли, акация, темный дуб)");

            if (cfg.hasChanged()) cfg.save();

        } catch (Exception e) {
            System.err.println("[MBO] Ошибка загрузки integration.cfg: " + e);
        }
    }
}