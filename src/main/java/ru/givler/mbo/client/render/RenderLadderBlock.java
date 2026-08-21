package ru.givler.mbo.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.MoreBeyondOrdinary;

public final class RenderLadderBlock implements ISimpleBlockRenderingHandler {
    private final int renderId;
    private IIcon baseIcon;

    public RenderLadderBlock(int renderId) { this.renderId = renderId; }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() == 0) {
            baseIcon = event.map.registerIcon(MoreBeyondOrdinary.MODID + ":ladder_base");
        }
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        IIcon icon = Blocks.ladder.getIcon(0, 0);
        Tessellator t = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, 0F);
        t.startDrawingQuads();
        t.setColorOpaque_F(1F, 1F, 1F);
        t.setNormal(0F, 0F, 1F);
        renderer.renderFaceZPos(block, 0D, 0D, 0D, icon);
        t.draw();
        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                    Block block, int modelId, RenderBlocks renderer) {
        int meta = world.getBlockMetadata(x, y, z);
        IIcon icon = baseIcon != null ? baseIcon : block.getIcon(0, meta);
        Tessellator t=Tessellator.instance;
        t.setBrightness(block.getMixedBrightnessForBlock(world,x,y,z));
        element(t,icon,meta,x,y,z,2,0,14,4,16,16,
                uv(12,0,14,16),uv(4,0,2,16),uv(2,0,4,16),uv(12,0,14,16),uv(7,3,9,5),uv(12,14,14,16));
        element(t,icon,meta,x,y,z,12,0,14,14,16,16,
                uv(2,0,4,16),uv(14,0,12,16),uv(12,0,14,16),uv(2,0,4,16),uv(7,3,9,5),uv(2,14,4,16));
        element(t,icon,meta,x,y,z,1,11,14.5,15,13,15.5,
                uv(1,3,15,5),uv(1,3,2,5),uv(1,3,15,5),uv(14,3,15,5),uv(1,3,15,4),uv(1,4,15,5));
        element(t,icon,meta,x,y,z,1,3,14.5,15,5,15.5,
                uv(1,11,15,13),uv(1,11,2,13),uv(1,11,15,13),uv(14,11,15,13),uv(1,11,15,12),uv(1,12,15,13));
        return true;
    }

    private static double[] uv(double a,double b,double c,double d){return new double[]{a,b,c,d};}
    private static void element(Tessellator t,IIcon i,int meta,int wx,int wy,int wz,
            double x1,double y1,double z1,double x2,double y2,double z2,
            double[] n,double[] e,double[] s,double[] w,double[] u,double[] d){
        x1/=16;x2/=16;y1/=16;y2/=16;z1/=16;z2/=16;
        face(t,i,meta,wx,wy,wz,n,.8F,x2,y1,z1,x1,y1,z1,x1,y2,z1,x2,y2,z1);
        face(t,i,meta,wx,wy,wz,e,.6F,x2,y1,z2,x2,y1,z1,x2,y2,z1,x2,y2,z2);
        face(t,i,meta,wx,wy,wz,s,.8F,x1,y1,z2,x2,y1,z2,x2,y2,z2,x1,y2,z2);
        face(t,i,meta,wx,wy,wz,w,.6F,x1,y1,z1,x1,y1,z2,x1,y2,z2,x1,y2,z1);
        face(t,i,meta,wx,wy,wz,u,1F,x1,y2,z2,x2,y2,z2,x2,y2,z1,x1,y2,z1);
        face(t,i,meta,wx,wy,wz,d,.5F,x1,y1,z1,x2,y1,z1,x2,y1,z2,x1,y1,z2);
    }
    private static void face(Tessellator t,IIcon i,int meta,int wx,int wy,int wz,double[] q,float shade,double... p){
        double u1=i.getInterpolatedU(q[0]),v1=i.getInterpolatedV(q[1]);
        double u2=i.getInterpolatedU(q[2]),v2=i.getInterpolatedV(q[3]);
        t.setColorOpaque_F(shade,shade,shade);
        put(t,meta,wx,wy,wz,p[0],p[1],p[2],u1,v2);put(t,meta,wx,wy,wz,p[3],p[4],p[5],u2,v2);
        put(t,meta,wx,wy,wz,p[6],p[7],p[8],u2,v1);put(t,meta,wx,wy,wz,p[9],p[10],p[11],u1,v1);
    }
    private static void put(Tessellator t,int meta,int wx,int wy,int wz,double x,double y,double z,double u,double v){
        double rx=x,rz=z;
        if(meta==3){rx=1-x;rz=1-z;}else if(meta==4){rx=z;rz=1-x;}else if(meta==5){rx=1-z;rz=x;}
        t.addVertexWithUV(wx+rx,wy+y,wz+rz,u,v);
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return false; }
    @Override public int getRenderId() { return renderId; }
}
