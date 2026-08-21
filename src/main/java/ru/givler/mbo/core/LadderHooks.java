package ru.givler.mbo.core;

import net.minecraft.world.World;

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
        return -1;
    }

    public static boolean hasSpecialSupport(World world, int x, int y, int z) {
        return true;
    }
}
