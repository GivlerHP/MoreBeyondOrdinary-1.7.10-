package ru.givler.mbo.client.render;

import net.minecraft.block.material.Material;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.World;
import ru.givler.mbo.waterlogging.ClientWaterloggedBlocks;
import ru.givler.mbo.waterlogging.WaterloggedBlockSupport;
import ru.givler.mbo.waterlogging.WaterloggedFlow;

public final class WaterloggedLiquidHeightHooks {
  private WaterloggedLiquidHeightHooks() {}

  public static float getHeightOverride(
      RenderBlocks renderer, int x, int y, int z, Material material) {
    if (material != Material.water) return -1.0F;
    World world = Minecraft.getMinecraft().theWorld;
    if (world == null) return -1.0F;
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
          return 1.0F - BlockLiquid.getLiquidHeightPercent(0) - 0.001F;
      }
    return -1.0F;
  }
}
