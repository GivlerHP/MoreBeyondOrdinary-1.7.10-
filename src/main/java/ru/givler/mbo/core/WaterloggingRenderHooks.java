package ru.givler.mbo.core;

import net.minecraft.world.IBlockAccess;
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

}
