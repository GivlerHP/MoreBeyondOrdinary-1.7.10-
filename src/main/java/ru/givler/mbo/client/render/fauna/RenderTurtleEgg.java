package ru.givler.mbo.client.render.fauna;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import ru.givler.mbo.block.fauna.BlockTurtleEgg;

public final class RenderTurtleEgg implements ISimpleBlockRenderingHandler {
  private final int id;

  public RenderTurtleEgg(int id) {
    this.id = id;
  }

  public int getRenderId() {
    return id;
  }

  public boolean shouldRender3DInInventory(int modelId) {
    return false;
  }

  public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {}

  public boolean renderWorldBlock(
      IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
    int count = BlockTurtleEgg.count(world.getBlockMetadata(x, y, z));
    double[][] bounds = {
      {.1875, 0, .1875, .5625, .4375, .5625},
      {.5625, 0, .5, .9375, .3125, .875},
      {.125, 0, .5625, .4375, .375, .875},
      {.5625, 0, .125, .875, .375, .4375}
    };
    try {
      for (int i = 0; i < count; i++) {
        double[] b = bounds[i];
        renderer.setRenderBounds(b[0], b[1], b[2], b[3], b[4], b[5]);
        renderer.renderStandardBlock(block, x, y, z);
      }
    } finally {
      renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
    }
    return true;
  }
}
