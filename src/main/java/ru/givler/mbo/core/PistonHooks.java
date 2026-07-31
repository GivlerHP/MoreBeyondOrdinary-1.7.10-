package ru.givler.mbo.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockSlimeMBO;
import ru.givler.mbo.registry.BlockRegistry;

/**
 * Back-in-Slime style piston planner. Returning -1 lets the unmodified
 * 1.7.10 piston handle assemblies that do not contain an MBO slime block.
 */
public final class PistonHooks {
    private PistonHooks() {}

    public static int handleEvent(BlockPistonBase piston, World world, int x, int y, int z,
                                  int event, int side) {
        if (side < 0 || side > 5) return -1;
        boolean sticky = piston == Blocks.sticky_piston;
        int dx = Facing.offsetsXForSide[side];
        int dy = Facing.offsetsYForSide[side];
        int dz = Facing.offsetsZForSide[side];
        List<Move> moves = new ArrayList<Move>();

        if (event == 0) {
            if (!collect(world, x + dx, y + dy, z + dz,
                    Facing.oppositeSide[side], side, x, y, z,
                    moves, new HashSet<Long>()) || !containsSlime(moves)) return -1;
            if (moves.size() > 12) return 0;
            if (!move(world, moves, side)) return 0;
            world.setBlock(x + dx, y + dy, z + dz, Blocks.piston_extension,
                    side | (sticky ? 8 : 0), 4);
            world.setTileEntity(x + dx, y + dy, z + dz,
                    BlockPistonMoving.getTileEntity(Blocks.piston_head,
                            side | (sticky ? 8 : 0), side, true, false));
            world.setBlockMetadataWithNotify(x, y, z, side | 8, 3);
            world.playSoundEffect(x + .5D, y + .5D, z + .5D, "tile.piston.out",
                    .5F, world.rand.nextFloat() * .25F + .6F);
            return 1;
        }

        if ((event == 1 || event == -1) && sticky) {
            int tx = x + dx * 2, ty = y + dy * 2, tz = z + dz * 2;
            if (!collect(world, tx, ty, tz, Facing.oppositeSide[side],
                    Facing.oppositeSide[side],
                    x + dx, y + dy, z + dz, moves, new HashSet<Long>())
                    || !containsSlime(moves)) return -1;
            if (moves.size() > 12) return 0;

            TileEntity head = world.getTileEntity(x + dx, y + dy, z + dz);
            if (head instanceof TileEntityPiston) {
                ((TileEntityPiston) head).clearPistonTileEntity();
            }
            world.setBlock(x, y, z, Blocks.piston_extension, side, 3);
            world.setTileEntity(x, y, z,
                    BlockPistonMoving.getTileEntity(piston, side, side, false, true));
            world.setBlockToAir(x + dx, y + dy, z + dz);
            move(world, moves, Facing.oppositeSide[side]);
            world.playSoundEffect(x + .5D, y + .5D, z + .5D, "tile.piston.in",
                    .5F, world.rand.nextFloat() * .15F + .6F);
            return 1;
        }
        return -1;
    }

    private static boolean collect(World world, int x, int y, int z,
                                   int ignoreSide, int side,
                                   int pistonX, int pistonY, int pistonZ,
                                   List<Move> moves, Set<Long> visited) {
        if (y < 0 || y >= world.getHeight()) return false;
        Block block = world.getBlock(x, y, z);
        if (block.getMaterial() == Material.air) return true;
        if (x == pistonX && y == pistonY && z == pistonZ) return true;
        long key = key(x, y, z);
        if (!visited.add(key)) return true;
        if (!canMove(block, world, x, y, z, side)) return false;

        moves.add(new Move(x, y, z, block, world.getBlockMetadata(x, y, z)));
        if (moves.size() > 12) return false;

        if (isSlime(block)) {
            for (int attachedSide = 0; attachedSide < 6; attachedSide++) {
                if (attachedSide == side || attachedSide == ignoreSide) continue;
                if (!collect(world, x + Facing.offsetsXForSide[attachedSide],
                        y + Facing.offsetsYForSide[attachedSide],
                        z + Facing.offsetsZForSide[attachedSide],
                        Facing.oppositeSide[attachedSide], side,
                        pistonX, pistonY, pistonZ, moves, visited)) return false;
            }
        }

        if (block.getMobilityFlag() != 1) {
            return collect(world, x + Facing.offsetsXForSide[side],
                    y + Facing.offsetsYForSide[side],
                    z + Facing.offsetsZForSide[side], ignoreSide, side,
                    pistonX, pistonY, pistonZ, moves, visited);
        }
        return true;
    }

    private static boolean canMove(Block block, World world, int x, int y, int z, int side) {
        if (block == Blocks.obsidian || block.getBlockHardness(world, x, y, z) == -1.0F
                || block.getMobilityFlag() == 2 || block.hasTileEntity(world.getBlockMetadata(x, y, z))) {
            return false;
        }
        if (block == Blocks.piston || block == Blocks.sticky_piston) {
            return !BlockPistonBase.isExtended(world.getBlockMetadata(x, y, z));
        }
        return true;
    }

    private static boolean move(World world, List<Move> moves, final int side) {
        final int dx = Facing.offsetsXForSide[side];
        final int dy = Facing.offsetsYForSide[side];
        final int dz = Facing.offsetsZForSide[side];
        Collections.sort(moves, new Comparator<Move>() {
            public int compare(Move a, Move b) {
                int pa = a.x * dx + a.y * dy + a.z * dz;
                int pb = b.x * dx + b.y * dy + b.z * dz;
                return pb - pa;
            }
        });

        Set<Long> destinations = new HashSet<Long>();
        for (Move move : moves) destinations.add(key(move.x + dx, move.y + dy, move.z + dz));
        for (Move move : moves) {
            if (!destinations.contains(key(move.x, move.y, move.z))) {
                world.setBlockToAir(move.x, move.y, move.z);
            }
        }
        for (Move move : moves) {
            int nx = move.x + dx, ny = move.y + dy, nz = move.z + dz;
            if (move.block.getMobilityFlag() == 1) {
                float chance = move.block instanceof BlockSnow ? -1.0F : 1.0F;
                move.block.dropBlockAsItemWithChance(world, move.x, move.y, move.z,
                        move.meta, chance, 0);
                world.setBlockToAir(nx, ny, nz);
            } else {
                world.setBlock(nx, ny, nz, Blocks.piston_extension, move.meta, 4);
                world.setTileEntity(nx, ny, nz,
                        BlockPistonMoving.getTileEntity(move.block, move.meta, side, true, false));
            }
        }
        return true;
    }

    private static boolean containsSlime(List<Move> moves) {
        for (Move move : moves) if (isSlime(move.block)) return true;
        return false;
    }

    private static boolean isSlime(Block block) {
        return block == BlockRegistry.SlimeBlock
                || (block instanceof BlockSlimeMBO
                && !(block == BlockRegistry.BouncyBrownMushroomBlock
                || block == BlockRegistry.BouncyRedMushroomBlock));
    }

    private static long key(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38)
                | ((long) (z & 0x3FFFFFF) << 12) | (long) (y & 0xFFF);
    }

    private static final class Move {
        final int x, y, z, meta;
        final Block block;
        Move(int x, int y, int z, Block block, int meta) {
            this.x = x; this.y = y; this.z = z; this.block = block; this.meta = meta;
        }
    }
}
