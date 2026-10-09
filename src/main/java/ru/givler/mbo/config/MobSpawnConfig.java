package ru.givler.mbo.config;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.EntityRegistry;
import java.io.File;
import java.util.*;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import ru.givler.mbo.entity.fauna.FaunaHabitats;
import ru.givler.mbo.entity.monster.*;
import ru.givler.mbo.handler.FaunaAquaticSpawner.Group;
import ru.givler.mbo.integration.caveabyss.FaunaCaveAbyss;
import ru.givler.mbo.registry.BlockRegistry;

/** Server-side spawn settings shared by fauna, golems and undead variants. */
public final class MobSpawnConfig {
  private static final Map<String, Rule> rules = new LinkedHashMap<String, Rule>();
  private static final Set<String> registered = new HashSet<String>();

  private static final Map<String, UndeadRule> undeadRules =
      new LinkedHashMap<String, UndeadRule>();
  private static boolean undeadRegistered;

  private static int interval, attempts;
  private static final int[] caps = new int[4];

  private MobSpawnConfig() {}

  public static void load(File directory) {
    Configuration config =
        new Configuration(new File(directory, "MoreBeyondOrdinary/mob_spawning.cfg"));
    config.load();
    migrateLegacyFiles(directory, config);
    migrateDefaults(config);
    migrateBalance(config);
    MobBiomeConfig.load(directory, config);
    rules.clear();
    registered.clear();
    try {
      interval =
          config.getInt(
              "aquaticInterval", "spawning", 20, 1, 1200, "Ticks between aquatic spawn passes.");
      attempts =
          config.getInt(
              "aquaticAttempts",
              "spawning",
              8,
              1,
              128,
              "Candidate positions per category per pass.");
      String[] keys = {"fishCap", "axolotlCap", "glowSquidCap", "monsterCap"};
      int[] defaults = {20, 5, 5, 70};
      for (int i = 0; i < keys.length; i++)
        caps[i] =
            config.getInt(
                keys[i],
                "spawning",
                defaults[i],
                0,
                1000,
                "Quota per 289 loaded chunks; 0 disables spawning. Guardians share the hostile quota.");
      add(config, "rabbit", 4, 2, 3, 0, 255, 9);
      add(config, "turtle", 5, 2, 5, 0, 255, 9);
      add(config, "cod", 10, 3, 6, 15, 255, 0);
      add(config, "salmon", 15, 1, 5, 15, 255, 0);
      add(config, "tropical_fish", 25, 8, 8, 15, 255, 0);
      add(config, "pufferfish", 5, 1, 3, 15, 255, 0);
      add(config, "fox", 8, 2, 4, 0, 255, 9);
      add(config, "axolotl", 10, 4, 6, -50, 63, 0);
      add(config, "glow_squid", 20, 4, 6, -50, 14, 0);
      add(config, "frog", 10, 2, 5, 0, 255, 9);
      add(config, "tadpole", 0, 1, 1, 0, 255, 0);
      add(config, "camel", 1, 1, 1, 0, 255, 9);
      add(config, "polar_bear", 1, 1, 2, 0, 255, 9);
      add(config, "panda", 10, 1, 2, 0, 255, 9);
      add(config, "goat", 5, 1, 3, 0, 255, 9);
      add(config, "guardian", 20, 2, 4, -50, 14, 0);
      add(config, "stone_golem", 20, 1, 1, -4096, 39, 0);
      loadUndead(config);
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
    rule.surfaceBiomes = names(MobBiomeConfig.surface(id));
    rule.caveBiomes = names(MobBiomeConfig.caves(id));
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
    rule.maxLight =
        config.getInt(
            "maxLight",
            id,
            id.equals("glow_squid")
                ? 0
                : id.equals("stone_golem") || id.equals("guardian") || id.equals("axolotl")
                    ? 7
                    : 15,
            rule.minLight,
            15,
            "Maximum light level.");
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
        EntityRegistry.addSpawn(
            entity,
            spawnWeight(id, biome),
            minGroup(id, biome),
            maxGroup(id, biome),
            creature,
            biome);
      }
    }
    validateNames(id, rule.whitelist);
    validateNames(id, rule.blacklist);
  }

  public static int aquaticInterval() {
    return interval;
  }

  public static int aquaticAttempts() {
    return attempts;
  }

  public static int aquaticCap(Group group) {
    return caps[group.ordinal()];
  }

  public static int minimumY(Group group) {
    int result = 255;
    for (String id : species(group))
      if (rule(id).enabled && rule(id).weight > 0) result = Math.min(result, rule(id).minY);
    return Math.max(FaunaCaveAbyss.hasLowerLayer() ? -50 : 0, result);
  }

  public static int maximumY(Group group) {
    int result = -51;
    for (String id : species(group))
      if (rule(id).enabled && rule(id).weight > 0) result = Math.max(result, rule(id).maxY);
    return Math.min(255, result);
  }

  private static String[] species(Group group) {
    switch (group) {
      case FISH:
        return new String[] {"cod", "salmon", "tropical_fish", "pufferfish"};
      case AXOLOTLS:
        return new String[] {"axolotl"};
      case GLOW_SQUID:
        return new String[] {"glow_squid"};
      default:
        return new String[] {"guardian"};
    }
  }

  public static int spawnWeight(String id, BiomeGenBase biome) {
    Rule r = rule(id);
    if (!r.enabled || !r.allowsBiome(biome)) return 0;
    String name = biome.biomeName.toLowerCase(Locale.ROOT);
    int base = r.weight, denominator = 1, numerator = 1;
    if (id.equals("rabbit")) {
      denominator = 4;
      numerator =
          BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.SANDY)
              ? 12
              : BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.SNOWY)
                  ? 10
                  : name.equals("grove")
                      ? 8
                      : name.equals("meadow") || name.equals("cherry blossom grove") ? 2 : 4;
    } else if (id.equals("cod") && name.equals("frozenocean")) {
      numerator = 3;
      denominator = 2;
    } else if (id.equals("salmon") && (name.contains("river") || name.contains("lake"))) {
      numerator = 1;
      denominator = 3;
    } else if (id.equals("pufferfish") && (name.equals("coral reef") || name.equals("tropics")))
      numerator = 3;
    else if (id.equals("fox") && !name.contains("taiga") && !name.contains("coniferous"))
      denominator = 2;
    else if (id.equals("frog") && (name.equals("moor") || name.equals("quagmire"))) denominator = 2;
    return base == 0 ? 0 : Math.max(1, base * numerator / denominator);
  }

  public static int minGroup(String id, BiomeGenBase biome) {
    return rule(id).minGroup;
  }

  public static int maxGroup(String id, BiomeGenBase biome) {
    Rule r = rule(id);
    return id.equals("rabbit") && biome.biomeName.equalsIgnoreCase("Meadow") && r.maxGroup == 3
        ? 6
        : r.maxGroup;
  }

  private static void migrateBalance(Configuration config) {
    int version = config.getInt("balanceVersion", "general", 0, 0, 2, "Spawn balance schema.");
    if (version >= 2) return;
    if (version == 1) {
      migrateInt(config, "panda", "weight", 80, 10);
      config.get("general", "balanceVersion", 2).set(2);
      return;
    }
    String[] ids = {
      "rabbit",
      "turtle",
      "cod",
      "salmon",
      "tropical_fish",
      "pufferfish",
      "fox",
      "axolotl",
      "glow_squid",
      "frog",
      "panda",
      "guardian"
    };
    int[] oldWeight = {6, 3, 15, 15, 20, 5, 4, 4, 20, 8, 1, 20},
        newWeight = {4, 5, 10, 15, 25, 5, 8, 10, 20, 10, 10, 20};
    int[] oldMin = {2, 2, 3, 2, 4, 1, 1, 1, 2, 2, 1, 2},
        newMin = {2, 2, 3, 1, 8, 1, 2, 4, 4, 2, 1, 2};
    int[] oldMax = {3, 3, 5, 4, 6, 2, 2, 2, 3, 3, 2, 3},
        newMax = {3, 5, 6, 5, 8, 3, 4, 6, 6, 5, 2, 4};
    for (int i = 0; i < ids.length; i++) {
      migrateInt(config, ids[i], "weight", oldWeight[i], newWeight[i]);
      migrateInt(config, ids[i], "minGroup", oldMin[i], newMin[i]);
      migrateInt(config, ids[i], "maxGroup", oldMax[i], newMax[i]);
    }
    config.get("general", "balanceVersion", 2).set(2);
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
    int light =
        id.equals("stone_golem")
            ? world.getBlockLightValue(x, y, z)
            : world.getFullBlockLightValue(x, y, z);
    if (light < rule.minLight || light > rule.maxLight) return false;
    String biome = world.getBiomeGenForCoords(x, z).biomeName.toLowerCase(Locale.ROOT);
    return FaunaHabitats.allows(
        id,
        world,
        x,
        y,
        z,
        rule.surfaceBiomes.contains("*") || rule.surfaceBiomes.contains(biome),
        rule.caveBiomes.contains("*") || rule.caveBiomes.contains(biome));
  }

  private static Set<String> names(String[] values) {
    Set<String> result = new HashSet<String>();
    for (String value : values) result.add(value.trim().toLowerCase(Locale.ROOT));
    return result;
  }

  /** Update only the old generated broad-biome defaults; explicit whitelists remain intact. */
  private static void migrateDefaults(Configuration config) {
    int version =
        config.getInt("habitatVersion", "general", 0, 0, 3, "Biome habitat defaults schema.");
    if (version >= 3) return;
    String[] ids = {
      "rabbit",
      "turtle",
      "cod",
      "salmon",
      "tropical_fish",
      "pufferfish",
      "fox",
      "axolotl",
      "glow_squid",
      "frog",
      "tadpole",
      "camel",
      "polar_bear",
      "panda",
      "goat",
      "guardian"
    };
    if (version == 2) {
      for (String id : ids) {
        if (!config.hasKey(id, "biomeBlacklist")) continue;
        String[] blacklist = config.getCategory(id).get("biomeBlacklist").getStringList();
        String[] defaults = MobBiomeConfig.all(id);
        if (defaults.length > 0 && names(blacklist).equals(names(defaults))) {
          config.getCategory(id).get("biomeBlacklist").set(new String[0]);
          FMLLog.info("[MBO fauna] Repaired erroneous generated blacklist for %s.", id);
        }
      }
      config.get("general", "habitatVersion", 3).set(3);
      return;
    }
    for (String id : ids) {
      if (!config.hasCategory(id) || !config.hasKey(id, "biomeWhitelist")) continue;
      if (config.getCategory(id).get("biomeWhitelist").getStringList().length != 0) continue;
      config.getCategory(id).get("biomeWhitelist").set(MobBiomeConfig.all(id));
      if (config.hasKey(id, "biomeTypes"))
        config.getCategory(id).get("biomeTypes").set(new String[0]);
      String[] ordered = {
        "rabbit",
        "turtle",
        "cod",
        "salmon",
        "tropical_fish",
        "pufferfish",
        "fox",
        "axolotl",
        "glow_squid",
        "frog",
        "tadpole",
        "camel",
        "polar_bear",
        "panda",
        "goat",
        "guardian"
      };
      int[] oldWeights = {6, 5, 15, 15, 25, 5, 8, 5, 10, 10, 0, 1, 1, 1, 5, 8};
      int[] newWeights = {6, 3, 15, 15, 20, 5, 4, 4, 20, 8, 0, 1, 1, 1, 5, 20};
      int[] oldMin = {1, 2, 3, 1, 8, 1, 2, 1, 2, 2, 1, 1, 1, 1, 1, 2};
      int[] newMin = {2, 2, 3, 2, 4, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1, 2};
      int[] oldMax = {3, 5, 6, 5, 8, 3, 4, 3, 4, 5, 1, 1, 2, 2, 3, 4};
      int[] newMax = {3, 3, 5, 4, 6, 2, 2, 2, 3, 3, 1, 1, 2, 2, 3, 3};
      int index = Arrays.asList(ordered).indexOf(id);
      migrateInt(config, id, "weight", oldWeights[index], newWeights[index]);
      migrateInt(config, id, "minGroup", oldMin[index], newMin[index]);
      migrateInt(config, id, "maxGroup", oldMax[index], newMax[index]);
      if (id.equals("guardian") || id.equals("glow_squid") || id.equals("axolotl")) {
        migrateInt(config, id, "minY", 0, -50);
        if (id.equals("guardian")) {
          migrateInt(config, id, "maxY", 63, 14);
          migrateInt(config, id, "weight", 8, 20);
        }
        if (id.equals("glow_squid")) {
          migrateInt(config, id, "maxY", 30, 14);
          migrateInt(config, id, "weight", 10, 20);
        }
      }
      FMLLog.info(
          "[MBO fauna] Migrated %s from broad biome defaults to approved habitat names.", id);
    }
    config.get("general", "habitatVersion", 3).set(3);
  }

  private static void migrateInt(
      Configuration config, String id, String key, int previous, int next) {
    if (config.hasKey(id, key) && config.getCategory(id).get(key).getInt() == previous)
      config.getCategory(id).get(key).set(next);
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
    Set<String> surfaceBiomes, caveBiomes;
    final List<BiomeDictionary.Type> types = new ArrayList<BiomeDictionary.Type>();

    boolean allowsBiome(BiomeGenBase biome) {
      for (String name : blacklist) if (biome.biomeName.equalsIgnoreCase(name.trim())) return false;
      if (whitelist.length > 0) {
        for (String name : whitelist)
          if (biome.biomeName.equalsIgnoreCase(name.trim())) return true;
        return false;
      }
      if (allBiomes) return true;
      if (types.isEmpty()) {
        String name = biome.biomeName.toLowerCase(Locale.ROOT);
        return surfaceBiomes.contains("*")
            || caveBiomes.contains("*")
            || surfaceBiomes.contains(name)
            || caveBiomes.contains(name);
      }
      for (BiomeDictionary.Type type : types)
        if (BiomeDictionary.isBiomeOfType(biome, type)) return true;
      return false;
    }
  }

  private static void loadUndead(Configuration config) {
    undeadRules.clear();
    undeadRegistered = false;
    addUndead(config, "husk", EntityMBOHusk.class, EntityZombie.class, 80);
    addUndead(config, "parched", EntityMBOParched.class, EntitySkeleton.class, 50);
    addUndead(config, "stray", EntityMBOStray.class, EntitySkeleton.class, 80);
    addUndead(config, "bogged", EntityMBOBogged.class, EntitySkeleton.class, 30);
  }

  /** Import once; existing settings in the new file always take priority. */
  private static void migrateLegacyFiles(File directory, Configuration config) {
    if (config.getInt(
            "legacyImportVersion", "general", 0, 0, 1, "Legacy spawn configuration migration.")
        >= 1) return;
    for (String filename : new String[] {"fauna.cfg", "undead.cfg"}) {
      File file = new File(directory, "MoreBeyondOrdinary/" + filename);
      if (!file.exists()) continue;
      Configuration legacy = new Configuration(file);
      legacy.load();
      for (String category : legacy.getCategoryNames()) {
        for (Map.Entry<String, Property> entry : legacy.getCategory(category).entrySet()) {
          if (!config.hasKey(category, entry.getKey()))
            config.getCategory(category).put(entry.getKey(), entry.getValue());
        }
      }
    }
    config.get("general", "legacyImportVersion", 1).set(1);
  }

  private static void addUndead(
      Configuration config,
      String id,
      Class<? extends EntityLiving> type,
      Class<? extends EntityLiving> base,
      int share) {
    UndeadRule rule = new UndeadRule();
    rule.type = type;
    rule.base = base;
    rule.enabled =
        config.getBoolean(
            "enabled", id, true, "Enable natural spawning; eggs and existing mobs still work.");
    rule.share =
        config.getInt(
            "spawnShare",
            id,
            share,
            0,
            100,
            "Percentage of the existing zombie/skeleton spawn weight assigned to this variant.");
    if (config.hasKey(id, "biomeWhitelist")) {
      MobBiomeConfig.migrateUndead(
          id, config.getCategory(id).get("biomeWhitelist").getStringList());
      config.getCategory(id).remove("biomeWhitelist");
    }
    rule.biomes = MobBiomeConfig.all(id);
    rule.blacklist =
        config.getStringList(
            "biomeBlacklist", id, new String[0], "Excluded biome names override the whitelist.");
    rule.dimensions =
        config.get(id, "dimensions", new int[] {0}, "Allowed dimension IDs.").getIntList();
    undeadRules.put(id, rule);
  }

  public static boolean allowsUndead(String id, EntityLiving entity) {
    UndeadRule rule = undeadRules.get(id);
    if (rule == null || !rule.enabled || rule.share == 0) return false;
    boolean dimension = false;
    for (int value : rule.dimensions)
      if (value == entity.worldObj.provider.dimensionId) dimension = true;
    if (!dimension
        || !rule.biome(
            entity.worldObj.getBiomeGenForCoords(
                MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posZ))))
      return false;
    int x = MathHelper.floor_double(entity.posX),
        y = MathHelper.floor_double(entity.boundingBox.minY),
        z = MathHelper.floor_double(entity.posZ);
    if (id.equals("stray"))
      while (y < 255 && entity.worldObj.getBlock(x, y + 1, z) == BlockRegistry.powderSnow) y++;
    boolean surface = entity.worldObj.canBlockSeeTheSky(x, y, z);
    if (!id.equals("bogged") && !surface) return false;
    String biome = entity.worldObj.getBiomeGenForCoords(x, z).biomeName;
    return contains(surface ? MobBiomeConfig.surface(id) : MobBiomeConfig.caves(id), biome);
  }

  public static void registerUndeadSpawns() {
    if (undeadRegistered) return;
    undeadRegistered = true;
    for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
      if (biome == null) continue;
      List<SpawnListEntry> entries = biome.getSpawnableList(EnumCreatureType.monster);
      List<SpawnListEntry> original = new ArrayList<SpawnListEntry>(entries);
      for (SpawnListEntry entry : original) {
        if (entry.entityClass != EntityZombie.class && entry.entityClass != EntitySkeleton.class)
          continue;
        int remaining = entry.itemWeight;
        List<SpawnListEntry> variants = new ArrayList<SpawnListEntry>();
        for (UndeadRule rule : undeadRules.values()) {
          if (!rule.enabled || rule.base != entry.entityClass || !rule.biome(biome)) continue;
          int weight = Math.min(remaining, entry.itemWeight * rule.share / 100);
          if (weight == 0) continue;
          remaining -= weight;
          variants.add(
              new SpawnListEntry(rule.type, weight, entry.minGroupCount, entry.maxGroupCount));
        }
        if (variants.isEmpty()) continue;
        int index = entries.indexOf(entry);
        entries.remove(index);
        if (remaining > 0)
          entries.add(
              index++,
              new SpawnListEntry(
                  entry.entityClass, remaining, entry.minGroupCount, entry.maxGroupCount));
        entries.addAll(index, variants);
      }
    }
  }

  private static boolean contains(String[] values, String name) {
    for (String value : values)
      if (value.trim().equals("*") || value.trim().equalsIgnoreCase(name)) return true;
    return false;
  }

  private static final class UndeadRule {
    Class<? extends EntityLiving> type, base;
    boolean enabled;
    int share;
    String[] biomes, blacklist;
    int[] dimensions;

    boolean biome(BiomeGenBase biome) {
      return contains(biomes, biome.biomeName) && !contains(blacklist, biome.biomeName);
    }
  }
}
