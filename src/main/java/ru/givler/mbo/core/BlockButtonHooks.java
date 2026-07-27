package ru.givler.mbo.core;

import net.minecraft.block.BlockButton;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public final class BlockButtonHooks {
    private BlockButtonHooks() { }

    public static int canPlaceOnSide(World world, int x, int y, int z, int side) {
        if (side == 1) return world.isSideSolid(x, y - 1, z, ForgeDirection.UP) ? 1 : 0;
        if (side == 0) return world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN) ? 1 : 0;
        return -1;
    }

    public static int canPlaceAt(World world, int x, int y, int z) {
        if (world.isSideSolid(x, y - 1, z, ForgeDirection.UP)
                || world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN)) return 1;
        return -1;
    }

    public static int placedMeta(World world, int x, int y, int z, int side) {
        if (side == 1 && world.isSideSolid(x, y - 1, z, ForgeDirection.UP)) return 5;
        if (side == 0 && world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN)) return 0;
        return -1;
    }

    public static boolean setBounds(BlockButton button, int meta) {
        int direction = meta & 7;
        if (direction != 0 && direction != 5) return false;
        boolean pressed = (meta & 8) != 0;
        float halfWidth = 0.1875F;
        float halfDepth = 0.125F;
        float height = pressed ? 0.0625F : 0.125F;
        if (direction == 5) {
            button.setBlockBounds(0.5F-halfWidth, 0, 0.5F-halfDepth,
                    0.5F+halfWidth, height, 0.5F+halfDepth);
        } else {
            button.setBlockBounds(0.5F-halfWidth, 1F-height, 0.5F-halfDepth,
                    0.5F+halfWidth, 1F, 0.5F+halfDepth);
        }
        return true;
    }

    public static boolean handleNeighbor(BlockButton button, World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        int direction = meta & 7;
        if (direction != 0 && direction != 5) return false;
        boolean supported = direction == 5
                ? world.isSideSolid(x, y - 1, z, ForgeDirection.UP)
                : world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN);
        if (!supported) {
            button.dropBlockAsItem(world, x, y, z, meta, 0);
            world.setBlockToAir(x, y, z);
        }
        return true;
    }

    public static boolean notifySupport(BlockButton button, World world, int x, int y, int z) {
        int direction = world.getBlockMetadata(x, y, z) & 7;
        if (direction == 5) {
            world.notifyBlocksOfNeighborChange(x, y - 1, z, button);
            return true;
        }
        if (direction == 0) {
            world.notifyBlocksOfNeighborChange(x, y + 1, z, button);
            return true;
        }
        return false;
    }

    public static int strongPower(BlockButton button, IBlockAccess world,
                                  int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);
        int direction = meta & 7;
        if (direction == 0 || direction == 5) return (meta & 8) != 0 ? 15 : 0;
        return -1;
    }
}
