package ru.givler.mbo.config;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public final class GameplayConfig {
  public static boolean goldenAppleInstantDamage = true;
  public static boolean hideOwnPotionParticles = true;

  private GameplayConfig() {}

  public static void load(File configDirectory) {
    Configuration config =
        new Configuration(new File(configDirectory, "MoreBeyondOrdinary/gameplay.cfg"));
    try {
      config.load();
      goldenAppleInstantDamage =
          config.getBoolean(
              "GoldenAppleInstantDamage",
              "items",
              true,
              "Remove the effects of normal and enchanted golden apples and deal instant damage instead.");
      hideOwnPotionParticles =
          config.getBoolean(
              "HideOwnPotionParticles",
              "client",
              true,
              "Hide your own potion particles in first-person view. Other players still see them.");
    } finally {
      if (config.hasChanged()) config.save();
    }
  }
}
