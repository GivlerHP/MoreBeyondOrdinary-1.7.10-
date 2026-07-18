package ru.givler.mbo.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

public class RenderBarrier implements ISimpleBlockRenderingHandler {

    private final int renderId;

    public RenderBarrier(int renderId) {
        this.renderId = renderId;
    }

    private boolean isHoldingBarrier(Block block) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) {
            return false;
        }
        ItemStack held = player.getHeldItem();
        return held != null && held.getItem() == Item.getItemFromBlock(block);
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        renderer.setOverrideBlockTexture(block.getIcon(0, metadata));
        renderer.renderBlockAsItem(Blocks.stone, 0, 1.0F);
        renderer.clearOverrideBlockTexture();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                    Block block, int modelId, RenderBlocks renderer) {
        return this.isHoldingBarrier(block) && renderer.renderStandardBlock(block, x, y, z);
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
