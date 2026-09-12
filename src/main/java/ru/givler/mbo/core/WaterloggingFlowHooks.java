package ru.givler.mbo.core;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockLever;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import ru.givler.mbo.waterlogging.WaterloggedBlockSupport;
import ru.givler.mbo.waterlogging.WaterloggedFlow;
import ru.givler.mbo.waterlogging.WaterloggedWorldData;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.block.model.BlockModelCollision;

public final class WaterloggingFlowHooks {
  private static final ThreadLocal<int[]> CURRENT_TICK = new ThreadLocal<int[]>();

  private WaterloggingFlowHooks() {}

  public static boolean protectsFromWaterFlow(
      Block liquid, World world, int x, int y, int z) {
    if (liquid.getMaterial() != Material.water) return false;
    Block target = world.getBlock(x, y, z);
    return target instanceof BlockModels
        || target instanceof BlockModelCollision
        || target instanceof BlockLever
        || target instanceof BlockButton
        || target instanceof BlockBasePressurePlate;
  }

  public static void onLiquidTick(Block liquid, World world, int x, int y, int z) {
    if (world.isRemote || liquid.getMaterial() != Material.water) return;
    CURRENT_TICK.set(new int[] {x, y, z});
    if (world.getBlockMetadata(x, y, z) != 0) return;
    WaterloggedWorldData data = WaterloggedWorldData.get(world);
    int[][] directions = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {0, -1, 0}};
    for (int[] direction : directions) {
      int targetX = x + direction[0];
      int targetY = y + direction[1];
      int targetZ = z + direction[2];
      if (WaterloggedFlow.canFillFromSource(world, targetX, targetY, targetZ, direction))
        data.set(world, targetX, targetY, targetZ, true);
    }
  }

  public static void afterLiquidTick(Block liquid, World world, int x, int y, int z) {
    if (world.isRemote || liquid.getMaterial() != Material.water) return;
    CURRENT_TICK.remove();
    WaterloggedWorldData data = WaterloggedWorldData.get(world);
    boolean fallingOutlet =
        y < 255
            && data.contains(x, y + 1, z)
            && WaterloggedBlockSupport.canWaterlog(world, x, y + 1, z)
            && WaterloggedFlow.hasOpenBottom(world, x, y + 1, z);
    if (!fallingOutlet) return;
    int level = 8;
    if (world.getBlock(x, y, z).getMaterial() != Material.water
        || world.getBlockMetadata(x, y, z) != level)
      world.setBlock(x, y, z, Blocks.flowing_water, level, 3);
    world.scheduleBlockUpdate(x, y, z, Blocks.flowing_water, Blocks.flowing_water.tickRate(world));
  }

  public static boolean isWaterloggedFlowSource(World world, int x, int y, int z) {
    int[] current = CURRENT_TICK.get();
    if (current == null || current[1] != y || Math.abs(current[0] - x) + Math.abs(current[2] - z) != 1)
      return false;
    WaterloggedWorldData data = WaterloggedWorldData.get(world);
    if (!data.contains(x, y, z) || !WaterloggedBlockSupport.canWaterlog(world, x, y, z))
      return false;
    return WaterloggedFlow.hasOpenSide(world, x, y, z, current[0] - x, current[2] - z);
  }

}
