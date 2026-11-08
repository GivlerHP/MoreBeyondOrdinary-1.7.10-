package ru.givler.mbo.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.block.BlockShelf;

/** Static original shelf geometry, emitted into the chunk mesh rather than a ticking TESR. */
public final class RenderShelf implements ISimpleBlockRenderingHandler {
    private static final float[][] BODY={
        {1.0F,0.0F,0.0F,1.0F,1.0F,1.0F,8.0F,0.0F,1.0F,0.0F,1.0F,8.0F,8.0F,1.0F,0.0F,0.8125F,9.5F,8.0F,1.0F,1.0F,0.8125F,9.5F,0.0F},
        {0.0F,0.0F,1.0F,0.0F,1.0F,1.0F,8.0F,0.0F,0.0F,0.0F,1.0F,8.0F,8.0F,1.0F,0.0F,1.0F,16.0F,8.0F,1.0F,1.0F,1.0F,16.0F,0.0F},
        {-1.0F,0.0F,0.0F,0.0F,1.0F,0.8125F,14.5F,0.0F,0.0F,0.0F,0.8125F,14.5F,8.0F,0.0F,0.0F,1.0F,16.0F,8.0F,0.0F,1.0F,1.0F,16.0F,0.0F},
        {0.0F,1.0F,0.0F,0.0F,1.0F,0.8125F,16.0F,5.0F,0.0F,1.0F,1.0F,16.0F,3.5F,1.0F,1.0F,1.0F,8.0F,3.5F,1.0F,1.0F,0.8125F,8.0F,5.0F},
        {0.0F,-1.0F,0.0F,0.0F,0.0F,1.0F,16.0F,6.0F,0.0F,0.0F,0.8125F,16.0F,4.5F,1.0F,0.0F,0.8125F,8.0F,4.5F,1.0F,0.0F,1.0F,8.0F,6.0F},
        {0.0F,0.0F,-1.0F,1.0F,0.25F,0.6875F,0.0F,6.0F,1.0F,0.0F,0.6875F,0.0F,8.0F,0.0F,0.0F,0.6875F,8.0F,8.0F,0.0F,0.25F,0.6875F,8.0F,6.0F},
        {1.0F,0.0F,0.0F,1.0F,0.25F,0.8125F,1.5F,6.0F,1.0F,0.0F,0.8125F,1.5F,8.0F,1.0F,0.0F,0.6875F,2.5F,8.0F,1.0F,0.25F,0.6875F,2.5F,6.0F},
        {-1.0F,0.0F,0.0F,0.0F,0.25F,0.6875F,5.5F,6.0F,0.0F,0.0F,0.6875F,5.5F,8.0F,0.0F,0.0F,0.8125F,6.5F,8.0F,0.0F,0.25F,0.8125F,6.5F,6.0F},
        {0.0F,1.0F,0.0F,0.0F,0.25F,0.6875F,8.0F,3.5F,0.0F,0.25F,0.8125F,8.0F,4.5F,1.0F,0.25F,0.8125F,16.0F,4.5F,1.0F,0.25F,0.6875F,16.0F,3.5F},
        {0.0F,-1.0F,0.0F,0.0F,0.0F,0.8125F,16.0F,4.5F,0.0F,0.0F,0.6875F,16.0F,3.5F,1.0F,0.0F,0.6875F,8.0F,3.5F,1.0F,0.0F,0.8125F,8.0F,4.5F},
        {0.0F,0.0F,-1.0F,1.0F,1.0F,0.6875F,0.0F,0.0F,1.0F,0.75F,0.6875F,0.0F,2.0F,0.0F,0.75F,0.6875F,8.0F,2.0F,0.0F,1.0F,0.6875F,8.0F,0.0F},
        {1.0F,0.0F,0.0F,1.0F,1.0F,0.8125F,1.5F,0.0F,1.0F,0.75F,0.8125F,1.5F,2.0F,1.0F,0.75F,0.6875F,2.5F,2.0F,1.0F,1.0F,0.6875F,2.5F,0.0F},
        {-1.0F,0.0F,0.0F,0.0F,1.0F,0.6875F,5.5F,0.0F,0.0F,0.75F,0.6875F,5.5F,2.0F,0.0F,0.75F,0.8125F,6.5F,2.0F,0.0F,1.0F,0.8125F,6.5F,0.0F},
        {0.0F,1.0F,0.0F,0.0F,1.0F,0.6875F,16.0F,6.0F,0.0F,1.0F,0.8125F,16.0F,5.0F,1.0F,1.0F,0.8125F,8.0F,5.0F,1.0F,1.0F,0.6875F,8.0F,6.0F},
        {0.0F,-1.0F,0.0F,0.0F,0.75F,0.8125F,8.0F,5.0F,0.0F,0.75F,0.6875F,8.0F,6.0F,1.0F,0.75F,0.6875F,16.0F,6.0F,1.0F,0.75F,0.8125F,16.0F,5.0F}
    };
    private static void emit(IIcon icon,int facing,double x,double y,double z,float[] q,boolean inventory) {
        Tessellator t=Tessellator.instance;
        int turns=facing==5?1:facing==3?2:facing==4?3:0;
        float nx=q[0],ny=q[1],nz=q[2];
        for(int n=0;n<turns;n++){float tmp=nx;nx=-nz;nz=tmp;}
        if(inventory)t.setNormal(nx,ny,nz);
        float shade=ny>0?1:ny<0?.5F:nz!=0?.8F:.6F;t.setColorOpaque_F(shade,shade,shade);
        for(int i=3;i<23;i+=5){
            double vx=q[i]-.5,vz=q[i+2]-.5;
            for(int n=0;n<turns;n++){double tmp=vx;vx=-vz;vz=tmp;}
            t.addVertexWithUV(x+.5+vx,y+q[i+1],z+.5+vz,icon.getInterpolatedU(q[i+3]),icon.getInterpolatedV(q[i+4]));
        }
    }
    private static final float[][] FRONTS=new float[5][];
    static {for(int i=0;i<5;i++) {
        float u=i==1||i==4?8:0,v=i==0?2:i==1||i==3?12:8;
        FRONTS[i]=new float[]{0,0,-1,1,.75F,.8125F,u,v,1,.25F,.8125F,u,v+4,0,.25F,.8125F,u+8,v+4,0,.75F,.8125F,u+8,v};
    }}
    private static void draw(Block block,int meta,int connection,double x,double y,double z,boolean inventory) {
        int facing=meta&7;IIcon icon=block.getIcon(0,meta);
        for(float[] q:BODY)emit(icon,facing,x,y,z,q,inventory);
        emit(icon,facing,x,y,z,FRONTS[(meta&8)==0?0:connection+1],inventory);
    }
    @Override public boolean renderWorldBlock(IBlockAccess world,int x,int y,int z,Block block,int id,RenderBlocks renderer){
        Tessellator.instance.setBrightness(block.getMixedBrightnessForBlock(world,x,y,z));
        int meta=world.getBlockMetadata(x,y,z);
        draw(block,meta,BlockShelf.connection(world,x,y,z),x,y,z,false);return true;
    }
    @Override public void renderInventoryBlock(Block block,int meta,int id,RenderBlocks renderer){
        GL11.glPushMatrix();try {GL11.glTranslatef(-.5F,-.5F,-.5F);
            Tessellator.instance.startDrawingQuads();draw(block,2,0,0,0,0,true);Tessellator.instance.draw();
        } finally {GL11.glPopMatrix();}
    }
    @Override public int getRenderId(){return BlockShelf.renderId;}
    @Override public boolean shouldRender3DInInventory(int id){return true;}
}
