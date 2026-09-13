package ru.givler.mbo.config;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public final class PlayerPingConfig {
  public static boolean enabled = true;
  public static boolean automaticColor = true;
  public static boolean showVanillaBars = false;
  public static int textColor = 0xA0A0A0;
  public static int xOffset;
  public static String format = "%dms";

  private PlayerPingConfig() {}

  public static void load(File configDirectory) {
    Configuration config =
        new Configuration(new File(configDirectory, "MoreBeyondOrdinary/client.cfg"));
    try {
      config.load();
      String category = "playerListPing";
      enabled =
          config.get(category, "enabled", enabled, "Show each player's ping in the player list.")
              .getBoolean(enabled);
      automaticColor =
          config
              .get(category, "automaticColor", automaticColor, "Color the text based on latency.")
              .getBoolean(automaticColor);
      showVanillaBars =
          config
              .get(category, "showVanillaBars", showVanillaBars, "Keep the vanilla ping bars next to the number.")
              .getBoolean(showVanillaBars);
      xOffset =
          config.get(category, "xOffset", xOffset, "Horizontal offset of the ping text.").getInt(xOffset);
      format =
          config
              .get(category, "format", format, "Text format; %d is replaced with the ping value.")
              .getString();
      String color =
          config
              .get(category, "textColor", "0xA0A0A0", "Text color used when automaticColor is disabled.")
              .getString();
      textColor = Integer.decode(color) & 0xFFFFFF;
    } catch (RuntimeException exception) {
      System.err.println("[MBO] Failed to load player-list ping settings: " + exception.getMessage());
    } finally {
      if (config.hasChanged()) config.save();
    }
  }
}
