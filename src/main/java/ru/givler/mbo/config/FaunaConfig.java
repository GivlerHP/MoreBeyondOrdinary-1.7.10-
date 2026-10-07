package ru.givler.mbo.config;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.EntityRegistry;
import java.io.File;
import java.util.*;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.config.Configuration;

/** Server-side spawn settings shared by all backported fauna. */
public final class FaunaConfig {
  private static final Map<String, Rule> rules = new LinkedHashMap<String, Rule>();
  private static final Set<String> registered = new HashSet<String>();

  private FaunaConfig() {}

  public static void load(File directory) {
    Configuration config = new Configuration(new File(directory, "MoreBeyondOrdinary/fauna.cfg"));
    config.load();
    rules.clear();
    registered.clear();
    try {
      add(config, "rabbit", 6, 1, 3, 0, 255, 9, "PLAINS", "FOREST", "SNOWY", "SANDY");
      add(config, "turtle", 5, 2, 5, 0, 255, 9, "BEACH");
      add(config, "cod", 15, 3, 6, 0, 255, 0, "OCEAN");
      add(config, "salmon", 15, 1, 5, 0, 255, 0, "RIVER", "OCEAN");
      add(config, "tropical_fish", 25, 8, 8, 0, 255, 0, "OCEAN");
      add(config, "pufferfish", 5, 1, 3, 0, 255, 0, "OCEAN");
      add(config, "fox", 8, 2, 4, 0, 255, 9, "CONIFEROUS");
      add(config, "axolotl", 5, 1, 3, 0, 63, 0, "*");
      add(config, "glow_squid", 10, 2, 4, 0, 30, 0, "*");
      add(config, "frog", 10, 2, 5, 0, 255, 9, "SWAMP");
      add(config, "tadpole", 0, 1, 1, 0, 255, 0); // Develops from frogspawn by default.
      add(config, "camel", 1, 1, 1, 0, 255, 9, "SANDY");
      add(config, "polar_bear", 1, 1, 2, 0, 255, 9, "SNOWY");
      add(config, "panda", 1, 1, 2, 0, 255, 9, "JUNGLE");
      add(config, "goat", 5, 1, 3, 0, 255, 9, "MOUNTAIN");
      add(config, "guardian", 8, 2, 4, 0, 63, 0, "OCEAN");
    } finally {
      if (config.hasChanged()) config.save();
    }
  }

  private static void add(
      Configuration config,
      String id,
      int weight,
      int minGroup,
      int maxGroup,
      int minY,
      int maxY,
      int minLight,
      String... types) {
    Rule rule = new Rule();
    rule.enabled =
        config.getBoolean(
            "enabled", id, weight > 0, "Enable natural spawning; existing entities remain.");
    rule.weight =
        config.getInt(
            "weight", id, weight, 0, 1000, "Relative spawn weight; 0 disables natural spawning.");
    rule.minGroup = config.getInt("minGroup", id, minGroup, 1, 32, "Minimum group size.");
    rule.maxGroup = config.getInt("maxGroup", id, maxGroup, 1, 32, "Maximum group size.");
    if (rule.maxGroup < rule.minGroup) {
      FMLLog.warning("[MBO fauna] %s maxGroup below minGroup; using minGroup.", id);
      rule.maxGroup = rule.minGroup;
    }
    rule.whitelist =
        config.getStringList(
            "biomeWhitelist",
            id,
            new String[0],
            "Case-insensitive biome names. Non-empty whitelist replaces selection by biomeTypes.");
    rule.blacklist =
        config.getStringList(
            "biomeBlacklist",
            id,
            new String[0],
            "Case-insensitive biome names. Blacklist takes priority over whitelist.");
    rule.typeNames =
        config.getStringList(
            "biomeTypes", id, types, "Forge biome types; any matching type allows spawning.");
    rule.dimensions =
        config.get("" + id, "dimensions", new int[] {0}, "Allowed dimension IDs.").getIntList();
    rule.minY =
        config.getInt(
            "minY", id, minY, -4096, 4096, "Minimum spawn Y; may be negative with CaveAbyss.");
    rule.maxY = config.getInt("maxY", id, maxY, -4096, 4096, "Maximum spawn Y.");
    if (rule.maxY < rule.minY) {
      FMLLog.warning("[MBO fauna] %s maxY below minY; swapping bounds.", id);
      int swap = rule.minY;
      rule.minY = rule.maxY;
      rule.maxY = swap;
    }
    rule.minLight = config.getInt("minLight", id, minLight, 0, 15, "Minimum light level.");
    rule.maxLight = config.getInt("maxLight", id, 15, rule.minLight, 15, "Maximum light level.");
    for (String type : rule.typeNames) {
      if (type.trim().equals("*")) {
        rule.allBiomes = true;
        continue;
      }
      try {
        rule.types.add(BiomeDictionary.Type.valueOf(type.trim().toUpperCase(Locale.ROOT)));
      } catch (IllegalArgumentException invalid) {
        FMLLog.warning("[MBO fauna] Unknown biome type '%s' for %s.", type, id);
      }
    }
    rules.put(id, rule);
  }

