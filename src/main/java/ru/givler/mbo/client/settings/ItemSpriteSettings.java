package ru.givler.mbo.client.settings;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public final class ItemSpriteSettings {
  public enum Mode { FIRST_PERSON, OFF }

  private static Mode mode = Mode.FIRST_PERSON;
  private static Configuration config;

  private ItemSpriteSettings() {}

  public static void load(File directory) {
    config = new Configuration(new File(directory, "MoreBeyondOrdinary/item-rendering.cfg"));
    config.load();
    Mode fallback = Mode.FIRST_PERSON;
    if (config.hasKey("quality", "FirstPersonItemOutline")
        && !config.get("quality", "FirstPersonItemOutline", true).getBoolean(true)) {
      fallback = Mode.OFF;
    }
    String saved = config.getString("ItemOutline", "quality", fallback.name(),
        "Choose where to close seams in flat item sprites.", new String[]{"FIRST_PERSON", "OFF"});
    mode = "OFF".equals(saved) ? Mode.OFF : Mode.FIRST_PERSON;
    config.get("quality", "ItemOutline", fallback.name()).set(mode.name());
    if (config.hasChanged()) config.save();
  }

  public static Mode mode() {
    return mode;
  }

  public static void apply(Mode value) {
    mode = value;
    if (value == Mode.OFF) ru.givler.mbo.client.render.ItemSpriteOutline.clearCache();
    config.get("quality", "ItemOutline", Mode.FIRST_PERSON.name()).set(value.name());
    config.save();
  }
}
