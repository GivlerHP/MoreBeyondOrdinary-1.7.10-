package ru.givler.mbo.client.render;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.World;
import ru.givler.mbo.core.WaterloggingRenderHooks;

/** Submit pane water to the same sorted translucent buffer as glass and ordinary water. */
public final class WaterloggedPaneChunkHooks {
  private WaterloggedPaneChunkHooks() { }

  public static boolean hasWater(RenderBlocks renderer, Block block, int x, int y, int z) {
    return block instanceof BlockPane && WaterloggingRenderHooks.isWaterlogged(renderer.blockAccess, x, y, z);
  }

  public static int renderPass(Block block, RenderBlocks renderer, int x, int y, int z) {
    return hasWater(renderer, block, x, y, z) ? 1 : block.getRenderBlockPass();
  }

  public static boolean canRender(Block block, int pass, RenderBlocks renderer, int x, int y, int z) {
    return block.canRenderInPass(pass) || pass == 1 && hasWater(renderer, block, x, y, z);
  }

  public static boolean render(RenderBlocks renderer, Block block, int x, int y, int z, int pass) {
    boolean drawn = block.canRenderInPass(pass) && renderer.renderBlockByRenderType(block, x, y, z);
    if (pass == 1 && hasWater(renderer, block, x, y, z)) {
      World world = renderer.blockAccess instanceof World
          ? (World) renderer.blockAccess : Minecraft.getMinecraft().theWorld;
      if (world != null) {
        WaterloggedBlockRenderer.renderPaneWaterInChunk(world, x, y, z);
        drawn = true;
      }
    }
    return drawn;
  }
}
