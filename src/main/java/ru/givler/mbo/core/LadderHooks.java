package ru.givler.mbo.core;

import net.minecraft.world.World;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

public final class LadderHooks {
    private static int renderType = 8;

    private LadderHooks() {}

    public static void setRenderType(int id) { renderType = id; }
    public static int getRenderType() { return renderType; }

    public static boolean canPlaceOnSpecial(World world, int x, int y, int z) {
        return true;
    }

    public static int specialPlacementMetadata(World world, int x, int y, int z, int side) {
        if (side == 2) return 2;
        if (side == 3) return 3;
        if (side == 4) return 4;
        if (side == 5) return 5;
        return 0;
    }

    public static void placedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        if (world.getBlockMetadata(x, y, z) != 0) return;
        world.setBlockMetadataWithNotify(x, y, z, floorFacing(placer.rotationYaw), 2);
    }

    public static int floorFacing(float yaw) {
        int direction = MathHelper.floor_double(yaw * 4D / 360D + 0.5D) & 3;
        return new int[] {2, 5, 3, 4}[direction];
    }

    public static boolean hasSpecialSupport(World world, int x, int y, int z) {
        return true;
    }
}
