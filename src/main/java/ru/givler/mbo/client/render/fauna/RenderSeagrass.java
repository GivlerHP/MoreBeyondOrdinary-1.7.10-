package ru.givler.mbo.client.render.fauna;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;

public final class RenderSeagrass implements ISimpleBlockRenderingHandler {
  private final int id;

  public RenderSeagrass(int id) {
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
    renderer.renderBlockLiquid(Blocks.water, x, y, z);
    return renderer.renderCrossedSquares(block, x, y, z);
  }
}
