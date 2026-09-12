package ru.givler.mbo.waterlogging;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public final class WaterloggedFlow {
  private WaterloggedFlow() {}

  public static void flow(World world, WaterloggedWorldData data, int x, int y, int z) {
    if (!flowDown(world, data, x, y, z)) flowSideways(world, data, x, y, z);
  }

  public static boolean flowDown(World world, WaterloggedWorldData data, int x, int y, int z) {
    if (world.isRemote || y <= 0 || !hasOpenBottom(world, x, y, z)) return false;

    int belowY = y - 1;
    if (WaterloggedBlockSupport.canWaterlog(world, x, belowY, z)) {
      return false;
    }

    Block below = world.getBlock(x, belowY, z);
    Material material = below.getMaterial();
    if (material == Material.water) return true;
    if (!world.isAirBlock(x, belowY, z) && !material.isReplaceable()) return false;
    world.setBlock(x, belowY, z, Blocks.flowing_water, 8, 3);
    world.scheduleBlockUpdate(x, belowY, z, Blocks.flowing_water, Blocks.flowing_water.tickRate(world));
    return true;
  }

  public static boolean hasOpenBottom(World world, int x, int y, int z) {
    boolean[] free = WaterloggedGeometry.waterCellsForRender(world, x, y, z);
    for (int cellX = 0; cellX < 2; cellX++)
      for (int cellZ = 0; cellZ < 2; cellZ++)
        if (free[WaterloggedGeometry.index(cellX, 0, cellZ)]) return true;
    return false;
  }

  public static boolean hasOpenTop(World world, int x, int y, int z) {
    boolean[] free = WaterloggedGeometry.freeCells(world, x, y, z);
    for (int cellX = 0; cellX < 2; cellX++)
      for (int cellZ = 0; cellZ < 2; cellZ++)
        if (free[WaterloggedGeometry.index(cellX, 1, cellZ)]) return true;
    return false;
  }

  public static boolean canFillFromSource(
      World world, int targetX, int targetY, int targetZ, int[] directionFromSource) {
    if (!WaterloggedBlockSupport.canWaterlog(world, targetX, targetY, targetZ)) return false;
    if (directionFromSource[1] < 0) return hasOpenTop(world, targetX, targetY, targetZ);
    return hasOpenSide(
        world,
        targetX,
        targetY,
        targetZ,
        -directionFromSource[0],
        -directionFromSource[2]);
  }

  public static boolean hasOpenSide(
      World world, int x, int y, int z, int directionX, int directionZ) {
    boolean[] free = WaterloggedGeometry.waterCellsForRender(world, x, y, z);
    int cellX = directionX < 0 ? 0 : directionX > 0 ? 1 : -1;
    int cellZ = directionZ < 0 ? 0 : directionZ > 0 ? 1 : -1;
    for (int cellY = 0; cellY < 2; cellY++)
      for (int other = 0; other < 2; other++) {
        int testX = cellX < 0 ? other : cellX;
        int testZ = cellZ < 0 ? other : cellZ;
        if (free[WaterloggedGeometry.index(testX, cellY, testZ)]) return true;
      }
    return false;
  }

  private static void flowSideways(
      World world, WaterloggedWorldData data, int x, int y, int z) {
    int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    for (int[] direction : directions) {
      if (!hasOpenSide(world, x, y, z, direction[0], direction[1])) continue;
      int targetX = x + direction[0];
      int targetZ = z + direction[1];
      if (WaterloggedBlockSupport.canWaterlog(world, targetX, y, targetZ)) {
        continue;
      }
      Block target = world.getBlock(targetX, y, targetZ);
      Material material = target.getMaterial();
      if (material == Material.water) continue;
      if (!world.isAirBlock(targetX, y, targetZ) && !material.isReplaceable()) continue;
      world.setBlock(targetX, y, targetZ, Blocks.flowing_water, 1, 3);
      world.scheduleBlockUpdate(
          targetX, y, targetZ, Blocks.flowing_water, Blocks.flowing_water.tickRate(world));
    }
  }

}
