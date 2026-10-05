package ru.givler.mbo.client.render;

import net.minecraft.block.material.Material;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import ru.givler.mbo.waterlogging.ClientWaterloggedBlocks;
import ru.givler.mbo.waterlogging.WaterloggedBlockSupport;
import ru.givler.mbo.waterlogging.WaterloggedFlow;

public final class WaterloggedLiquidHeightHooks {
  private WaterloggedLiquidHeightHooks() {}

  public static float sourceHeight() {
    // RenderBlocks subtracts its surface offset after converting this float to double.
    return 1.0F - BlockLiquid.getLiquidHeightPercent(0);
  }

  public static double sourceSurface() {
    return (double) sourceHeight() - (double) 0.001F;
  }

  public static float cornerHeight(IBlockAccess access, int x, int y, int z) {
    for (int dx = 0; dx >= -1; --dx) for (int dz = 0; dz >= -1; --dz) {
      if (access.getBlock(x+dx,y+1,z+dz).getMaterial() == Material.water) return 1F;
    }
    for (int dx = 0; dx >= -1; --dx) for (int dz = 0; dz >= -1; --dz) {
      if (ru.givler.mbo.core.WaterloggingRenderHooks.isWaterlogged(access,x+dx,y+1,z+dz)
          && access instanceof World
          && WaterloggedFlow.hasOpenBottom((World)access,x+dx,y+1,z+dz)) return 1F;
    }
    return sourceHeight();
  }

  public static float getHeightOverride(
      RenderBlocks renderer, int x, int y, int z, Material material) {
    if (material != Material.water) return -1.0F;
    World world = Minecraft.getMinecraft().theWorld;
    if (world == null) return -1.0F;
    // Falling ordinary water has priority over the adjacent virtual source.
    // Vanilla also raises a corner to full height when any of its four
    // columns contains water above it.
    for (int dx = 0; dx >= -1; --dx) for (int dz = 0; dz >= -1; --dz)
      if (renderer.blockAccess.getBlock(x+dx,y+1,z+dz).getMaterial() == Material.water) return 1F;
    for (int offsetX = 0; offsetX >= -1; offsetX--)
      for (int offsetZ = 0; offsetZ >= -1; offsetZ--) {
        int blockX = x + offsetX;
        int blockZ = z + offsetZ;
        if (ClientWaterloggedBlocks.containsCurrent(blockX, y + 1, blockZ)
            && WaterloggedBlockSupport.canWaterlog(world, blockX, y + 1, blockZ)
            && WaterloggedFlow.hasOpenBottom(world, blockX, y + 1, blockZ)) return 1.0F;
      }
    for (int offsetX = 0; offsetX >= -1; offsetX--)
      for (int offsetZ = 0; offsetZ >= -1; offsetZ--) {
        int blockX = x + offsetX;
        int blockZ = z + offsetZ;
        if (ClientWaterloggedBlocks.containsCurrent(blockX, y, blockZ)
            && WaterloggedBlockSupport.canWaterlog(world, blockX, y, blockZ)
            && WaterloggedFlow.hasOpenTop(world, blockX, y, blockZ))
          return sourceHeight();
      }
    return -1.0F;
  }
}
