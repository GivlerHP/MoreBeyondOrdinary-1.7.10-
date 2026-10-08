package ru.givler.mbo.fauna;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraftforge.common.config.Configuration;
import ru.givler.mbo.config.FaunaConfig;
import ru.givler.mbo.entity.fauna.*;
import ru.givler.mbo.handler.FaunaAquaticSpawner;
import ru.givler.mbo.handler.FaunaAquaticSpawner.Group;

/** Exercises actual group spawning, quota isolation and existing configuration upgrades. */
public final class FaunaSpawnSmoke {
  public static void check(FaunaSmoke.AquaticWorld world) throws Exception {
    File directory = new File("build/fauna-balance-config-" + UUID.randomUUID());
    FaunaConfig.load(directory);
    Configuration config = new Configuration(new File(directory, "MoreBeyondOrdinary/fauna.cfg"));
    config.load();
    if (config.getCategory("panda").get("weight").getInt() != 10
        || config.getCategory("turtle").get("maxGroup").getInt() != 5
        || config.getCategory("axolotl").get("minGroup").getInt() != 4)
      throw new AssertionError("Modern default spawn values missing");
    config.get("general", "balanceVersion", 1).set(0);
    config.getCategory("cod").get("weight").set(15);
    config.getCategory("cod").get("maxGroup").set(5);
    config.getCategory("fox").get("weight").set(37);
    config.getCategory("fox").get("biomeBlacklist").set(new String[] {"Taiga"});
    config.save();
    FaunaConfig.load(directory);
    config.load();
    if (config.getCategory("cod").get("weight").getInt() != 10
        || config.getCategory("cod").get("maxGroup").getInt() != 6
        || config.getCategory("fox").get("weight").getInt() != 37
        || config.getCategory("fox").get("biomeBlacklist").getStringList().length != 1)
      throw new AssertionError(
          "Balance migration overwrote custom settings or missed generated defaults");

    config.get("general", "balanceVersion", 2).set(1);
    config.getCategory("panda").get("weight").set(80);
    config.save();
    FaunaConfig.load(directory);
    config.load();
    if (config.getCategory("panda").get("weight").getInt() != 10)
      throw new AssertionError("Previous panda default was not reduced");
    config.get("general", "balanceVersion", 2).set(1);
    config.getCategory("panda").get("weight").set(7);
    config.save();
    FaunaConfig.load(directory);
    config.load();
    if (config.getCategory("panda").get("weight").getInt() != 7)
      throw new AssertionError("Custom panda weight was overwritten");

    world.habitatFixture = true;
    world.roofY = -1000;
    world.floorY = -60;
    world.habitatLight = 0;
    world.habitatBiome.biomeName = "Deep Ocean";
    java.nio.file.Path habitats =
        new File(directory, "MoreBeyondOrdinary/biome_habitats.json").toPath();
    String originalHabitats =
        new String(
            java.nio.file.Files.readAllBytes(habitats), java.nio.charset.StandardCharsets.UTF_8);
    java.nio.file.Files.write(
        habitats,
        originalHabitats
            .replace("Deep Ocean", "Configured Ocean")
            .getBytes(java.nio.charset.StandardCharsets.UTF_8));
    FaunaConfig.load(directory);
    world.habitatBiome.biomeName = "Configured Ocean";
    if (!FaunaConfig.allowsPosition("tropical_fish", world, 0, 50, 0))
      throw new AssertionError("External JSON edits do not control actual spawn profiles");
    if (!new String(
            java.nio.file.Files.readAllBytes(habitats), java.nio.charset.StandardCharsets.UTF_8)
        .contains("Configured Ocean"))
      throw new AssertionError("External habitat file was overwritten");
    java.nio.file.Files.write(
        habitats, originalHabitats.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    FaunaConfig.load(directory);
    world.habitatBiome.biomeName = "Deep Ocean";
    world.playerEntities = new ArrayList();
    java.lang.reflect.Field random = net.minecraft.world.World.class.getField("rand");
    random.setAccessible(true);
    random.set(world, new java.util.Random(42));
    for (int sample = 0; sample < 100; sample++) {
      int y = FaunaAquaticSpawner.fishSpawnY(world, 0, 0, 15, 255);
      if (y < 15 || y > 61 || !FaunaConfig.allowsPosition("tropical_fish", world, 0, y, 0))
        throw new AssertionError("Fish search selected air or an invalid water depth: " + y);
    }
    if (FaunaAquaticSpawner.fishSpawnY(world, 0, 0, 70, 255) != 69
        || FaunaAquaticSpawner.fishAttemptBudget(8, 289) != 32
        || FaunaAquaticSpawner.fishAttemptBudget(8, 578) != 64
        || FaunaAquaticSpawner.fishAttemptBudget(128, 10000) != 256)
      throw new AssertionError("Fish search budget or dry-column handling incorrect");
    world.spawnedFauna = new ArrayList<Entity>();
    int spawned = FaunaAquaticSpawner.spawnGroup(world, "tropical_fish", 0, 50, 0);
    if (spawned != 8 || world.spawnedFauna.size() != 8)
      throw new AssertionError("A tropical fish school must contain eight members: " + spawned);
    EntityMBOFish fish = (EntityMBOFish) world.spawnedFauna.get(0);
    EntityMBOAxolotl axolotl = new EntityMBOAxolotl(world);
    EntityMBOGlowSquid glow = new EntityMBOGlowSquid(world);
    EntityMBOGuardian guardian = new EntityMBOGuardian(world);
    if (FaunaAquaticSpawner.category(fish) != Group.FISH
        || FaunaAquaticSpawner.category(axolotl) != Group.AXOLOTLS
        || FaunaAquaticSpawner.category(glow) != Group.GLOW_SQUID
        || FaunaAquaticSpawner.category(guardian) != Group.GUARDIANS
        || FaunaAquaticSpawner.category(new EntityMBOElderGuardian(world)) != null
        || FaunaAquaticSpawner.category(new EntityMBOTadpole(world)) != null)
      throw new AssertionError("Aquatic quotas overlap");
    for (EnumCreatureType type : EnumCreatureType.values())
      if (fish.isCreatureType(type, true)
          || axolotl.isCreatureType(type, true)
          || glow.isCreatureType(type, true))
        throw new AssertionError("Aquatic species still consume the vanilla land/squid quota");
    if (!guardian.isCreatureType(EnumCreatureType.monster, true))
      throw new AssertionError("Guardians must share the hostile quota");
    if (!FaunaAquaticSpawner.countsTowardQuota(fish))
      throw new AssertionError("Natural fish uncounted");
    fish.setFromBucket(true);
    if (FaunaAquaticSpawner.countsTowardQuota(fish))
      throw new AssertionError("Bucket fish consume quota");
    axolotl.setCustomNameTag("test");
    if (FaunaAquaticSpawner.countsTowardQuota(axolotl))
      throw new AssertionError("Named animal consumes quota");
    if (FaunaAquaticSpawner.scaledCap(20, 289) != 20
        || FaunaAquaticSpawner.scaledCap(5, 578) != 10
        || FaunaAquaticSpawner.scaledCap(0, 289) != 0)
      throw new AssertionError("Spawn area quota scaling incorrect");
    world.spawnedFauna = null;
    world.habitatFixture = false;
    FaunaConfig.load(new File("build/fauna-smoke-config"));
    System.out.println("Aquatic groups, separate quotas and spawn balance migration passed");
  }
}
