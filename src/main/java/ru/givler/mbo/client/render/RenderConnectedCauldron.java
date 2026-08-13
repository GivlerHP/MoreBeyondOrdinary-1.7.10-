package ru.givler.mbo.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.core.CauldronHooks;

public class RenderConnectedCauldron implements ISimpleBlockRenderingHandler {
    private final int renderId;
    public RenderConnectedCauldron(int renderId) { this.renderId = renderId; }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        boolean west = CauldronHooks.canConnect(world,x,y,z,x-1,y,z);
        boolean east = CauldronHooks.canConnect(world,x,y,z,x+1,y,z);
        boolean north = CauldronHooks.canConnect(world,x,y,z,x,y,z-1);
        boolean south = CauldronHooks.canConnect(world,x,y,z,x,y,z+1);

        renderFloorSlab(world,renderer,block,x,y,z,
                west?0:.125,.25,north?0:.125,
                east?1:.875,.3125,south?1:.875);
        if (!west) renderPart(renderer,block,x,y,z,0,0,0,.125,1,1);
        if (!east) renderPart(renderer,block,x,y,z,.875,0,0,1,1,1);
        if (!north) renderPart(renderer,block,x,y,z,west?0:.125,0,0,east?1:.875,1,.125);
        if (!south) renderPart(renderer,block,x,y,z,west?0:.125,0,.875,east?1:.875,1,1);

