package ru.givler.mbo.core;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.util.Facing;
import ru.givler.mbo.block.BlockObserver;
import ru.givler.mbo.registry.BlockRegistry;

/** Gives observers the location of a changed block, which neighbor callbacks omit in 1.7.10. */
public final class ObserverHooks {
    private ObserverHooks() {}

    public static void onBlockChanged(World world, int x, int y, int z) {
        if (world.isRemote || BlockRegistry.Observer == null) return;
        for (int side = 0; side < 6; side++) {
            int nx = x + Facing.offsetsXForSide[side];
            int ny = y + Facing.offsetsYForSide[side];
            int nz = z + Facing.offsetsZForSide[side];
            if (ny < 0 || ny >= world.getActualHeight() || !world.blockExists(nx, ny, nz)) continue;
            Block neighbor = world.getBlock(nx, ny, nz);
            if (neighbor == BlockRegistry.Observer)
                ((BlockObserver) neighbor).observedChange(world, nx, ny, nz, x, y, z);
        }
    }

    public static void onMetadataChanged(boolean changed, World world, int x, int y, int z) {
        if (changed) onBlockChanged(world, x, y, z);
    }
}
