package ru.givler.mbo.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.block.BlockVerticalSlab;
import ru.givler.mbo.block.BlockQuarterColumn;
import ru.givler.mbo.client.texture.ConnectedTextureHooks;

public final class RenderVerticalSlab implements ISimpleBlockRenderingHandler {
    private static final float[] SHADE = {.5F, 1F, .8F, .8F, .6F, .6F};

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                    Block block, int modelId, RenderBlocks renderer) {
        BlockVerticalSlab slab = (BlockVerticalSlab) block;
        IBlockAccess materials = new GroundMaterialAccess(world);
        boolean cube = BlockQuarterColumn.isCube(block);
        int mask = cube ? BlockQuarterColumn.cubeMask(world, x, y, z) : BlockVerticalSlab.shapeMask(world, x, y, z);
        Tessellator tess = Tessellator.instance;
        boolean blocksLight = block.getLightOpacity(world, x, y, z) > 0;
        int brightness = blocksLight ? 0 : block.getMixedBrightnessForBlock(world, x, y, z);
        for (int side = 0; side < 6; side++) {
            ForgeDirection direction = ForgeDirection.getOrientation(side);
            int nx = x + direction.offsetX, ny = y + direction.offsetY, nz = z + direction.offsetZ;
            Block neighbor = world.getBlock(nx, ny, nz);
            int other = neighbor instanceof BlockVerticalSlab
                    ? BlockVerticalSlab.shapeMask(world, nx, ny, nz) : neighbor.isOpaqueCube() ? 15 : 0;
            if (BlockQuarterColumn.isCube(neighbor)) {
                int occupied = BlockQuarterColumn.cubeMask(world, nx, ny, nz);
                other = cube ? occupied : side == 0 ? occupied >> 4 : side == 1 ? occupied & 15 : occupied & (occupied >> 4);
            } else if (cube) {
                other |= other << 4;
            }
            IIcon base = block.getIcon(side, 0);
            IIcon icon = ConnectedTextureHooks.resolve(base, slab, world, x, y, z, side);
            if (icon == base) {
                icon = ConnectedTextureHooks.resolve(base, slab.source, materials, x, y, z, side);
            }
            int externalBrightness = block.getMixedBrightnessForBlock(world, nx, ny, nz);
            for (int remaining = mask; remaining != 0; remaining &= remaining - 1) {
                int quarter = Integer.numberOfTrailingZeros(remaining);
                boolean external = cube ? BlockQuarterColumn.touchesCubeFace(quarter, side)
                    : BlockVerticalSlab.touchesEdge(quarter, side);
                int opposite = cube ? BlockQuarterColumn.adjacentCube(quarter, side)
                    : BlockVerticalSlab.adjacentQuarter(quarter, side);
                if (((external ? other : mask) & 1 << opposite) != 0) continue;
                double minY = (quarter >> 2) * .5, maxY = cube ? minY + .5 : 1;
                double minX = (quarter & 1) * .5, minZ = (quarter >> 1 & 1) * .5;
                renderer.setRenderBounds(minX, minY, minZ, minX + .5, maxY, minZ + .5);
                // A recessed face of a light barrier still faces the air on this side.
                // The barrier's own cell has no usable light value for that face.
                tess.setBrightness(external || blocksLight ? externalBrightness : brightness);
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
            float width = block instanceof BlockQuarterColumn ? .5F : 1F;
            float height = BlockQuarterColumn.isCube(block) ? .5F : 1F;
            GL11.glTranslatef(-width / 2, -height / 2, -.25F);
            renderer.setRenderBounds(0, 0, 0, width, height, .5);
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

    static void face(RenderBlocks renderer, Block block, int x, int y, int z, int side, IIcon icon) {
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

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return BlockVerticalSlab.renderId; }
}
