package ru.givler.mbo.core;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.client.state.ClientFenceConnections;
import ru.givler.mbo.data.world.FenceConnectionData;

/** Shared connection rules injected into vanilla fences and walls. */
public final class FenceConnectionHooks {
    private FenceConnectionHooks() { }

    public static void setOrigin(int x, int y, int z) {
        FenceConnectionContext.set(x, y, z);
    }

    public static boolean allowsConnection(Block owner, IBlockAccess world, int x, int y, int z) {
        int[] origin = FenceConnectionContext.get();
        if (origin == null || world.getBlock(origin[0], origin[1], origin[2]) != owner) return true;
        int dx = x - origin[0], dz = z - origin[2];
        if (y != origin[1] || Math.abs(dx) + Math.abs(dz) != 1) return true;
        int side = dx < 0 ? 4 : dx > 0 ? 5 : dz < 0 ? 2 : 3;
        return !isDisabled(world, origin[0], origin[1], origin[2], side)
                && !isDisabled(world, x, y, z,
                    FenceConnectionData.opposite(side));
    }

    private static boolean isDisabled(IBlockAccess access, int x, int y, int z, int side) {
        byte mask;
        if (access instanceof World && !((World) access).isRemote)
            mask = FenceConnectionData.get((World) access).mask(x, y, z);
        else mask = ClientFenceConnections.get(x, y, z);
        return (mask & FenceConnectionData.bit(side)) != 0;
    }

    public static boolean isCompatibleNeighbor(IBlockAccess world, int x, int y, int z) {
        Block neighbor = world.getBlock(x, y, z);
        return neighbor instanceof BlockFence
                || neighbor instanceof BlockFenceGate
                || neighbor instanceof BlockWall;
    }
}
