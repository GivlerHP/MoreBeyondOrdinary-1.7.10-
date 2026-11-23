package ru.givler.mbo.mirror;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/** Coordinates and packed block state for the visible part of a streamed mirror scene. */
public final class SilverMirrorBlocks {
    public static final int RADIUS = 14;
    private static final int WIDTH = RADIUS * 2 + 1;
    public static final int HEIGHT = 22;
    public static final int SIZE = WIDTH * WIDTH * HEIGHT;

    private SilverMirrorBlocks() {}

    public static int minY(int exitY) { return Math.max(0,exitY-10); }
    public static int maxY(int exitY) { return Math.min(255,exitY+11); }

    static int index(int dx,int dy,int dz) {
        return ((dx+RADIUS)*WIDTH+dz+RADIUS)*HEIGHT+dy;
    }

    static int state(Chunk chunk,int x,int y,int z,boolean sky) {
        int light=chunk.getSavedLightValue(EnumSkyBlock.Block,x,y,z);
        int skyLight=sky ? chunk.getSavedLightValue(EnumSkyBlock.Sky,x,y,z) : 0;
        return Block.getIdFromBlock(chunk.getBlock(x,y,z)) | chunk.getBlockMetadata(x,y,z)<<12
            | light<<16 | skyLight<<20;
    }

    public static void apply(World world,int exitX,int exitY,int exitZ,int index,int state) {
        if (index<0 || index>=SIZE) return;
        int y=minY(exitY)+index%HEIGHT;
        if (y>maxY(exitY)) return;
        int x=exitX-RADIUS+(index/HEIGHT)/WIDTH;
        int z=exitZ-RADIUS+(index/HEIGHT)%WIDTH;
        Block block=Block.getBlockById(state&4095);
        if (block==null) block=Blocks.air;
        Chunk chunk=world.getChunkFromChunkCoords(x>>4,z>>4);
        boolean sky=!world.provider.hasNoSky;
        ExtendedBlockStorage[] sections=chunk.getBlockStorageArray();
        ExtendedBlockStorage section=sections[y>>4];
        if (section==null) {
            if (block==Blocks.air && (state>>>16&255)==(sky?240:0)) return;
            section=sections[y>>4]=new ExtendedBlockStorage(y&~15,sky);
        }
        section.func_150818_a(x&15,y&15,z&15,block);
        section.setExtBlockMetadata(x&15,y&15,z&15,state>>>12&15);
        section.setExtBlocklightValue(x&15,y&15,z&15,state>>>16&15);
        if (sky) section.setExtSkylightValue(x&15,y&15,z&15,state>>>20&15);
    }
}