        int fluidMetadata=getConnectedLevel(world,x,y,z);
        int level=fluidMetadata&3;
        if(level>0) renderFluid(world,x,y,z,level,(fluidMetadata&4)!=0,west,east,north,south);
        renderer.setRenderBounds(0,0,0,1,1,1);
        return true;
    }

    private void renderPart(RenderBlocks renderer, Block block, int x,int y,int z,
                            double minX,double minY,double minZ,double maxX,double maxY,double maxZ) {
        renderer.setRenderBounds(minX,minY,minZ,maxX,maxY,maxZ);
        renderer.renderStandardBlock(block,x,y,z);
    }

    private void renderFloorSlab(IBlockAccess world,RenderBlocks renderer,Block block,int x,int y,int z,
                                 double minX,double minY,double minZ,double maxX,double maxY,double maxZ) {
        renderer.setRenderBounds(minX,minY,minZ,maxX,maxY,maxZ);
        Tessellator t=Tessellator.instance;
        int brightness=block.getMixedBrightnessForBlock(world,x,y,z);
        t.setBrightness(brightness);

        t.setColorOpaque_F(.5F,.5F,.5F);
        renderSlabUnderside(t,x,y,z,minX,minY-.0001,minZ,maxX,maxZ);
        t.setColorOpaque_F(1,1,1);
        renderer.renderFaceYPos(block,x,y,z,BlockCauldron.getCauldronIcon("inner"));
        t.setColorOpaque_F(.8F,.8F,.8F);
        renderer.renderFaceZNeg(block,x,y,z,block.getIcon(2,world.getBlockMetadata(x,y,z)));
        renderer.renderFaceZPos(block,x,y,z,block.getIcon(3,world.getBlockMetadata(x,y,z)));
        t.setColorOpaque_F(.6F,.6F,.6F);
        renderer.renderFaceXNeg(block,x,y,z,block.getIcon(4,world.getBlockMetadata(x,y,z)));
        renderer.renderFaceXPos(block,x,y,z,block.getIcon(5,world.getBlockMetadata(x,y,z)));
        t.setColorOpaque_F(1,1,1);
    }

    private void renderSlabUnderside(Tessellator t,int x,int y,int z,
                                     double minX,double height,double minZ,double maxX,double maxZ) {
        IIcon icon=BlockCauldron.getCauldronIcon("inner");
        t.addVertexWithUV(x+minX,y+height,z+minZ,icon.getMinU(),icon.getMinV());
        t.addVertexWithUV(x+minX,y+height,z+maxZ,icon.getMinU(),icon.getMaxV());
        t.addVertexWithUV(x+maxX,y+height,z+maxZ,icon.getMaxU(),icon.getMaxV());
        t.addVertexWithUV(x+maxX,y+height,z+minZ,icon.getMaxU(),icon.getMinV());
        t.addVertexWithUV(x+maxX,y+height,z+minZ,icon.getMaxU(),icon.getMinV());
        t.addVertexWithUV(x+maxX,y+height,z+maxZ,icon.getMaxU(),icon.getMaxV());
        t.addVertexWithUV(x+minX,y+height,z+maxZ,icon.getMinU(),icon.getMaxV());
        t.addVertexWithUV(x+minX,y+height,z+minZ,icon.getMinU(),icon.getMinV());
    }

    private void renderFluid(IBlockAccess world,int x,int y,int z,int level,boolean lava,
                             boolean west,boolean east,boolean north,boolean south) {
        Tessellator t=Tessellator.instance;
        Block fluid=lava?Blocks.lava:Blocks.water;
        IIcon icon=fluid.getIcon(1,0);
        int color=fluid.colorMultiplier(world,x,y,z);
        float r=((color>>16)&255)/255F, g=((color>>8)&255)/255F, b=(color&255)/255F;
        t.setBrightness(lava?15728880:fluid.getMixedBrightnessForBlock(world,x,y,z));
        t.setColorRGBA_F(r,g,b,lava?1F:.8F);
        double height=y+(6+level*3)/16.0;
        renderSurface(t,icon,x,height,z,west?0:.1251,east?1:.8749,north?0:.1251,south?1:.8749);
    }

    private void renderSurface(Tessellator t,IIcon icon,double x,double y,double z,
                               double minX,double maxX,double minZ,double maxZ) {
        t.addVertexWithUV(x+minX,y,z+maxZ,icon.getMinU(),icon.getMaxV());
        t.addVertexWithUV(x+maxX,y,z+maxZ,icon.getMaxU(),icon.getMaxV());
        t.addVertexWithUV(x+maxX,y,z+minZ,icon.getMaxU(),icon.getMinV());
        t.addVertexWithUV(x+minX,y,z+minZ,icon.getMinU(),icon.getMinV());
    }

    private int getConnectedLevel(IBlockAccess world,int x,int y,int z) {
        return CauldronHooks.getConnectedMetadata(world,x,y,z);
    }

    @Override
    public void renderInventoryBlock(Block block,int metadata,int modelId,RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(-.5F,-.5F,-.5F);
        renderInventoryPart(renderer,block,metadata,0,0,0,1,.3125,1);
        renderInventoryPart(renderer,block,metadata,0,0,0,.125,1,1);
        renderInventoryPart(renderer,block,metadata,.875,0,0,1,1,1);
        renderInventoryPart(renderer,block,metadata,0,0,0,1,1,.125);
        renderInventoryPart(renderer,block,metadata,0,0,.875,1,1,1);
        GL11.glPopMatrix();
    }

    private void renderInventoryPart(RenderBlocks renderer,Block block,int meta,
                                     double minX,double minY,double minZ,double maxX,double maxY,double maxZ) {
        renderer.setRenderBounds(minX,minY,minZ,maxX,maxY,maxZ);
        Tessellator t=Tessellator.instance;
        t.startDrawingQuads(); t.setNormal(0,-1,0); renderer.renderFaceYNeg(block,0,0,0,block.getIcon(0,meta)); t.draw();
        t.startDrawingQuads(); t.setNormal(0,1,0); renderer.renderFaceYPos(block,0,0,0,block.getIcon(1,meta)); t.draw();
        t.startDrawingQuads(); t.setNormal(0,0,-1); renderer.renderFaceZNeg(block,0,0,0,block.getIcon(2,meta)); t.draw();
        t.startDrawingQuads(); t.setNormal(0,0,1); renderer.renderFaceZPos(block,0,0,0,block.getIcon(3,meta)); t.draw();
        t.startDrawingQuads(); t.setNormal(-1,0,0); renderer.renderFaceXNeg(block,0,0,0,block.getIcon(4,meta)); t.draw();
        t.startDrawingQuads(); t.setNormal(1,0,0); renderer.renderFaceXPos(block,0,0,0,block.getIcon(5,meta)); t.draw();
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return renderId; }
}
