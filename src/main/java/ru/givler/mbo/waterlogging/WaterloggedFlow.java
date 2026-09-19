package ru.givler.mbo.waterlogging;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public final class WaterloggedFlow {
  private WaterloggedFlow() {}

  public static void flow(World world, WaterloggedWorldData data, int x, int y, int z) {
    if (!flowDown(world, data, x, y, z)) flowSideways(world, data, x, y, z);
  }

  public static boolean canRetainWater(World world, int x, int y, int z) {
    Block block = world.getBlock(x, y, z);
    if (block instanceof BlockDoor) return true;
    if (!(block instanceof BlockTrapDoor)) return true;
    if ((world.getBlockMetadata(x, y, z) & 4) != 0) return true;
    if (world.getBlock(x + 1, y, z).getMaterial() == Material.water
        || world.getBlock(x - 1, y, z).getMaterial() == Material.water
        || world.getBlock(x, y, z + 1).getMaterial() == Material.water
        || world.getBlock(x, y, z - 1).getMaterial() == Material.water) return true;
    return world.getBlock(x, y + 1, z).getMaterial() == Material.water
        && world.getBlock(x, y - 1, z).getMaterial() == Material.water;
  }

  public static boolean canRetainWater(
      World world, WaterloggedWorldData data, int x, int y, int z) {
    Block block = world.getBlock(x, y, z);
    if (block instanceof BlockDoor) return canDoorRetainWater(world, data, x, y, z);
    if (!(block instanceof BlockTrapDoor)) return true;

    boolean waterAbove = world.getBlock(x, y + 1, z).getMaterial() == Material.water;
    boolean waterBelow = world.getBlock(x, y - 1, z).getMaterial() == Material.water;
    if (waterAbove && waterBelow) return true;

    byte ingress = data.ingressMask(x, y, z);
    if ((ingress & WaterloggedWorldData.INTERNAL_SOURCE) != 0) return true;
    int metadata = world.getBlockMetadata(x, y, z);
    if ((metadata & 4) != 0) {
      if ((ingress & WaterloggedGeometry.faceBit(0, 1, 0)) != 0 && waterAbove) return true;
      if ((ingress & WaterloggedGeometry.faceBit(0, -1, 0)) != 0 && waterBelow) return true;
      int[][] lateralSides = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
      for (int[] side : lateralSides) {
        if (!isLateralToVerticalTrapdoor(metadata, side[0], side[1])
            || (ingress & WaterloggedGeometry.faceBit(side[0], 0, side[1])) == 0) continue;
        int waterX = x + side[0], waterZ = z + side[1];
        if (world.getBlock(waterX, y, waterZ).getMaterial() == Material.water
            && world.getBlockMetadata(waterX, y, waterZ) == 0) return true;
      }
      return false;
    }

    if ((ingress & WaterloggedWorldData.OPEN_TRAPDOOR_INGRESS) != 0) return false;
    if ((ingress & WaterloggedGeometry.faceBit(0, 1, 0)) != 0) return false;
    int[][] sides = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    for (int[] side : sides) {
      if ((ingress & WaterloggedGeometry.faceBit(side[0], 0, side[1])) == 0) continue;
      int waterX = x + side[0], waterZ = z + side[1];
      if (world.getBlock(waterX, y, waterZ).getMaterial() == Material.water
          && world.getBlockMetadata(waterX, y, waterZ) == 0) return true;
    }
    return false;
  }

  private static boolean canDoorRetainWater(
      World world, WaterloggedWorldData data, int x, int y, int z) {
    boolean waterAbove = world.getBlock(x, y + 1, z).getMaterial() == Material.water;
    boolean waterBelow = world.getBlock(x, y - 1, z).getMaterial() == Material.water;
    if (waterAbove && waterBelow) return true;

    byte ingress = data.ingressMask(x, y, z);
    if ((ingress & WaterloggedWorldData.INTERNAL_SOURCE) != 0) return true;
    int metadata = world.getBlockMetadata(x, y, z);
    int baseY = (metadata & 8) == 0 ? y : y - 1;
    boolean open = (world.getBlockMetadata(x, baseY, z) & 4) != 0;
    if (!open && (ingress & WaterloggedWorldData.OPEN_TRAPDOOR_INGRESS) != 0) return false;
    if ((ingress & WaterloggedGeometry.faceBit(0, 1, 0)) != 0 && waterAbove) return true;
    if ((ingress & WaterloggedGeometry.faceBit(0, -1, 0)) != 0 && waterBelow) return true;

    int[][] sides = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    for (int[] side : sides) {
      if (!WaterloggedGeometry.isLateralToDoor(world, x, y, z, side[0], side[1])
          || (ingress & WaterloggedGeometry.faceBit(side[0], 0, side[1])) == 0) continue;
      int waterX = x + side[0], waterZ = z + side[1];
      if (world.getBlock(waterX, y, waterZ).getMaterial() == Material.water
          && world.getBlockMetadata(waterX, y, waterZ) == 0) return true;
    }
    return false;
  }

  public static boolean flowDown(World world, WaterloggedWorldData data, int x, int y, int z) {
    if (world.isRemote || y <= 0 || !canFlowTo(world, data, x, y, z, 0, -1, 0)) return false;

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
    if (!WaterloggedGeometry.isFaceOpen(world, x, y, z, 0, -1, 0)) return false;
    boolean[] free = WaterloggedGeometry.waterCellsForRender(world, x, y, z);
    for (int cellX = 0; cellX < 2; cellX++)
      for (int cellZ = 0; cellZ < 2; cellZ++)
        if (free[WaterloggedGeometry.index(cellX, 0, cellZ)]) return true;
    return false;
  }

  public static boolean hasOpenTop(World world, int x, int y, int z) {
    if (!WaterloggedGeometry.isFaceOpen(world, x, y, z, 0, 1, 0)) return false;
    boolean[] free = WaterloggedGeometry.freeCells(world, x, y, z);
    for (int cellX = 0; cellX < 2; cellX++)
      for (int cellZ = 0; cellZ < 2; cellZ++)
        if (free[WaterloggedGeometry.index(cellX, 1, cellZ)]) return true;
    return false;
  }

  public static boolean canFillFromSource(
      World world, int targetX, int targetY, int targetZ, int[] directionFromSource) {
    if (!WaterloggedBlockSupport.canWaterlog(world, targetX, targetY, targetZ)) return false;
    if (!canRetainWater(world, targetX, targetY, targetZ)) return false;
    if (directionFromSource[1] < 0) return hasOpenTop(world, targetX, targetY, targetZ);
    Block target = world.getBlock(targetX, targetY, targetZ);
    if (target instanceof BlockDoor
        && !WaterloggedGeometry.isLateralToDoor(
            world,
            targetX,
            targetY,
            targetZ,
            -directionFromSource[0],
            -directionFromSource[2])) return false;
    if (target instanceof BlockTrapDoor) {
      int metadata = world.getBlockMetadata(targetX, targetY, targetZ);
      if ((metadata & 4) != 0
          && !isLateralToVerticalTrapdoor(
              metadata, -directionFromSource[0], -directionFromSource[2])) return false;
    }
    return hasOpenSide(
        world,
        targetX,
        targetY,
        targetZ,
        -directionFromSource[0],
        -directionFromSource[2]);
  }

  private static boolean isLateralToVerticalTrapdoor(
      int metadata, int directionX, int directionZ) {
    int facing = metadata & 3;
    return facing < 2 ? directionX != 0 : directionZ != 0;
  }

  public static boolean hasOpenSide(
      World world, int x, int y, int z, int directionX, int directionZ) {
    if (!WaterloggedGeometry.isFaceOpen(
        world, x, y, z, directionX, 0, directionZ)) return false;
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
      if (!canFlowTo(world, data, x, y, z, direction[0], 0, direction[1])) continue;
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

  public static boolean canFlowTo(
      World world, WaterloggedWorldData data, int x, int y, int z,
      int directionX, int directionY, int directionZ) {
    return WaterloggedGeometry.canReachFace(
        world, x, y, z, data.ingressMask(x, y, z), directionX, directionY, directionZ);
  }

  public static boolean canSupplyWaterTo(
      World world, WaterloggedWorldData data, int x, int y, int z,
      int directionX, int directionY, int directionZ) {
    byte mask = data.ingressMask(x, y, z);
    if ((mask & WaterloggedWorldData.INTERNAL_SOURCE) == 0)
      mask &= ~WaterloggedGeometry.faceBit(directionX, directionY, directionZ);
    return WaterloggedGeometry.canReachFace(
        world, x, y, z, mask, directionX, directionY, directionZ);
  }

}
