package ru.givler.mbo.core;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.world.IBlockAccess;

/** Shared connection rules injected into vanilla fences and walls. */
public final class FenceConnectionHooks {
    private FenceConnectionHooks() { }

    public static boolean isCompatibleNeighbor(IBlockAccess world, int x, int y, int z) {
        Block neighbor = world.getBlock(x, y, z);
        return neighbor instanceof BlockFence
                || neighbor instanceof BlockFenceGate
                || neighbor instanceof BlockWall;
    }
}
