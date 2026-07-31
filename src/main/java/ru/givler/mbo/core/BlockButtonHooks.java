package ru.givler.mbo.core;

import net.minecraft.block.BlockButton;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
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

    public static void placedBy(World world, int x, int y, int z, EntityLivingBase placer) {
        int meta = world.getBlockMetadata(x, y, z);
        int direction = meta & 7;
        if (direction != 0 && direction != 5) return;

        int facing = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        int rotatedDirection;
        if (direction == 5) {
            rotatedDirection = (facing & 1) == 0 ? 5 : 6;
        } else {
            rotatedDirection = (facing & 1) == 0 ? 0 : 7;
        }
        world.setBlockMetadataWithNotify(x, y, z, (meta & 8) | rotatedDirection, 2);
    }

    public static boolean setBounds(BlockButton button, int meta) {
        int direction = meta & 7;
        if (direction != 0 && direction != 5 && direction != 6 && direction != 7) return false;
        boolean pressed = (meta & 8) != 0;
        float halfWidth = 0.1875F;
        float halfDepth = 0.125F;
        float height = pressed ? 0.0625F : 0.125F;
        boolean ceiling = direction == 0 || direction == 7;
        float minY = ceiling ? 1F - height : 0F;
        float maxY = ceiling ? 1F : height;
        if (direction == 0 || direction == 5) {
            button.setBlockBounds(0.5F-halfWidth, minY, 0.5F-halfDepth,
                    0.5F+halfWidth, maxY, 0.5F+halfDepth);
        } else {
            button.setBlockBounds(0.5F-halfDepth, minY, 0.5F-halfWidth,
                    0.5F+halfDepth, maxY, 0.5F+halfWidth);
        }
        return true;
    }

    public static boolean handleNeighbor(BlockButton button, World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        int direction = meta & 7;
        if (direction != 0 && direction != 5 && direction != 6 && direction != 7) return false;
        boolean ceiling = direction == 0 || direction == 7;
        boolean supported = ceiling
                ? world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN)
                : world.isSideSolid(x, y - 1, z, ForgeDirection.UP);
        if (!supported) {
            button.dropBlockAsItem(world, x, y, z, meta, 0);
            world.setBlockToAir(x, y, z);
        }
        return true;
    }

    public static boolean notifySupport(BlockButton button, World world, int x, int y, int z) {
        int direction = world.getBlockMetadata(x, y, z) & 7;
        if (direction == 5 || direction == 6) {
            world.notifyBlocksOfNeighborChange(x, y - 1, z, button);
            return true;
        }
        if (direction == 0 || direction == 7) {
            world.notifyBlocksOfNeighborChange(x, y + 1, z, button);
            return true;
        }
        return false;
    }

    public static int strongPower(BlockButton button, IBlockAccess world,
                                  int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);
        int direction = meta & 7;
        if (direction == 0 || direction == 5 || direction == 6 || direction == 7) {
            return (meta & 8) != 0 ? 15 : 0;
        }
        return -1;
    }
}
