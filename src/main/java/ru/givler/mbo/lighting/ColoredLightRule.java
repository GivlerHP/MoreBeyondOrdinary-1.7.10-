package ru.givler.mbo.lighting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** Parsed once at configuration load, never while emitting vertices. */
public final class ColoredLightRule {
  public final String name;
  public final int metadata, color, brightness;
  private ColoredLightRule(String name, int metadata, int color, int brightness) {
    this.name = name; this.metadata = metadata; this.color = color; this.brightness = brightness;
  }
  public static ColoredLightRule parse(String id, JsonElement value, boolean block) {
    String[] parts = id.split(":", -1);
    if (parts.length < 2 || parts.length > 3 || !parts[0].matches("[A-Za-z0-9_.-]+") || !parts[1].matches("[A-Za-z0-9_./-]+")) throw new IllegalArgumentException("Expected modid:name[:meta]");
    int meta = parts.length == 3 ? Integer.parseInt(parts[2]) : -1;
    if (meta < -1 || (parts.length == 3 && meta < 0) || (block && meta > 15)) throw new IllegalArgumentException("Invalid metadata");
    JsonObject object = value.getAsJsonObject();
    String hex = object.get("color").getAsString().trim();
    if (hex.startsWith("#")) hex = hex.substring(1);
    else if (hex.startsWith("0x") || hex.startsWith("0X")) hex = hex.substring(2);
    if (!hex.matches("[a-fA-F0-9]{6}")) throw new IllegalArgumentException("Color must be #RRGGBB");
    int level = -1;
    if (object.has("brightness")) {
      String raw = object.get("brightness").getAsString();
      if (!raw.equalsIgnoreCase("auto")) {
        if (!raw.matches("[0-9]+")) throw new IllegalArgumentException("Brightness must be an integer 0..15 or auto");
        level = Integer.parseInt(raw);
        if (level > 15) throw new IllegalArgumentException("Brightness must be 0..15");
      }
    }
    return new ColoredLightRule(parts[0] + ":" + parts[1], meta, Integer.parseInt(hex, 16), level);
  }
  public int emission(int automaticLevel) { return ColoredLightApi.emission(color, brightness < 0 ? automaticLevel : brightness); }
}
