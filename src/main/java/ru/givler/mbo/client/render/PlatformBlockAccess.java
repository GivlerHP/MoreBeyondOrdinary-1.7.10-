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
    private final Map<Long,Integer> frozenLight=new HashMap<Long,Integer>();
    private int[] lightField;private int lightSizeX,lightSizeY,lightSizeZ;
    private final IBlockAccess world;private final int originX,originY,originZ;
    PlatformBlockAccess(List<PlatformBlock> source,IBlockAccess world,int originX,int originY,int originZ){
        this.world=world;this.originX=originX;this.originY=originY;this.originZ=originZ;
        for(PlatformBlock block:source)blocks.put(key(block.x,block.y,block.z),block);
    }
    PlatformBlockAccess(List<PlatformBlock> source,List<Integer> light,IBlockAccess world,int originX,int originY,int originZ){
        this(source,world,originX,originY,originZ);for(int i=0;i<source.size()&&i<light.size();i++){PlatformBlock block=source.get(i);frozenLight.put(key(block.x,block.y,block.z),light.get(i));}
    }
    PlatformBlockAccess(List<PlatformBlock> source,int[] light,int sizeX,int sizeY,int sizeZ,IBlockAccess world,int originX,int originY,int originZ){
        this(source,world,originX,originY,originZ);this.lightField=light;this.lightSizeX=sizeX;this.lightSizeY=sizeY;this.lightSizeZ=sizeZ;
    }
    private PlatformBlock at(int x,int y,int z){return blocks.get(key(x,y,z));}
    private static long key(int x,int y,int z){return ((long)(x&0x1fffff)<<42)|((long)(y&0x1fffff)<<21)|(long)(z&0x1fffff);}
    @Override public Block getBlock(int x,int y,int z){PlatformBlock b=at(x,y,z);return b==null?Blocks.air:b.block;}
    @Override public int getBlockMetadata(int x,int y,int z){PlatformBlock b=at(x,y,z);return b==null?0:b.meta;}
    @Override public TileEntity getTileEntity(int x,int y,int z){return null;}
    @Override public int getLightBrightnessForSkyBlocks(int x,int y,int z,int minimum){if(lightField!=null&&x>=-1&&x<=lightSizeX&&y>=-1&&y<=lightSizeY&&z>=-1&&z<=lightSizeZ){int sy=lightSizeY+2,sz=lightSizeZ+2,index=(x+1)*sy*sz+(y+1)*sz+(z+1);if(index>=0&&index<lightField.length)return lightField[index];}Integer saved=frozenLight.get(key(x,y,z));return saved==null?world.getLightBrightnessForSkyBlocks(originX+x,originY+y,originZ+z,minimum):saved.intValue();}
    @Override public int isBlockProvidingPowerTo(int x,int y,int z,int side){return 0;}
    @Override public boolean isAirBlock(int x,int y,int z){return getBlock(x,y,z)==Blocks.air;}
    @Override public BiomeGenBase getBiomeGenForCoords(int x,int z){return world.getBiomeGenForCoords(originX+x,originZ+z);}
    @Override public int getHeight(){return 256;}
    @Override public boolean extendedLevelsInChunkCache(){return false;}
    @Override public boolean isSideSolid(int x,int y,int z,ForgeDirection side,boolean fallback){Block b=getBlock(x,y,z);return b==Blocks.air?false:b.isSideSolid(this,x,y,z,side);}
}
