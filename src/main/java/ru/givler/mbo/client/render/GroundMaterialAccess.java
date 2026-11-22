package ru.givler.mbo.client.render;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.block.BlockGroundCover;
import ru.givler.mbo.block.BlockVerticalSlab;

/** Material rules see source metadata, which is independent of slab direction and connections. */
final class GroundMaterialAccess implements IBlockAccess {
    private final IBlockAccess world;

    GroundMaterialAccess(IBlockAccess world) {
        this.world = world;
    }

    @Override public Block getBlock(int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block instanceof BlockGroundCover) return ((BlockGroundCover) block).source;
        return block instanceof BlockVerticalSlab ? ((BlockVerticalSlab) block).source : block;
    }

    @Override public int getBlockMetadata(int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block instanceof BlockGroundCover) return ((BlockGroundCover) block).getSourceMetadata();
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
