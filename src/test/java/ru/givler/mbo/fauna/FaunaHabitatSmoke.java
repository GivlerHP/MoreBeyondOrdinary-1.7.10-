package ru.givler.mbo.fauna;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.Configuration;
import ru.givler.mbo.config.MobBiomeConfig;
import ru.givler.mbo.config.MobSpawnConfig;
import ru.givler.mbo.integration.caveabyss.FaunaCaveAbyss;

/** Checks the configured profiles, not merely their JSON representation. */
public final class FaunaHabitatSmoke {
  public static void check(FaunaSmoke.AquaticWorld world) throws Exception {
    if (Arrays.asList(MobBiomeConfig.all("goat")).contains("Crag")
        || Arrays.asList(MobBiomeConfig.all("goat")).contains("Alps Forest"))
      throw new AssertionError("Excluded goat biomes returned");
    if (!Arrays.asList(MobBiomeConfig.all("panda"))
        .equals(Arrays.asList("Bamboo Forest", "BambooForest")))
      throw new AssertionError("Pandas must be restricted to both bamboo forests");
    for (String fish : new String[] {"tropical_fish", "pufferfish"})
      if (!Arrays.asList(MobBiomeConfig.surface(fish))
          .containsAll(Arrays.asList("Ocean", "Deep Ocean")))
        throw new AssertionError("Approved ocean fish missing");
    for (String biome : new String[] {"Bayou", "Rainforest", "Sacred Springs"})
      if (Arrays.asList(MobBiomeConfig.all("axolotl")).contains(biome))
        throw new AssertionError("Excluded axolotl biome returned");
    if (Arrays.asList(MobBiomeConfig.all("frog")).contains("Bog"))
      throw new AssertionError("Bog frogs returned");

    File freshDirectory = new File("build/fauna-fresh-config-" + UUID.randomUUID());
    MobSpawnConfig.load(freshDirectory);
    Configuration fresh =
        new Configuration(new File(freshDirectory, "MoreBeyondOrdinary/mob_spawning.cfg"));
    fresh.load();
    for (String id :
        new String[] {
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
        })
      if (fresh.getCategory(id).get("biomeBlacklist").getStringList().length != 0)
        throw new AssertionError("Fresh configuration must not blacklist approved habitats: " + id);
    fresh.get("general", "habitatVersion", 3).set(2);
    fresh.getCategory("guardian").get("biomeBlacklist").set(MobBiomeConfig.all("guardian"));
    fresh.getCategory("fox").get("biomeBlacklist").set(new String[] {"Taiga"});
    fresh.save();
    MobSpawnConfig.load(freshDirectory);
    fresh.load();
    if (fresh.getCategory("guardian").get("biomeBlacklist").getStringList().length != 0)
      throw new AssertionError("Broken version-2 generated blacklist was not repaired");
    if (!Arrays.equals(
        fresh.getCategory("fox").get("biomeBlacklist").getStringList(), new String[] {"Taiga"}))
      throw new AssertionError("Repair replaced a custom blacklist");

    world.habitatFixture = true;
    world.habitatBiome = new BiomeGenBase(240) {};
    world.habitatBiome.biomeName = "Deep Ocean";
    world.roofY = 15;
    world.habitatLight = 0;
    if (spawn("guardian", world, 10)
        || !spawn("glow_squid", world, 10)
        || !spawn("axolotl", world, 10))
      throw new AssertionError(
          "Without CaveAbyss, an ordinary ocean cave must not allow guardians");
    world.roofY = -1000;
    if (spawn("guardian", world, 10)
        || spawn("glow_squid", world, 10)
        || spawn("axolotl", world, 10))
      throw new AssertionError("Open ocean must not use the cave spawn profile");
    if (!spawn("pufferfish", world, 50) || !spawn("tropical_fish", world, 50))
      throw new AssertionError("Ocean surface fish rejected");
    world.roofY = 55;
    if (spawn("cod", world, 50)) throw new AssertionError("Surface fish spawned in a cave");
    world.roofY = 15;
    world.habitatLight = 1;
    if (spawn("glow_squid", world, 10))
      throw new AssertionError("Glow squid spawned in a lit cave");
    world.habitatLight = 0;
    world.habitatBiome.biomeName = "Lush Swamp";
    if (!spawn("axolotl", world, 10) || spawn("guardian", world, 10))
      throw new AssertionError("Freshwater cave profile mixed with ocean predators");
    world.roofY = -1000;
    world.floorY = 58;
    world.habitatLight = 7;
    if (!spawn("axolotl", world, 60))
      throw new AssertionError("Approved shaded shallow pool rejected");
    world.habitatLight = 8;
    if (spawn("axolotl", world, 60)) throw new AssertionError("Unshaded pool accepted");
    world.floorY = -60;
    world.roofY = -5;
    world.habitatLight = 0;
    world.habitatBiome.biomeName = "Coral Reef";
    if (spawn("guardian", world, -20))
      throw new AssertionError("Negative spawning enabled without CaveAbyss");
    Field loaded = FaunaCaveAbyss.class.getDeclaredField("loaded");
    loaded.setAccessible(true);
    Field api = FaunaCaveAbyss.class.getDeclaredField("oceanBiome");
    api.setAccessible(true);
    try {
      api.set(
          null,
          Class.forName("ru.givler.caveabyss.world.OceanCaves")
              .getMethod("isOceanBiome", BiomeGenBase.class));
      System.out.println("Testing MBO profiles against the compiled CaveAbyss ocean API");
    } catch (ClassNotFoundException absent) {
      /* Ordinary MBO-only smoke run. */
    }
    loaded.setBoolean(null, true);
    try {
      world.roofY = 15;
      if (!spawn("guardian", world, 10))
        throw new AssertionError("Connected upper CaveAbyss ocean cave must allow guardians");
      world.floorY = 0;
      if (spawn("guardian", world, 10))
        throw new AssertionError("Isolated vanilla ocean cave must not allow guardians");
      world.floorY = -60;
      world.roofY = -5;
      if (!spawn("guardian", world, -20) || !spawn("glow_squid", world, -20))
        throw new AssertionError("CaveAbyss lower ocean habitat rejected");
    } finally {
      loaded.setBoolean(null, false);
      api.set(null, null);
    }
    world.habitatFixture = false;

    File directory = new File("build/fauna-habitat-migration-" + UUID.randomUUID());
    Configuration old =
        new Configuration(new File(directory, "MoreBeyondOrdinary/mob_spawning.cfg"));
    old.load();
    old.get("general", "habitatVersion", 0).set(0);
    old.get("guardian", "biomeWhitelist", new String[0]).set(new String[0]);
    old.get("guardian", "biomeTypes", new String[] {"OCEAN"}).set(new String[] {"OCEAN"});
    old.get("guardian", "minY", 0).set(0);
    old.get("guardian", "maxY", 63).set(63);
    old.get("guardian", "weight", 8).set(8);
    old.get("fox", "biomeWhitelist", new String[] {"Custom Forest"})
        .set(new String[] {"Custom Forest"});
    old.save();
    MobSpawnConfig.load(directory);
    old.load();
    if (old.getCategory("guardian").get("minY").getInt() != -50
        || old.getCategory("guardian").get("weight").getInt() != 20
        || !Arrays.asList(MobBiomeConfig.caves("guardian")).contains("Coral Reef"))
      throw new AssertionError("Legacy broad defaults did not migrate");
    if (!Arrays.equals(
        old.getCategory("fox").get("biomeWhitelist").getStringList(),
        new String[] {"Custom Forest"}))
      throw new AssertionError("Migration replaced an explicit custom whitelist");
    checkUnifiedConfigMigration();
    MobSpawnConfig.load(new File("build/fauna-smoke-config"));
    System.out.println(
        "Approved biome profiles, cave/surface separation, negative heights and config migration passed");
  }

