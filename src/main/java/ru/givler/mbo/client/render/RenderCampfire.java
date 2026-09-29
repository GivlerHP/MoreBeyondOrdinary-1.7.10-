package ru.givler.mbo.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.block.BlockCampfire;

public final class RenderCampfire implements ISimpleBlockRenderingHandler {
    private final int renderId;
    public RenderCampfire(int renderId) { this.renderId = renderId; }
    @Override public int getRenderId() { return renderId; }
    @Override public boolean shouldRender3DInInventory(int id) { return true; }

    @Override public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                               Block block, int modelId, RenderBlocks renderer) {
        draw((BlockCampfire) block, x, y, z, (world.getBlockMetadata(x, y, z) & 1) != 0,
                block.getMixedBrightnessForBlock(world, x, y, z), false);
        return true;
    }

    @Override public void renderInventoryBlock(Block block, int meta, int modelId, RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(-.5F, -.5F, -.5F);
        draw((BlockCampfire) block, 0, 0, 0, true, 0xF000F0, true);
        GL11.glPopMatrix();
    }

    private void draw(BlockCampfire block, double x, double y, double z,
                      boolean lit, int brightness, boolean inventory) {
        Tessellator t = Tessellator.instance;
        if (inventory) t.startDrawingQuads();
        t.setBrightness(brightness);
        IIcon log = block.getIcon(0, 0);
        IIcon embers = lit ? block.getLitLogIcon() : log;

        // Bounds and UV regions follow the original five-piece campfire model, in sixteenths.
        box(t, x,y,z, 1,0,0, 5,4,16, log,embers,
                new float[][] {{0,0,16,4},{0,0,16,4},{0,4,4,8},{0,4,4,8},{16,0,0,4},{0,1,16,5}},
                new int[] {1,1,0,0,0,0}, new boolean[] {false,false,false,false,false,true});
        box(t, x,y,z, 11,0,0, 15,4,16, log,embers,
                new float[][] {{0,0,16,4},{0,0,16,4},{0,4,4,8},{0,4,4,8},{16,1,0,5},{0,0,16,4}},
                new int[] {1,1,0,0,0,0}, new boolean[] {false,false,false,false,true,false});
        box(t, x,y,z, 0,3,1, 16,7,5, log,embers,
                new float[][] {{0,4,16,8},{0,0,16,4},{0,0,16,4},{16,0,0,4},{0,4,4,8},{0,4,4,8}},
                new int[] {0,2,0,0,0,0}, new boolean[] {true,false,true,true,false,false});
        box(t, x,y,z, 0,3,11, 16,7,15, log,embers,
                new float[][] {{0,4,16,8},{0,0,16,4},{16,0,0,4},{0,0,16,4},{0,4,4,8},{0,4,4,8}},
                new int[] {0,2,0,0,0,0}, new boolean[] {true,false,true,true,false,false});
        box(t, x,y,z, 5,0,0, 11,1,16, log,embers,
                new float[][] {{0,8,16,14},{0,8,16,14},{0,15,6,16},{10,15,16,16},{0,8,16,14},{0,8,16,14}},
                new int[] {1,1,0,0,0,0}, new boolean[] {false,true,false,false,true,true});

        if (lit) {
            t.setBrightness(0xF000F0);
            t.setColorOpaque_F(1,1,1);
            flame(t, block.getFireIcon(), x, y, z);
            t.setBrightness(brightness);
        }
        if (inventory) t.draw();
    }

    private void box(Tessellator t, double x, double y, double z,
                     int x1, int y1, int z1, int x2, int y2, int z2,
                     IIcon log, IIcon embers, float[][] uv, int[] turns, boolean[] glowing) {
        double a=x+x1/16.0, b=x+x2/16.0, c=y+y1/16.0, d=y+y2/16.0, e=z+z1/16.0, f=z+z2/16.0;
        for (int side=0; side<6; side++) {
            float shade=side==0?.5F:side==1?1F:side<4?.8F:.6F;
            t.setColorOpaque_F(shade,shade,shade);
            double[][] p;
            switch (side) {
                case 0: p=new double[][]{{a,c,e},{b,c,e},{b,c,f},{a,c,f}}; break;
                case 1: p=new double[][]{{a,d,f},{b,d,f},{b,d,e},{a,d,e}}; break;
                case 2: p=new double[][]{{b,c,e},{a,c,e},{a,d,e},{b,d,e}}; break;
                case 3: p=new double[][]{{a,c,f},{b,c,f},{b,d,f},{a,d,f}}; break;
                case 4: p=new double[][]{{a,c,e},{a,c,f},{a,d,f},{a,d,e}}; break;
                default: p=new double[][]{{b,c,f},{b,c,e},{b,d,e},{b,d,f}};
            }
            face(t,p,glowing[side]?embers:log,uv[side],turns[side]);
        }
    }

    private void face(Tessellator t, double[][] p, IIcon icon, float[] uv, int turns) {
        double u0=icon.getInterpolatedU(uv[0]), v0=icon.getInterpolatedV(uv[1]);
        double u1=icon.getInterpolatedU(uv[2]), v1=icon.getInterpolatedV(uv[3]);
        double[][] corners={{u0,v1},{u1,v1},{u1,v0},{u0,v0}};
        for (int i=0;i<4;i++) {
            double[] tex=corners[(i+turns)&3];
            t.addVertexWithUV(p[i][0],p[i][1],p[i][2],tex[0],tex[1]);
        }
    }

    private void flame(Tessellator t, IIcon icon, double x, double y, double z) {
        double low=y+.0625, high=y+1.0625;
        double[][][] sheets={
                {{x+.05,low,z+.05},{x+.95,low,z+.95},{x+.95,high,z+.95},{x+.05,high,z+.05}},
                {{x+.95,low,z+.05},{x+.05,low,z+.95},{x+.05,high,z+.95},{x+.95,high,z+.05}}
        };
        for (double[][] sheet:sheets) {
            face(t,sheet,icon,new float[]{0,0,16,16},0);
            face(t,new double[][]{sheet[1],sheet[0],sheet[3],sheet[2]},icon,new float[]{0,0,16,16},0);
        }
    }
}
