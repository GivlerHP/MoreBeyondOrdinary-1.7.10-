package ru.givler.mbo.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import ru.givler.mbo.block.BlockAdminFire;

public final class RenderAdminFire implements ISimpleBlockRenderingHandler {
  private final int renderId;

  public RenderAdminFire(int renderId) { this.renderId = renderId; }

  @Override
  public boolean renderWorldBlock(
      IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
    BlockAdminFire fire = (BlockAdminFire) block;
    fire.beginRender(world, x, y, z);
    return renderer.renderBlockFire(fire, x, y, z);
  }

  @Override public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {}
  @Override public boolean shouldRender3DInInventory(int modelId) { return false; }
  @Override public int getRenderId() { return renderId; }
}
