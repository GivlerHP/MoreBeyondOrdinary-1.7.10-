package ru.givler.mbo.config;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.lighting.ColoredLightRule;

/** User overrides resolve to identity maps once; no registry strings on the render path. */
public final class ColoredLightSources {
  private static File file;
  private static volatile Map<Block, Rules> blocks = Collections.emptyMap();
  private static volatile Map<Item, Rules> items = Collections.emptyMap();
  private static volatile Map<String, ColoredLightRule> entities = Collections.emptyMap();
  public static volatile boolean coloredBurning = true;
  public static volatile int burningBrightness = 15;
  private static final class Rules {
    ColoredLightRule all;
    final Map<Integer, ColoredLightRule> meta = new HashMap<>();
    ColoredLightRule get(int value) { ColoredLightRule exact = meta.get(value); return exact == null ? all : exact; }
    void add(ColoredLightRule rule) { if (rule.metadata < 0) all = rule; else meta.put(rule.metadata, rule); }
  }
  private ColoredLightSources() {}
  public static void configure(File directory) { ColoredLightConfig.configure(directory); file = ColoredLightConfig.file(); }

  public static boolean reload() {
    try {
      JsonObject root = ColoredLightConfig.readOrCreate();
      ColoredLightConfig.applyExtraSources(root);
      Runnable applyOptions = ColoredLightConfig.validate(root);
      Map<Block, Rules> nextBlocks = new IdentityHashMap<>(); Map<Item, Rules> nextItems = new IdentityHashMap<>();
      Map<String, ColoredLightRule> nextEntities = new HashMap<>();
      boolean nextBurning = true; int nextBurningBrightness = 15;
      if (root.has("effects") && root.getAsJsonObject("effects").has("colored_burning")) {
        JsonObject effect = root.getAsJsonObject("effects").getAsJsonObject("colored_burning");
        if (effect.has("enabled")) nextBurning = effect.get("enabled").getAsBoolean();
        if (effect.has("color") && !effect.get("color").getAsString().equalsIgnoreCase("auto")) throw new IllegalArgumentException("Colored burning color must be auto; it follows MBO fire state");
        if (effect.has("brightness")) {
          String level = effect.get("brightness").getAsString();
          if (!level.matches("[0-9]+")) throw new IllegalArgumentException("Burning brightness must be an integer 0..15");
          nextBurningBrightness = Integer.parseInt(level);
          if (nextBurningBrightness > 15) throw new IllegalArgumentException("Burning brightness must be 0..15");
        }
      }
      load(root, "blocks", (id, value) -> {
        ColoredLightRule rule = ColoredLightRule.parse(id, value, true);
        Block block = (Block) Block.blockRegistry.getObject(rule.name);
        if (block == null || !Block.blockRegistry.containsKey(rule.name)) throw new IllegalArgumentException("Unknown block");
        Rules rules = nextBlocks.get(block); if (rules == null) nextBlocks.put(block, rules = new Rules()); rules.add(rule);
      });
      load(root, "items", (id, value) -> {
        ColoredLightRule rule = ColoredLightRule.parse(id, value, false);
        Item item = (Item) Item.itemRegistry.getObject(rule.name);
        if (item == null) throw new IllegalArgumentException("Unknown item");
        Rules rules = nextItems.get(item); if (rules == null) nextItems.put(item, rules = new Rules()); rules.add(rule);
      });
      load(root, "entities", (id, value) -> {
        ColoredLightRule rule = ColoredLightRule.parse(id, value, false);
        if (rule.metadata != -1) throw new IllegalArgumentException("Entities do not have metadata");
        nextEntities.put(rule.name.toLowerCase(Locale.ROOT), rule);
      });
      blocks = nextBlocks; items = nextItems; entities = nextEntities;
      coloredBurning = nextBurning; burningBrightness = nextBurningBrightness;
      applyOptions.run();
      ru.givler.mbo.client.lighting.GardenLanternLight.register();
      return true;
    } catch (IOException | RuntimeException error) {
      System.err.println("[MBO] Keeping previous colored-light rules: " + error); return false;
    }
  }
  private interface Loader { void add(String id, JsonElement value); }
  private static void load(JsonObject root, String category, Loader loader) {
    if (!root.has(category)) return;
    for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject(category).entrySet()) {
      try { loader.add(entry.getKey(), entry.getValue()); }
      catch (RuntimeException error) { System.err.println("[MBO] Ignoring light rule " + category + "/" + entry.getKey() + ": " + error.getMessage()); }
    }
  }
  public static ColoredLightRule block(Block block, int meta) { Rules rules = blocks.get(block); return rules == null ? null : rules.get(meta); }
  public static ColoredLightRule item(ItemStack stack) { if (stack == null) return null; Rules rules = items.get(stack.getItem()); return rules == null ? null : rules.get(stack.getItemDamage()); }
  public static ColoredLightRule entity(String id) { return entities.get(id); }
  public static File file() { return file; }
}