  public static void register(
      String id, Class<? extends EntityLiving> entity, EnumCreatureType creature) {
    Rule rule = rule(id);
    if (!registered.add(id)) return;
    if (!rule.enabled || rule.weight == 0) return;
    for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
      if (biome == null
          || biome.getClass().getName().toLowerCase(Locale.ROOT).contains("divinerpg")) continue;
      if (rule.allowsBiome(biome)) {
        int weight =
            id.equals("rabbit") && BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.SANDY)
                ? Math.max(1, rule.weight / 2)
                : rule.weight;
        EntityRegistry.addSpawn(entity, weight, rule.minGroup, rule.maxGroup, creature, biome);
      }
    }
    validateNames(id, rule.whitelist);
    validateNames(id, rule.blacklist);
  }

  private static void validateNames(String id, String[] names) {
    for (String name : names) {
      boolean found = false;
      for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray())
        if (biome != null && biome.biomeName.equalsIgnoreCase(name.trim())) {
          found = true;
          break;
        }
      if (!found) FMLLog.warning("[MBO fauna] Unknown biome name '%s' for %s.", name, id);
    }
  }

  public static boolean allowsPosition(String id, World world, int x, int y, int z) {
    Rule rule = rule(id);
    if (!rule.enabled || rule.weight == 0 || y < rule.minY || y > rule.maxY) return false;
    boolean dimension = false;
    for (int allowed : rule.dimensions)
      if (world.provider.dimensionId == allowed) {
        dimension = true;
        break;
      }
    if (!dimension || !rule.allowsBiome(world.getBiomeGenForCoords(x, z))) return false;
    int light = world.getFullBlockLightValue(x, y, z);
    return light >= rule.minLight && light <= rule.maxLight;
  }

  private static Rule rule(String id) {
    Rule rule = rules.get(id);
    if (rule == null) throw new IllegalStateException("Fauna settings not loaded: " + id);
    return rule;
  }

  private static final class Rule {
    boolean enabled;
    boolean allBiomes;
    int weight, minGroup, maxGroup, minY, maxY, minLight, maxLight;
    int[] dimensions;
    String[] whitelist, blacklist, typeNames;
    final List<BiomeDictionary.Type> types = new ArrayList<BiomeDictionary.Type>();

    boolean allowsBiome(BiomeGenBase biome) {
      for (String name : blacklist) if (biome.biomeName.equalsIgnoreCase(name.trim())) return false;
      if (whitelist.length > 0) {
        for (String name : whitelist)
          if (biome.biomeName.equalsIgnoreCase(name.trim())) return true;
        return false;
      }
      if (allBiomes) return true;
      for (BiomeDictionary.Type type : types)
        if (BiomeDictionary.isBiomeOfType(biome, type)) return true;
      return false;
    }
  }
}
