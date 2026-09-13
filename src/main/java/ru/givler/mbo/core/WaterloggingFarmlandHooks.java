package ru.givler.mbo.core;

import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import ru.givler.mbo.waterlogging.WaterloggedWorldData;

public final class WaterloggingFarmlandHooks {
  private WaterloggingFarmlandHooks() {}

  public static boolean hasNearbyWater(World world, int x, int y, int z) {
    WaterloggedWorldData waterlogged = WaterloggedWorldData.get(world);
    for (int waterX = x - 4; waterX <= x + 4; waterX++) {
      for (int waterY = y; waterY <= y + 1; waterY++) {
        for (int waterZ = z - 4; waterZ <= z + 4; waterZ++) {
          if (world.getBlock(waterX, waterY, waterZ).getMaterial() == Material.water
              || waterlogged.contains(waterX, waterY, waterZ)) return true;
        }
      }
    }
    return false;
  }
}
