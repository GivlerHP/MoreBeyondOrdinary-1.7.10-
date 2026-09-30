package ru.givler.mbo.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Draws the four leaf-litter coverage shapes from the modern template models. */
public final class RenderLeafLitter implements ISimpleBlockRenderingHandler {
    private static final double HEIGHT = .25D / 16D;
    private final int renderId;

    public RenderLeafLitter(int renderId) { this.renderId = renderId; }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                    Block block, int modelId, RenderBlocks renderer) {
        int meta = world.getBlockMetadata(x, y, z);
        int layers = (meta & 3) + 1;
        int rotation = (meta >>> 2) & 3;
        Tessellator t = Tessellator.instance;
        t.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        int color = block.colorMultiplier(world, x, y, z);
        t.setColorOpaque_F(((color >>> 16) & 255) / 255F,
                ((color >>> 8) & 255) / 255F, (color & 255) / 255F);
        IIcon icon = block.getIcon(1, 0);
        if (layers == 4) draw(t, icon, x, y, z, rotation, 0, 0, 1, 1);
        else {
            if (layers == 1) draw(t, icon, x, y, z, rotation, 0, 0, .5, .5);
            else draw(t, icon, x, y, z, rotation, 0, 0, .5, 1);
            if (layers == 3) draw(t, icon, x, y, z, rotation, .5, .5, 1, 1);
        }
        return true;
    }

    private static void draw(Tessellator t, IIcon icon, int wx, int wy, int wz,
                             int rotation, double minX, double minZ, double maxX, double maxZ) {
        vertex(t, icon, wx, wy, wz, rotation, minX, minZ);
        vertex(t, icon, wx, wy, wz, rotation, minX, maxZ);
        vertex(t, icon, wx, wy, wz, rotation, maxX, maxZ);
        vertex(t, icon, wx, wy, wz, rotation, maxX, minZ);
    }

    private static void vertex(Tessellator t, IIcon icon, int wx, int wy, int wz,
                               int rotation, double x, double z) {
        double rx, rz;
        switch (rotation) {
            case 1: rx = 1 - z; rz = x; break;
            case 2: rx = 1 - x; rz = 1 - z; break;
            case 3: rx = z; rz = 1 - x; break;
            default: rx = x; rz = z;
        }
        t.addVertexWithUV(wx + rx, wy + HEIGHT, wz + rz,
                icon.getInterpolatedU(x * 16), icon.getInterpolatedV(z * 16));
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        Tessellator t = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslatef(-.5F, -.5F, 0F);
        t.startDrawingQuads();
        t.setColorOpaque_F(1F, 1F, 1F);
        t.setNormal(0, 0, 1);
        renderer.renderFaceZPos(block, 0, 0, 0, block.getIcon(1, 0));
        t.draw();
        GL11.glPopMatrix();
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return false; }
    @Override public int getRenderId() { return renderId; }
}
