package ru.givler.mbo.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Keeps the observer's arrow and side texture upright for all six facings. */
public final class RenderObserver implements ISimpleBlockRenderingHandler {
    private final int renderId;

    public RenderObserver(int id) { renderId = id; }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                    Block block, int modelId, RenderBlocks renderer) {
        rotate(world.getBlockMetadata(x, y, z) & 7, renderer);
        try { return renderer.renderStandardBlock(block, x, y, z); }
        finally { reset(renderer); }
    }

    private static void rotate(int facing, RenderBlocks renderer) {
        switch (facing) {
            case 2:
                renderer.uvRotateTop = 3;
                renderer.uvRotateBottom = 3;
                break;
            case 5:
                renderer.uvRotateTop = 2;
                renderer.uvRotateBottom = 1;
                break;
            case 4:
                renderer.uvRotateTop = 1;
                renderer.uvRotateBottom = 2;
                break;
            case 1:
                renderer.uvRotateNorth = 1;
                renderer.uvRotateSouth = 2;
                renderer.uvRotateWest = 3;
                renderer.uvRotateEast = 3;
                break;
            case 0:
                renderer.uvRotateNorth = 1;
                renderer.uvRotateSouth = 2;
                break;
        }
    }

    private static void reset(RenderBlocks renderer) {
        renderer.flipTexture = false;
        renderer.uvRotateNorth = 0;
        renderer.uvRotateSouth = 0;
        renderer.uvRotateWest = 0;
        renderer.uvRotateEast = 0;
        renderer.uvRotateTop = 0;
        renderer.uvRotateBottom = 0;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        Tessellator t = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslatef(-.5F, -.5F, -.5F);
        for (int side = 0; side < 6; side++) {
            t.startDrawingQuads();
            switch (side) {
                case 0: t.setNormal(0, -1, 0); renderer.renderFaceYNeg(block, 0, 0, 0, block.getIcon(side, 5)); break;
                case 1: t.setNormal(0, 1, 0); renderer.renderFaceYPos(block, 0, 0, 0, block.getIcon(side, 5)); break;
                case 2: t.setNormal(0, 0, -1); renderer.renderFaceZNeg(block, 0, 0, 0, block.getIcon(side, 5)); break;
                case 3: t.setNormal(0, 0, 1); renderer.renderFaceZPos(block, 0, 0, 0, block.getIcon(side, 5)); break;
                case 4: t.setNormal(-1, 0, 0); renderer.renderFaceXNeg(block, 0, 0, 0, block.getIcon(side, 5)); break;
                default: t.setNormal(1, 0, 0); renderer.renderFaceXPos(block, 0, 0, 0, block.getIcon(side, 5));
            }
            t.draw();
        }
        GL11.glPopMatrix();
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return renderId; }
}
