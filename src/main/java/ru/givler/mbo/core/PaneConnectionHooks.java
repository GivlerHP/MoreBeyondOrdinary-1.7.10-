package ru.givler.mbo.core;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

/** Connection and isolated-post geometry used by the vanilla BlockPane patch. */
public final class PaneConnectionHooks {
    private static final float MIN = 7.0F / 16.0F;
    private static final float MAX = 9.0F / 16.0F;

    private PaneConnectionHooks() { }

    public static boolean canConnect(BlockPane pane, IBlockAccess world, int x, int y, int z,
                                     ForgeDirection direction) {
        Block neighbor = world.getBlock(x, y, z);
        int metadata = world.getBlockMetadata(x, y, z);

        if (neighbor instanceof BlockStairs) {
            return direction != stairFacing(metadata);
        }
        if (neighbor instanceof BlockTrapDoor) {
            return (metadata & 4) != 0 && direction == openTrapdoorSide(metadata);
        }
        return pane.canPaneConnectToBlock(neighbor)
                || world.isSideSolid(x, y, z, direction.getOpposite(), false);
    }

    public static boolean setIsolatedBounds(BlockPane pane, IBlockAccess world, int x, int y, int z) {
        if (!isIsolated(pane, world, x, y, z)) return false;
        pane.setBlockBounds(MIN, 0.0F, MIN, MAX, 1.0F, MAX);
        return true;
    }

    @SuppressWarnings("unchecked")
    public static boolean addIsolatedCollision(BlockPane pane, World world, int x, int y, int z,
                                                AxisAlignedBB mask, List boxes) {
        if (!isIsolated(pane, world, x, y, z)) return false;
        AxisAlignedBB post = AxisAlignedBB.getBoundingBox(
                x + MIN, y, z + MIN, x + MAX, y + 1.0D, z + MAX);
        if (mask == null || post.intersectsWith(mask)) boxes.add(post);
        return true;
    }

    public static boolean isIsolated(BlockPane pane, IBlockAccess world, int x, int y, int z) {
        return !pane.canPaneConnectTo(world, x, y, z - 1, ForgeDirection.NORTH)
                && !pane.canPaneConnectTo(world, x, y, z + 1, ForgeDirection.SOUTH)
                && !pane.canPaneConnectTo(world, x - 1, y, z, ForgeDirection.WEST)
                && !pane.canPaneConnectTo(world, x + 1, y, z, ForgeDirection.EAST);
    }

    private static ForgeDirection stairFacing(int metadata) {
        switch (metadata & 3) {
            case 0: return ForgeDirection.EAST;
            case 1: return ForgeDirection.WEST;
            case 2: return ForgeDirection.SOUTH;
            default: return ForgeDirection.NORTH;
        }
    }

    private static ForgeDirection openTrapdoorSide(int metadata) {
        switch (metadata & 3) {
            case 0: return ForgeDirection.NORTH;
            case 1: return ForgeDirection.SOUTH;
            case 2: return ForgeDirection.WEST;
            default: return ForgeDirection.EAST;
        }
    }
}
