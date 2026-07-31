package ru.givler.mbo.core;

import java.util.List;
import java.util.ArrayDeque;
import java.util.HashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;

public final class CauldronHooks {
    private static int renderType = 24;
    private CauldronHooks() { }

    public static int getRenderType() { return renderType; }
    public static void setRenderType(int id) { renderType = id; }

    public static int handleActivation(BlockCauldron block, World world, int x, int y, int z, EntityPlayer player) {
        ItemStack held=player.getCurrentEquippedItem();
        int meta=world.getBlockMetadata(x,y,z);
        boolean lava=(meta&4)!=0;
        int level=meta&3;

        if(held!=null && (held.getItem()==Items.lava_bucket || held.getItem()==Items.water_bucket)) {
            boolean fillingLava=held.getItem()==Items.lava_bucket;
            if(level>0 && lava!=fillingLava) return 1;
            if(!canUseFluid(world,x,y,z,fillingLava)) return 1;
            if(!world.isRemote) {
                setConnectedLevel(world,x,y,z,(fillingLava?4:0)|3);
                if(!player.capabilities.isCreativeMode)
                    player.inventory.setInventorySlotContents(player.inventory.currentItem,new ItemStack(Items.bucket));
            }
            return 1;
        }
        if(held!=null && held.getItem()==Items.bucket && level==3) {
            if(!world.isRemote) {
                ItemStack filled=new ItemStack(lava?Items.lava_bucket:Items.water_bucket);
                setConnectedLevel(world,x,y,z,0);
                if(!player.capabilities.isCreativeMode)
                    player.inventory.setInventorySlotContents(player.inventory.currentItem,filled);
            }
            return 1;
        }
        if(lava) return 1;
        normalizeConnected(world,x,y,z);
        return -1;
    }

    public static boolean handleCollision(World world,int x,int y,int z,Entity entity) {
        int meta=getConnectedMetadata(world,x,y,z);
        if((meta&4)==0 || (meta&3)==0) return false;
        double surface=y+(6+(meta&3)*3)/16.0;
        if(entity.boundingBox.minY<=surface) {
            entity.setFire(4);
            entity.attackEntityFrom(DamageSource.lava,4.0F);
        }
        return true;
    }

    public static int getLevel(int metadata) { return metadata&3; }

    public static void normalizeConnected(World world, int x, int y, int z) {
        int level = findMaximumLevel(world, x, y, z);
        setConnectedLevel(world, x, y, z, level);
    }

    public static void syncConnected(World world, int x, int y, int z, int level) {
        setConnectedLevel(world, x, y, z, level);
    }

    private static int findMaximumLevel(World world, int x, int y, int z) {
        return getConnectedMetadata(world,x,y,z);
    }

    private static void setConnectedLevel(World world, int x, int y, int z, int level) {
        for (int[] pos : connected(world,x,y,z))
            if (world.getBlockMetadata(pos[0],pos[1],pos[2]) != level)
                world.setBlockMetadataWithNotify(pos[0],pos[1],pos[2],level,2);
    }

    private static boolean canUseFluid(World world,int x,int y,int z,boolean lava) {
        for(int[] pos:connected(world,x,y,z)) {
            int meta=world.getBlockMetadata(pos[0],pos[1],pos[2]);
            if((meta&3)>0 && ((meta&4)!=0)!=lava) return false;
        }
        return true;
    }

    public static int getConnectedMetadata(IBlockAccess world,int x,int y,int z) {
        int maximum=0;
        for(int[] pos:connected(world,x,y,z))
            maximum=Math.max(maximum,world.getBlockMetadata(pos[0],pos[1],pos[2]));
        return maximum;
    }

    public static boolean canConnect(IBlockAccess world,int x,int y,int z,int nx,int ny,int nz) {
        if(!(world.getBlock(nx,ny,nz) instanceof BlockCauldron)) return false;
        int rootKind=fluidKind(world.getBlockMetadata(x,y,z));
        int otherKind=fluidKind(world.getBlockMetadata(nx,ny,nz));
        if(rootKind!=0 && otherKind!=0) return rootKind==otherKind;
        int desired=rootKind!=0?rootKind:resolveFluidKind(world,x,y,z);
        if(desired==0) {
            int otherDesired=otherKind!=0?otherKind:resolveFluidKind(world,nx,ny,nz);
            return otherDesired==0;
        }
        if(otherKind!=0) return otherKind==desired;
        return (adjacentFluidMask(world,nx,ny,nz)&oppositeMask(desired))==0;
    }

