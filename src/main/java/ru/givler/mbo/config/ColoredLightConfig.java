package ru.givler.mbo.config;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public final class ColoredLightConfig {
  public static boolean enabled = true;
  public static int radius = 16, scanBudget = 2048, propagationBudget = 4096, refreshTicks = 2;
  public static int urgentScanBudget = 32768, urgentPropagationBudget = 32768;
  public static float strength = 1F, colorFalloff = 0.7F, brightnessGain = 1F;
  public static boolean dynamicLights = true;
  public static int dynamicLimit = 4, dynamicInterval = 4;
  private ColoredLightConfig() {}

  public static void load(File directory) {
    Configuration config = new Configuration(new File(directory, "MoreBeyondOrdinary/colored-light.cfg"));
    config.load();
    enabled = config.get("lighting", "enabled", true, "Client-side block lighting tint; no shader pack required.").getBoolean(true);
    radius = config.getInt("radius", "lighting", 16, 8, 24, "Discovery range hint in blocks, rounded to persistent sections with neighboring inputs.");
    scanBudget = config.getInt("scanBudget", "lighting", 2048, 256, 8192, "Maximum block reads per client tick.");
    propagationBudget = config.getInt("propagationBudget", "lighting", 4096, 256, 16384, "Maximum incremental RGB cell updates per client tick.");
    net.minecraftforge.common.config.Property refresh = config.get("lighting", "refreshTicks", 2, "Pause between background scans. Block edits bypass this pause.");
    if (refresh.getInt() == 20) refresh.set(2); // Migrate the initial prototype's default.
    refreshTicks = Math.max(1, Math.min(200, refresh.getInt()));
    urgentScanBudget = config.getInt("urgentScanBudget", "lighting", 32768, 2048, 65536, "Block-read budget for newly discovered sections.");
    urgentPropagationBudget = config.getInt("urgentPropagationBudget", "lighting", 32768, 4096, 65536, "Incremental RGB update budget after edits or loading sections.");
    net.minecraftforge.common.config.Property saturation = config.get("lighting", "strength", 1F, "Color saturation 0..1.");
    if (Math.abs(saturation.getDouble() - 0.85) < 0.00001) saturation.set(1D);
    strength = Math.max(0, Math.min(1, (float) saturation.getDouble()));
    colorFalloff = config.getFloat("colorFalloff", "lighting", 0.7F, 0.25F, 2F, "Exponent of saturation falloff. Below 1 keeps distant colors more vivid.");
    brightnessGain = config.getFloat("brightnessGain", "lighting", 1F, 0F, 2F, "Visual colored-light brightness multiplier; 1 follows source brightness.");
    dynamicLights = config.get("dynamic", "enabled", true, "Colored held items, dropped items and registered entities.").getBoolean(true);
    dynamicLimit = config.getInt("maxSources", "dynamic", 4, 0, 8, "Nearest moving sources. Keep low to limit chunk rebuilds in the fallback renderer.");
    dynamicInterval = config.getInt("updateTicks", "dynamic", 4, 1, 20, "Fallback moving-light update interval; shader positions update every render frame.");
    if (config.hasChanged()) config.save();
  }
}
