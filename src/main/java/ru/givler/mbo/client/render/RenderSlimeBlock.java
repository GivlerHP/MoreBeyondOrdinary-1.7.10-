package ru.givler.mbo.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

public class RenderSlimeBlock implements ISimpleBlockRenderingHandler {
    private final int renderId;
    public RenderSlimeBlock(int renderId) { this.renderId=renderId; }

    @Override public boolean renderWorldBlock(IBlockAccess world,int x,int y,int z,Block block,int modelId,RenderBlocks renderer) {
        renderer.setRenderBounds(0,0,0,1,1,1);
        renderer.renderStandardBlock(block,x,y,z);
        renderer.setRenderBounds(.1875,.1875,.1875,.8125,.8125,.8125);
        boolean old=renderer.renderAllFaces;
        renderer.renderAllFaces=true;
        boolean result=renderer.renderStandardBlock(block,x,y,z);
        renderer.renderAllFaces=old;
        renderer.setRenderBounds(0,0,0,1,1,1);
        return result;
    }

    @Override public void renderInventoryBlock(Block block,int meta,int modelId,RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(-.5F,-.5F,-.5F);
        renderCube(block,meta,renderer,.1875,.1875,.1875,.8125,.8125,.8125);
        renderCube(block,meta,renderer,0,0,0,1,1,1);
        GL11.glPopMatrix();
    }

    private void renderCube(Block block,int meta,RenderBlocks r,double minX,double minY,double minZ,double maxX,double maxY,double maxZ) {
        r.setRenderBounds(minX,minY,minZ,maxX,maxY,maxZ);
        Tessellator t=Tessellator.instance;
        t.startDrawingQuads(); t.setNormal(0,-1,0); r.renderFaceYNeg(block,0,0,0,block.getIcon(0,meta)); t.draw();
        t.startDrawingQuads(); t.setNormal(0,1,0); r.renderFaceYPos(block,0,0,0,block.getIcon(1,meta)); t.draw();
        t.startDrawingQuads(); t.setNormal(0,0,-1); r.renderFaceZNeg(block,0,0,0,block.getIcon(2,meta)); t.draw();
        t.startDrawingQuads(); t.setNormal(0,0,1); r.renderFaceZPos(block,0,0,0,block.getIcon(3,meta)); t.draw();
        t.startDrawingQuads(); t.setNormal(-1,0,0); r.renderFaceXNeg(block,0,0,0,block.getIcon(4,meta)); t.draw();
        t.startDrawingQuads(); t.setNormal(1,0,0); r.renderFaceXPos(block,0,0,0,block.getIcon(5,meta)); t.draw();
    }

    @Override public boolean shouldRender3DInInventory(int id) { return true; }
    @Override public int getRenderId() { return renderId; }
}