    private static List<int[]> connected(IBlockAccess world,int x,int y,int z) {
        List<int[]> result = new java.util.ArrayList<int[]>();
        ArrayDeque<int[]> queue = new ArrayDeque<int[]>();
        HashSet<String> visited = new HashSet<String>();
        int desired=fluidKind(world.getBlockMetadata(x,y,z));
        if(desired==0) desired=resolveFluidKind(world,x,y,z);
        queue.add(new int[]{x,y,z});
        while(!queue.isEmpty() && result.size()<256) {
            int[] pos=queue.removeFirst();
            String key=pos[0]+":"+pos[1]+":"+pos[2];
            if(!visited.add(key) || !(world.getBlock(pos[0],pos[1],pos[2]) instanceof BlockCauldron)) continue;
            int kind=fluidKind(world.getBlockMetadata(pos[0],pos[1],pos[2]));
            if(desired==0) {
                if(kind!=0) continue;
            } else {
                if(kind!=0 && kind!=desired) continue;
                if(kind==0 && (adjacentFluidMask(world,pos[0],pos[1],pos[2])&oppositeMask(desired))!=0)
                    continue;
            }
            result.add(pos);
            queue.add(new int[]{pos[0]-1,pos[1],pos[2]});
            queue.add(new int[]{pos[0]+1,pos[1],pos[2]});
            queue.add(new int[]{pos[0],pos[1],pos[2]-1});
            queue.add(new int[]{pos[0],pos[1],pos[2]+1});
        }
        return result;
    }

    private static int resolveFluidKind(IBlockAccess world,int x,int y,int z) {
        int mask=0;
        ArrayDeque<int[]> queue=new ArrayDeque<int[]>();
        HashSet<String> visited=new HashSet<String>();
        queue.add(new int[]{x,y,z});
        while(!queue.isEmpty() && visited.size()<256) {
            int[] pos=queue.removeFirst();
            String key=pos[0]+":"+pos[1]+":"+pos[2];
            if(!visited.add(key) || !(world.getBlock(pos[0],pos[1],pos[2]) instanceof BlockCauldron)) continue;
            int kind=fluidKind(world.getBlockMetadata(pos[0],pos[1],pos[2]));
            if(kind==1) mask|=1;
            else if(kind==2) mask|=2;
            if(mask==3) return 0;
            queue.add(new int[]{pos[0]-1,pos[1],pos[2]});
            queue.add(new int[]{pos[0]+1,pos[1],pos[2]});
            queue.add(new int[]{pos[0],pos[1],pos[2]-1});
            queue.add(new int[]{pos[0],pos[1],pos[2]+1});
        }
        return mask==1?1:mask==2?2:0;
    }

    private static int adjacentFluidMask(IBlockAccess world,int x,int y,int z) {
        int mask=0;
        int[][] offsets={{-1,0},{1,0},{0,-1},{0,1}};
        for(int[] offset:offsets) {
            if(!(world.getBlock(x+offset[0],y,z+offset[1]) instanceof BlockCauldron)) continue;
            int kind=fluidKind(world.getBlockMetadata(x+offset[0],y,z+offset[1]));
            if(kind==1) mask|=1;
            else if(kind==2) mask|=2;
        }
        return mask;
    }

    private static int fluidKind(int meta) {
        if((meta&3)==0) return 0;
        return (meta&4)!=0?2:1;
    }

    private static int oppositeMask(int kind) { return kind==1?2:1; }

    @SuppressWarnings("unchecked")
    public static void addCollisionBoxes(BlockCauldron block, World world, int x, int y, int z,
                                         AxisAlignedBB mask, List list, Entity entity) {
        add(mask, list, x, y, z, 0, 0, 0, 1, 0.3125, 1);
        if (!canConnect(world,x,y,z,x-1,y,z))
            add(mask, list, x, y, z, 0, 0, 0, 0.125, 1, 1);
        if (!canConnect(world,x,y,z,x+1,y,z))
            add(mask, list, x, y, z, 0.875, 0, 0, 1, 1, 1);
        if (!canConnect(world,x,y,z,x,y,z-1))
            add(mask, list, x, y, z, 0, 0, 0, 1, 1, 0.125);
        if (!canConnect(world,x,y,z,x,y,z+1))
            add(mask, list, x, y, z, 0, 0, 0.875, 1, 1, 1);
    }

    private static void add(AxisAlignedBB mask, List list, int x, int y, int z,
                            double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x+minX,y+minY,z+minZ,x+maxX,y+maxY,z+maxZ);
        if (mask == null || box.intersectsWith(mask)) list.add(box);
    }
}
