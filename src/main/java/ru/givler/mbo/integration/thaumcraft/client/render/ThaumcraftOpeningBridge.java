package ru.givler.mbo.integration.thaumcraft.client.render;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

/** Loaded only after Loader confirms that Thaumcraft is present. */
public final class ThaumcraftOpeningBridge {
    private ThaumcraftOpeningBridge() {}

    public static boolean isArcaneDoor(Block block){
        return block==thaumcraft.common.config.ConfigBlocks.blockArcaneDoor;
    }

    public static int fullDoorMetadata(Block block,IBlockAccess access,int x,int y,int z){
        return ((thaumcraft.common.blocks.BlockArcaneDoor)block).getFullMetadata(access,x,y,z);
    }
}
