package ru.givler.mbo.core;

import net.minecraft.world.IBlockAccess;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import ru.givler.mbo.waterlogging.ClientWaterloggedBlocks;
import ru.givler.mbo.waterlogging.WaterloggedWorldData;

public final class WaterloggingRenderHooks {
  private WaterloggingRenderHooks() {}

  public static boolean isWaterlogged(IBlockAccess access, int x, int y, int z) {
    if (access instanceof World) {
      World world = (World) access;
      return world.isRemote
          ? ClientWaterloggedBlocks.contains(world.provider.dimensionId, x, y, z)
          : WaterloggedWorldData.get(world).contains(x, y, z);
    }
    return access instanceof ChunkCache && ClientWaterloggedBlocks.containsCurrent(x, y, z);
  }

  public static boolean shouldHideFace(
      BlockLiquid liquid, IBlockAccess access, int x, int y, int z, int side) {
    if (liquid.getMaterial() != Material.water) return false;
    if (isWaterlogged(access, x, y, z)) return true;
    return false;
  }

  public static boolean shouldHideTopUnderside(
      Block liquid, IBlockAccess access, int x, int y, int z) {
    return liquid.getMaterial() == Material.water
        && access.getBlock(x, y + 1, z).isOpaqueCube();
  }
}
