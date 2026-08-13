package ru.givler.mbo.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

public class RenderBarrel implements ISimpleBlockRenderingHandler {
    private final int renderId;
    public RenderBarrel(int renderId) { this.renderId = renderId; }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        Tessellator t = Tessellator.instance;
        GL11.glRotatef(90F, 0F, 1F, 0F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        t.startDrawingQuads(); t.setNormal(0, -1, 0); renderer.renderFaceYNeg(block, 0, 0, 0, block.getIcon(0, 1)); t.draw();
        t.startDrawingQuads(); t.setNormal(0, 1, 0); renderer.renderFaceYPos(block, 0, 0, 0, block.getIcon(1, 1)); t.draw();
        t.startDrawingQuads(); t.setNormal(0, 0, -1); renderer.renderFaceZNeg(block, 0, 0, 0, block.getIcon(2, 1)); t.draw();
        t.startDrawingQuads(); t.setNormal(0, 0, 1); renderer.renderFaceZPos(block, 0, 0, 0, block.getIcon(3, 1)); t.draw();
        t.startDrawingQuads(); t.setNormal(-1, 0, 0); renderer.renderFaceXNeg(block, 0, 0, 0, block.getIcon(4, 1)); t.draw();
        t.startDrawingQuads(); t.setNormal(1, 0, 0); renderer.renderFaceXPos(block, 0, 0, 0, block.getIcon(5, 1)); t.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks r) {
        int facing = BlockPistonBase.getPistonOrientation(world.getBlockMetadata(x, y, z));
        switch (facing) {
            case 0: r.uvRotateEast=3; r.uvRotateWest=3; r.uvRotateSouth=3; r.uvRotateNorth=3; break;
            case 2: r.uvRotateSouth=1; r.uvRotateNorth=2; r.uvRotateEast=3; break;
            case 3: r.uvRotateSouth=2; r.uvRotateNorth=1; r.uvRotateWest=3; r.uvRotateTop=3; r.uvRotateBottom=3; break;
            case 4: r.uvRotateEast=1; r.uvRotateWest=2; r.uvRotateNorth=3; r.uvRotateTop=2; r.uvRotateBottom=1; break;
            case 5: r.uvRotateEast=2; r.uvRotateWest=1; r.uvRotateSouth=3; r.uvRotateTop=1; r.uvRotateBottom=2; break;
        }
        boolean result = r.renderStandardBlock(block, x, y, z);
        r.uvRotateEast=r.uvRotateWest=r.uvRotateSouth=r.uvRotateNorth=r.uvRotateTop=r.uvRotateBottom=0;
        return result;
    }
    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return renderId; }
}
