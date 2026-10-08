package ru.givler.mbo.entity.fauna;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import ru.givler.mbo.integration.biomesoplenty.FaunaBiomesOPlenty;
import ru.givler.mbo.integration.caveabyss.FaunaCaveAbyss;

/** Local habitat checks in addition to biome/dimension/light configuration. No chunk loading. */
public final class FaunaHabitats {
  private FaunaHabitats() {}

  public static boolean allows(
      String id, World world, int x, int y, int z, boolean surfaceBiome, boolean caveBiome) {
    if (y < 0 && !FaunaCaveAbyss.hasLowerLayer()) return false;
    if (id.equals("guardian") && !FaunaCaveAbyss.isOceanCave(world, x, y, z)) return false;
    if (id.equals("guardian") || id.equals("glow_squid") || id.equals("axolotl")) {
      if (!water(world, x, y, z) || !water(world, x, y + 1, z)) return false;
      if (!clearWaterVolume(world, x, y, z, id.equals("glow_squid") ? 1 : 0)) return false;
      boolean covered = caveRoof(world, x, y, z);
      if (covered) {
        boolean ocean = FaunaCaveAbyss.isOcean(world.getBiomeGenForCoords(x, z));
        int maximum = ocean ? 14 : 30;
        return caveBiome
            && y >= -50
            && y <= maximum
            && (id.equals("axolotl") || ocean)
            && world.getFullBlockLightValue(x, y, z) <= (id.equals("glow_squid") ? 0 : 7)
            && safeFloor(world, x, y, z);
      }
      return id.equals("axolotl")
          && surfaceBiome
          && y >= 0
          && y <= 63
          && world.getBlockMetadata(x, y, z) == 0
          && world.getFullBlockLightValue(x, y, z) <= 7
          && shallowPool(world, x, y, z);
    }
    if (!surfaceBiome || y < 0) return false;
    if (id.equals("cod")
        || id.equals("salmon")
        || id.equals("tropical_fish")
        || id.equals("pufferfish"))
      return y >= 15 && clearWaterVolume(world, x, y, z, 1) && !caveRoof(world, x, y, z);
    if (id.equals("frog")) return !water(world, x, y, z) && nearbyWater(world, x, y, z, 4);
    if (id.equals("turtle")) return nearbyWater(world, x, y, z, 6);
    if (id.equals("panda")) return nearbyBamboo(world, x, y, z);
    if (id.equals("goat")) {
      if (!world.canBlockSeeTheSky(x, y + 1, z)) return false;
      int footing = 0;
      for (int dx = -1; dx <= 1; dx++)
        for (int dz = -1; dz <= 1; dz++)
          if (world.blockExists(x + dx, y - 1, z + dz)
              && world.getBlock(x + dx, y - 1, z + dz).getMaterial().isSolid()) footing++;
      return footing >= 5;
    }
    if (id.equals("camel")) return world.canBlockSeeTheSky(x, y + 2, z);
    return true;
  }

  public static boolean caveRoof(World world, int x, int y, int z) {
    // Ice, tree crowns and aquatic plants must not turn an open ocean into a cave.
    for (int dy = 1; dy <= 32 && y + dy < 256; dy++) {
      if (!world.blockExists(x, y + dy, z)) return false;
      Material material = world.getBlock(x, y + dy, z).getMaterial();
      if (material == Material.rock
          || material == Material.ground
          || material == Material.grass
          || material == Material.clay) return true;
    }
    return false;
  }

  private static boolean water(World world, int x, int y, int z) {
    return world.blockExists(x, y, z) && world.getBlock(x, y, z).getMaterial() == Material.water;
  }

  private static boolean clearWaterVolume(World world, int x, int y, int z, int radius) {
    for (int dx = -radius; dx <= radius; dx++)
      for (int dz = -radius; dz <= radius; dz++)
        if (!water(world, x + dx, y, z + dz) || !water(world, x + dx, y + 1, z + dz)) return false;
    return true;
  }

  private static boolean safeFloor(World world, int x, int y, int z) {
    // Follow the same uninterrupted water column as CaveAbyss, even far above its magma floor.
    int minimum = FaunaCaveAbyss.hasLowerLayer() ? -64 : 0;
    for (int current = y; current >= minimum; current--) {
      if (!world.blockExists(x, current, z)) return false;
      Block block = world.getBlock(x, current, z);
      if (block.getClass().getName().equals("ru.givler.caveabyss.block.BlockMagma")) return false;
      if (block.getMaterial() != Material.water) return true;
    }
    return true;
  }

  private static boolean shallowPool(World world, int x, int y, int z) {
    boolean ground = false, air = false;
    for (int dy = 1; dy <= 4; dy++) {
      if (!world.blockExists(x, y - dy, z) || !world.blockExists(x, y + dy, z)) return false;
      Material floor = world.getBlock(x, y - dy, z).getMaterial();
      ground |=
          floor == Material.ground
              || floor == Material.grass
              || floor == Material.clay
              || floor == Material.sand;
      air |= world.isAirBlock(x, y + dy, z);
    }
    return ground
        && air
        && water(world, x - 1, y, z)
        && water(world, x + 1, y, z)
        && water(world, x, y, z - 1)
        && water(world, x, y, z + 1);
  }

  private static boolean nearbyWater(World world, int x, int y, int z, int radius) {
    for (int dy = -2; dy <= 0; dy++)
      for (int dx = -radius; dx <= radius; dx++)
        for (int dz = -radius; dz <= radius; dz++)
          if (water(world, x + dx, y + dy, z + dz)) return true;
    return false;
  }

  private static boolean nearbyBamboo(World world, int x, int y, int z) {
    for (int dy = 0; dy <= 2; dy++)
      for (int dx = -6; dx <= 6; dx++)
        for (int dz = -6; dz <= 6; dz++)
          if (world.blockExists(x + dx, y + dy, z + dz)
              && FaunaBiomesOPlenty.isBamboo(world.getBlock(x + dx, y + dy, z + dz))) return true;
    return false;
  }
}
