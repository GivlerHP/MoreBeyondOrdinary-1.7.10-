package ru.givler.mbo.client.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.movingplatform.PlatformBlock;

/** A small immutable world view used to render connected blocks on a moving platform. */
final class PlatformBlockAccess implements IBlockAccess {
    private final Map<Long,PlatformBlock> blocks=new HashMap<Long,PlatformBlock>();
    private final IBlockAccess world;private final int originX,originY,originZ;
    PlatformBlockAccess(List<PlatformBlock> source,IBlockAccess world,int originX,int originY,int originZ){
        this.world=world;this.originX=originX;this.originY=originY;this.originZ=originZ;
        for(PlatformBlock block:source)blocks.put(key(block.x,block.y,block.z),block);
    }
    private PlatformBlock at(int x,int y,int z){return blocks.get(key(x,y,z));}
    private static long key(int x,int y,int z){return ((long)(x&0x1fffff)<<42)|((long)(y&0x1fffff)<<21)|(long)(z&0x1fffff);}
    @Override public Block getBlock(int x,int y,int z){PlatformBlock b=at(x,y,z);return b==null?Blocks.air:b.block;}
    @Override public int getBlockMetadata(int x,int y,int z){PlatformBlock b=at(x,y,z);return b==null?0:b.meta;}
    @Override public TileEntity getTileEntity(int x,int y,int z){return null;}
    @Override public int getLightBrightnessForSkyBlocks(int x,int y,int z,int minimum){return world.getLightBrightnessForSkyBlocks(originX+x,originY+y,originZ+z,minimum);}
    @Override public int isBlockProvidingPowerTo(int x,int y,int z,int side){return 0;}
    @Override public boolean isAirBlock(int x,int y,int z){return getBlock(x,y,z)==Blocks.air;}
    @Override public BiomeGenBase getBiomeGenForCoords(int x,int z){return world.getBiomeGenForCoords(originX+x,originZ+z);}
    @Override public int getHeight(){return 256;}
    @Override public boolean extendedLevelsInChunkCache(){return false;}
    @Override public boolean isSideSolid(int x,int y,int z,ForgeDirection side,boolean fallback){Block b=getBlock(x,y,z);return b==Blocks.air?false:b.isSideSolid(this,x,y,z,side);}
}