  private static void checkUnifiedConfigMigration() throws Exception {
    File directory = new File("build/mob-config-migration-" + UUID.randomUUID());
    File oldFaunaFile = new File(directory, "MoreBeyondOrdinary/fauna.cfg");
    File oldUndeadFile = new File(directory, "MoreBeyondOrdinary/undead.cfg");
    Configuration fauna = new Configuration(oldFaunaFile);
    fauna.load();
    fauna.get("fox", "weight", 37).set(37);
    fauna.get("spawning", "fishCap", 31).set(31);
    fauna.save();
    Configuration undead = new Configuration(oldUndeadFile);
    undead.load();
    undead.get("husk", "spawnShare", 23).set(23);
    undead.get("husk", "enabled", false).set(false);
    undead.get("husk", "biomeWhitelist", new String[] {"Custom Desert"});
    undead.save();
    byte[] originalFauna = Files.readAllBytes(oldFaunaFile.toPath());
    byte[] originalUndead = Files.readAllBytes(oldUndeadFile.toPath());
    File unifiedFile = new File(directory, "MoreBeyondOrdinary/mob_spawning.cfg");
    Configuration unified = new Configuration(unifiedFile);
    unified.load();
    unified.get("fox", "weight", 41).set(41);
    unified.save();
    MobSpawnConfig.load(directory);
    unified.load();
    if (unified.getCategory("fox").get("weight").getInt() != 41
        || unified.getCategory("spawning").get("fishCap").getInt() != 31
        || unified.getCategory("husk").get("spawnShare").getInt() != 23
        || unified.getCategory("husk").get("enabled").getBoolean())
      throw new AssertionError("Unified migration lost settings or overwrote an existing value");
    if (!Arrays.equals(MobBiomeConfig.surface("husk"), new String[] {"Custom Desert"}))
      throw new AssertionError("Legacy undead habitat did not migrate with the unified config");
    if (!Arrays.equals(originalFauna, Files.readAllBytes(oldFaunaFile.toPath()))
        || !Arrays.equals(originalUndead, Files.readAllBytes(oldUndeadFile.toPath())))
      throw new AssertionError("Migration modified legacy backup files");
    fauna.getCategory("fox").get("weight").set(99);
    fauna.save();
    MobSpawnConfig.load(directory);
    unified.load();
    if (unified.getCategory("fox").get("weight").getInt() != 41)
      throw new AssertionError("Legacy settings were imported more than once");
  }

  private static boolean spawn(String species, FaunaSmoke.AquaticWorld world, int y) {
    return MobSpawnConfig.allowsPosition(species, world, 0, y, 0);
  }
}
