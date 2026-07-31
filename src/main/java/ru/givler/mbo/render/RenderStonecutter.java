package ru.givler.mbo.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.stonecutter.BlockStonecutter;

public class RenderStonecutter implements ISimpleBlockRenderingHandler {
    private final int id;
    public RenderStonecutter(int id){this.id=id;}
    @Override public boolean renderWorldBlock(IBlockAccess world,int x,int y,int z,Block block,int modelId,RenderBlocks r){
        int meta=world.getBlockMetadata(x,y,z)&3;
        r.setRenderBounds(0,0,0,1,.5625,1);
        r.renderStandardBlock(block,x,y,z);
        r.setRenderAllFaces(true);
        if((meta&1)==0) {
            r.setRenderBounds(0,0,.5,1,.4375,.5);
            r.renderFaceZNeg(block,x,y+.5625,z,((BlockStonecutter)block).saw);
            r.renderFaceZPos(block,x,y+.5625,z,((BlockStonecutter)block).saw);
        } else {
            r.setRenderBounds(.5,0,0,.5,.4375,1);
            r.renderFaceXNeg(block,x,y+.5625,z,((BlockStonecutter)block).saw);
            r.renderFaceXPos(block,x,y+.5625,z,((BlockStonecutter)block).saw);
        }
        r.setRenderAllFaces(false);
        return true;
    }
    @Override public void renderInventoryBlock(Block block,int meta,int modelId,RenderBlocks r){
        GL11.glPushMatrix(); GL11.glTranslatef(-.5F,-.5F,-.5F);
        cube(block,meta,r,0,0,0,1,.5625,1);
        r.setRenderBounds(0,0,.5,1,.4375,.5);
        Tessellator t=Tessellator.instance;
        t.startDrawingQuads();t.setNormal(0,0,-1);
        r.renderFaceZNeg(block,0,.5625,0,((BlockStonecutter)block).saw);t.draw();
        t.startDrawingQuads();t.setNormal(0,0,1);
        r.renderFaceZPos(block,0,.5625,0,((BlockStonecutter)block).saw);t.draw();
        GL11.glPopMatrix();
    }
    private void cube(Block b,int m,RenderBlocks r,double a,double c,double d,double e,double f,double g){
        r.setRenderBounds(a,c,d,e,f,g); Tessellator t=Tessellator.instance;
        t.startDrawingQuads();t.setNormal(0,-1,0);r.renderFaceYNeg(b,0,0,0,b.getIcon(0,m));t.draw();
        t.startDrawingQuads();t.setNormal(0,1,0);r.renderFaceYPos(b,0,0,0,b.getIcon(1,m));t.draw();
        t.startDrawingQuads();t.setNormal(0,0,-1);r.renderFaceZNeg(b,0,0,0,b.getIcon(2,m));t.draw();
        t.startDrawingQuads();t.setNormal(0,0,1);r.renderFaceZPos(b,0,0,0,b.getIcon(3,m));t.draw();
        t.startDrawingQuads();t.setNormal(-1,0,0);r.renderFaceXNeg(b,0,0,0,b.getIcon(4,m));t.draw();
        t.startDrawingQuads();t.setNormal(1,0,0);r.renderFaceXPos(b,0,0,0,b.getIcon(5,m));t.draw();
    }
    @Override public boolean shouldRender3DInInventory(int id){return true;}
    @Override public int getRenderId(){return id;}
}
