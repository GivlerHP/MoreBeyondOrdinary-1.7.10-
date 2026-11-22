package ru.givler.mbo.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.block.BlockVerticalSlab;
import ru.givler.mbo.client.texture.ConnectedTextureHooks;

public final class RenderVerticalSlab implements ISimpleBlockRenderingHandler {
    private static final float[] SHADE = {.5F, 1F, .8F, .8F, .6F, .6F};

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                    Block block, int modelId, RenderBlocks renderer) {
        BlockVerticalSlab slab = (BlockVerticalSlab) block;
        IBlockAccess materials = new SlabMaterialAccess(world);
        int mask = BlockVerticalSlab.shapeMask(world, x, y, z);
        Tessellator tess = Tessellator.instance;
        int brightness = block.getMixedBrightnessForBlock(world, x, y, z);
        for (int side = 0; side < 6; side++) {
            ForgeDirection direction = ForgeDirection.getOrientation(side);
            int nx = x + direction.offsetX, ny = y + direction.offsetY, nz = z + direction.offsetZ;
            Block neighbor = world.getBlock(nx, ny, nz);
            int other = neighbor instanceof BlockVerticalSlab
                    ? BlockVerticalSlab.shapeMask(world, nx, ny, nz) : neighbor.isOpaqueCube() ? 15 : 0;
            IIcon base = block.getIcon(side, 0);
            IIcon icon = ConnectedTextureHooks.resolve(base, slab, world, x, y, z, side);
            if (icon == base) {
                icon = ConnectedTextureHooks.resolve(base, slab.source, materials, x, y, z, side);
            }
            int externalBrightness = block.getMixedBrightnessForBlock(world, nx, ny, nz);
            for (int quarter = 0; quarter < 4; quarter++) {
                if ((mask & 1 << quarter) == 0) continue;
                boolean external = BlockVerticalSlab.touchesEdge(quarter, side);
                int opposite = BlockVerticalSlab.adjacentQuarter(quarter, side);
                if (((external ? other : mask) & 1 << opposite) != 0) continue;
                double minX = (quarter & 1) * .5, minZ = (quarter >> 1) * .5;
                renderer.setRenderBounds(minX, 0, minZ, minX + .5, 1, minZ + .5);
                tess.setBrightness(external ? externalBrightness : brightness);
                tess.setColorOpaque_F(SHADE[side], SHADE[side], SHADE[side]);
                face(renderer, block, x, y, z, side, icon);
            }
        }
        renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
        return true;
    }

    @Override
    public void renderInventoryBlock(Block block, int meta, int modelId, RenderBlocks renderer) {
        Tessellator tess = Tessellator.instance;
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(-.5F, -.5F, -.25F);
            renderer.setRenderBounds(0, 0, 0, 1, 1, .5);
            for (int side = 0; side < 6; side++) {
                ForgeDirection direction = ForgeDirection.getOrientation(side);
                tess.startDrawingQuads();
                tess.setNormal(direction.offsetX, direction.offsetY, direction.offsetZ);
                face(renderer, block, 0, 0, 0, side, block.getIcon(side, 0));
                tess.draw();
            }
        } finally {
            renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
            GL11.glPopMatrix();
        }
    }

    private static void face(RenderBlocks renderer, Block block, int x, int y, int z, int side, IIcon icon) {
        switch (side) {
            case 0: renderer.renderFaceYNeg(block, x, y, z, icon); break;
            case 1: renderer.renderFaceYPos(block, x, y, z, icon); break;
            case 2: renderer.renderFaceZNeg(block, x, y, z, icon); break;
            case 3: renderer.renderFaceZPos(block, x, y, z, icon); break;
            case 4: renderer.renderFaceXNeg(block, x, y, z, icon); break;
            case 5: renderer.renderFaceXPos(block, x, y, z, icon); break;
            default: throw new IllegalArgumentException("Invalid block face: " + side);
        }
    }

    /** Material rules see source metadata, which is independent of slab direction and connections. */
    private static final class SlabMaterialAccess implements IBlockAccess {
        private final IBlockAccess world;

        SlabMaterialAccess(IBlockAccess world) {
            this.world = world;
        }

        @Override public Block getBlock(int x, int y, int z) {
            Block block = world.getBlock(x, y, z);
            return block instanceof BlockVerticalSlab ? ((BlockVerticalSlab) block).source : block;
        }

        @Override public int getBlockMetadata(int x, int y, int z) {
            Block block = world.getBlock(x, y, z);
            return block instanceof BlockVerticalSlab ? ((BlockVerticalSlab) block).getSourceMetadata()
                    : world.getBlockMetadata(x, y, z);
        }

        @Override public TileEntity getTileEntity(int x, int y, int z) {
            return world.getTileEntity(x, y, z);
        }

        @Override public int getLightBrightnessForSkyBlocks(int x, int y, int z, int minimum) {
            return world.getLightBrightnessForSkyBlocks(x, y, z, minimum);
        }

        @Override public int isBlockProvidingPowerTo(int x, int y, int z, int side) {
            return world.isBlockProvidingPowerTo(x, y, z, side);
        }

        @Override public boolean isAirBlock(int x, int y, int z) {
            return world.isAirBlock(x, y, z);
        }

        @Override public BiomeGenBase getBiomeGenForCoords(int x, int z) {
            return world.getBiomeGenForCoords(x, z);
        }

        @Override public int getHeight() {
            return world.getHeight();
        }

        @Override public boolean extendedLevelsInChunkCache() {
            return world.extendedLevelsInChunkCache();
        }

        @Override public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean fallback) {
            return world.isSideSolid(x, y, z, side, fallback);
        }
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return BlockVerticalSlab.renderId; }
}
