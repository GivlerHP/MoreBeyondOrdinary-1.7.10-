package ru.givler.mbo.integration.carpentersblocks.client.render;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

/** Loaded only after Loader confirms that Carpenter's Blocks is present. */
public final class CarpenterOpeningBridge {
    private CarpenterOpeningBridge() {}

    public static boolean isAny(Block block){
        return isDoor(block)||isHatch(block)||isGate(block);
    }
    public static boolean isDoor(Block block){return block==com.carpentersblocks.util.registry.BlockRegistry.blockCarpentersDoor;}
    public static boolean isHatch(Block block){return block==com.carpentersblocks.util.registry.BlockRegistry.blockCarpentersHatch;}
    public static boolean isGate(Block block){return block==com.carpentersblocks.util.registry.BlockRegistry.blockCarpentersGate;}

    /** @return {baseY, synthetic metadata}, or null. */
    public static int[] door(IBlockAccess access,int x,int y,int z){
        com.carpentersblocks.tileentity.TEBase te=(com.carpentersblocks.tileentity.TEBase)access.getTileEntity(x,y,z);
        if(te==null)return null;
        int baseY=com.carpentersblocks.data.Hinge.getPiece(te)==1?y-1:y;
        te=(com.carpentersblocks.tileentity.TEBase)access.getTileEntity(x,baseY,z);
        if(te==null)return null;
        int meta=com.carpentersblocks.data.Hinge.getFacing(te)&3;
        if(com.carpentersblocks.data.Hinge.getState(te)==1)meta|=4;
        if(com.carpentersblocks.data.Hinge.getHinge(te)==1)meta|=16;
        return new int[]{baseY,meta};
    }

    public static Integer hatch(IBlockAccess access,int x,int y,int z){
        com.carpentersblocks.tileentity.TEBase te=(com.carpentersblocks.tileentity.TEBase)access.getTileEntity(x,y,z);
        if(te==null)return null;
        int meta=com.carpentersblocks.data.Hatch.getDir(te)&3;
        if(com.carpentersblocks.data.Hatch.getState(te)==1)meta|=4;
        if(com.carpentersblocks.data.Hatch.getPos(te)==1)meta|=8;
        return meta;
    }

    public static Integer gate(IBlockAccess access,int x,int y,int z){
        com.carpentersblocks.tileentity.TEBase te=(com.carpentersblocks.tileentity.TEBase)access.getTileEntity(x,y,z);
        if(te==null)return null;
        int axis=com.carpentersblocks.data.Gate.getFacing(te);
        int sign=com.carpentersblocks.data.Gate.getDirOpen(te);
        int meta=axis==0?(sign==1?2:0):(sign==1?1:3);
        if(com.carpentersblocks.data.Gate.getState(te)==1)meta|=4;
        return meta;
    }
}
