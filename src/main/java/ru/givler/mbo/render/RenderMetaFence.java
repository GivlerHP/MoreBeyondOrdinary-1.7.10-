package ru.givler.mbo.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import ru.givler.mbo.block.BlockBasicFence;

public class RenderMetaFence implements ISimpleBlockRenderingHandler {

    private final int renderId;

    public RenderMetaFence(int renderId) {
        this.renderId = renderId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        renderer.setOverrideBlockTexture(block.getIcon(0, metadata));
        renderer.renderBlockAsItem(Blocks.fence, 0, 1.0F);
        renderer.clearOverrideBlockTexture();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                    Block block, int modelId, RenderBlocks renderer) {
        return renderer.renderBlockFence((BlockBasicFence) block, x, y, z);
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return this.renderId;
    }
}
