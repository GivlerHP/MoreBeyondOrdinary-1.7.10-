package ru.givler.mbo.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.block.BlockGroundCover;
import ru.givler.mbo.client.texture.ConnectedTextureHooks;

public final class RenderGroundCover implements ISimpleBlockRenderingHandler {
    private static final float[] SHADE = {.5F, 1F, .8F, .8F, .6F, .6F};

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                    Block block, int modelId, RenderBlocks renderer) {
        BlockGroundCover cover = (BlockGroundCover) block;
        IBlockAccess materials = new GroundMaterialAccess(world);
        double height = BlockGroundCover.height(world.getBlockMetadata(x, y, z));
        int tint = cover.topColor(materials, x, y, z);
        Tessellator tess = Tessellator.instance;
        for (int side = 0; side < 6; side++) {
            ForgeDirection face = ForgeDirection.getOrientation(side);
            int nx = x + face.offsetX, ny = y + face.offsetY, nz = z + face.offsetZ;
            Block neighbor = world.getBlock(nx, ny, nz);
            double bottom = 0;
            if (side >= 2 && neighbor instanceof BlockGroundCover) {
                bottom = BlockGroundCover.height(world.getBlockMetadata(nx, ny, nz));
            } else if ((side != 1 || height == 1)
                    && (neighbor.isOpaqueCube() || neighbor instanceof BlockGroundCover
                    && neighbor.isSideSolid(world, nx, ny, nz, face.getOpposite()))) {
                continue;
            }
            if (bottom >= height) continue;
            renderer.setRenderBounds(0, bottom, 0, 1, height, 1);
            boolean insetTop = side == 1 && height < 1;
            tess.setBrightness(block.getMixedBrightnessForBlock(world,
                    insetTop ? x : nx, insetTop ? y : ny, insetTop ? z : nz));
            color(tess, side, side == 1 ? tint : 0xFFFFFF);
            IIcon base = cover.getIcon(side, 0);
            IIcon icon = ConnectedTextureHooks.resolve(base, cover, world, x, y, z, side);
            if (icon == base) icon = ConnectedTextureHooks.resolve(base, cover.source, materials, x, y, z, side);
            drawFace(renderer, block, x, y, z, side, height, icon);
            if (side >= 2 && cover.source == Blocks.grass && icon == base && !renderer.hasOverrideBlockTexture()) {
                color(tess, side, tint);
                drawFace(renderer, block, x, y, z, side, height, BlockGrass.getIconSideOverlay());
            }
        }
        renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
        return true;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        BlockGroundCover cover = (BlockGroundCover) block;
        Tessellator tess = Tessellator.instance;
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(-.5F, -.125F, -.5F);
            renderer.setRenderBounds(0, 0, 0, 1, BlockGroundCover.height(0), 1);
            for (int side = 0; side < 6; side++) {
                ForgeDirection face = ForgeDirection.getOrientation(side);
                tess.startDrawingQuads();
                tess.setNormal(face.offsetX, face.offsetY, face.offsetZ);
                tess.setColorOpaque_I(side == 1 ? cover.inventoryTopColor() : 0xFFFFFF);
                drawFace(renderer, block, 0, 0, 0, side, BlockGroundCover.height(0), cover.getIcon(side, 0));
                if (side >= 2 && cover.source == Blocks.grass) {
                    tess.setColorOpaque_I(cover.inventoryTopColor());
                    drawFace(renderer, block, 0, 0, 0, side, BlockGroundCover.height(0), BlockGrass.getIconSideOverlay());
                }
                tess.draw();
            }
        } finally {
            renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
            GL11.glPopMatrix();
        }
    }

    private static void drawFace(RenderBlocks renderer, Block block, int x, int y, int z,
                                 int side, double height, IIcon icon) {
        // The grass line belongs at the top of each layer, just as in the source block.
        if (side >= 2 && height < 1 && !renderer.hasOverrideBlockTexture()) {
            icon = new TopAlignedIcon(icon, (1 - height) * 16);
        }
        RenderVerticalSlab.face(renderer, block, x, y, z, side, icon);
    }

    private static final class TopAlignedIcon implements IIcon {
        private final IIcon icon;
        private final double offset;

        TopAlignedIcon(IIcon icon, double offset) {
            this.icon = icon;
            this.offset = offset;
        }

        @Override public int getIconWidth() { return icon.getIconWidth(); }
        @Override public int getIconHeight() { return icon.getIconHeight(); }
        @Override public float getMinU() { return icon.getMinU(); }
        @Override public float getMaxU() { return icon.getMaxU(); }
        @Override public float getMinV() { return icon.getMinV(); }
        @Override public float getMaxV() { return icon.getMaxV(); }
        @Override public float getInterpolatedU(double u) { return icon.getInterpolatedU(u); }
        @Override public float getInterpolatedV(double v) { return icon.getInterpolatedV(Math.max(0, v - offset)); }
        @Override public String getIconName() { return icon.getIconName(); }
    }

    private static void color(Tessellator tess, int side, int rgb) {
        float shade = SHADE[side] / 255F;
        tess.setColorOpaque_F((rgb >> 16 & 255) * shade, (rgb >> 8 & 255) * shade, (rgb & 255) * shade);
    }

    @Override public boolean shouldRender3DInInventory(int id) { return true; }
    @Override public int getRenderId() { return BlockGroundCover.renderId; }
}
