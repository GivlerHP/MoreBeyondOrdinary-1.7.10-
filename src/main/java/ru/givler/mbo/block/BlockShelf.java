package ru.givler.mbo.block;

import ru.givler.mbo.tileentity.TileEntityShelf;
import ru.givler.mbo.handler.BlockInteractionAccess;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.client.renderer.texture.IIconRegister;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.givler.mbo.registry.CreativeTabRegistry;

public final class BlockShelf extends BlockContainer {
    public static int renderId;
    public final String wood;
    public BlockShelf(String wood) {
        super(Material.wood);this.wood=wood;
        setBlockName(wood+"_shelf");setHardness(2);setResistance(3);
        setStepSound(soundTypeWood);setCreativeTab(CreativeTabRegistry.tabMBOblocks);
    }
    @Override public TileEntity createNewTileEntity(World world,int meta) { return new TileEntityShelf(); }
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @Override public int getRenderType(){return renderId;}
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister icons) {
        blockIcon=icons.registerIcon("mbo:shelf/"+wood+"_shelf");
    }
    @Override public IIcon getIcon(int side,int meta){return blockIcon;}
    @Override public void setBlockBoundsForItemRender(){setBlockBounds(0,0,11/16F,1,1,1);}
    @Override public void setBlockBoundsBasedOnState(IBlockAccess w,int x,int y,int z) {
        int f=w.getBlockMetadata(x,y,z)&7;
        setBlockBounds(f==4?11/16F:0,0,f==2?11/16F:0,f==5?5/16F:1,1,f==3?5/16F:1);
    }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z){
        setBlockBoundsBasedOnState(w,x,y,z);return super.getCollisionBoundingBoxFromPool(w,x,y,z);
    }
    @Override public void onBlockPlacedBy(World w,int x,int y,int z,EntityLivingBase entity,ItemStack stack) {
        int[] directions={2,5,3,4};int facing=directions[MathHelper.floor_double(entity.rotationYaw*4/360+.5)&3];
        w.setBlockMetadataWithNotify(x,y,z,facing|(w.isBlockIndirectlyGettingPowered(x,y,z)?8:0),3);
    }
    @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block neighbor) {
        if(w.isRemote)return;
        int meta=w.getBlockMetadata(x,y,z),next=(meta&7)|(w.isBlockIndirectlyGettingPowered(x,y,z)?8:0);
        if(meta!=next)w.setBlockMetadataWithNotify(x,y,z,next,3);
    }
    private static boolean matches(IBlockAccess w,int x,int y,int z,int facing) {
        return (!(w instanceof World)||((World)w).blockExists(x,y,z))&&w.getBlock(x,y,z) instanceof BlockShelf
            &&w.getBlockMetadata(x,y,z)==(facing|8);
    }
    /** Returns offsets for one deterministic group, independent of the clicked member. */
    public static int[] group(IBlockAccess w,int x,int y,int z,int facing) {
        int dx=facing==2?-1:facing==3?1:0,dz=facing==4?1:facing==5?-1:0;
        int left=0;
        while(left>-64&&matches(w,x+(left-1)*dx,y,z+(left-1)*dz,facing))left--;
        if(left==-64)return new int[]{0,1};
        int start=left+((-left)/3)*3,end=start;
        while(end<start+3&&matches(w,x+end*dx,y,z+end*dz,facing))end++;
        return new int[]{start,end};
    }
    public static int connection(IBlockAccess w,int x,int y,int z) {
        int meta=w.getBlockMetadata(x,y,z);if((meta&8)==0)return 0;
        int[] g=group(w,x,y,z,meta&7);
        return g[1]-g[0]<=1?0:g[0]==0?1:g[1]==1?3:2;
    }
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz) {
        int meta=w.getBlockMetadata(x,y,z),facing=meta&7;
        if(side!=facing)return false;
        if(w.isRemote)return true;
        if(!(w.getTileEntity(x,y,z) instanceof TileEntityShelf))return true;
        List<TileEntityShelf> shelves=new ArrayList<TileEntityShelf>();
        if((meta&8)!=0) {
            // Left-to-right as seen by the player. Bounded scan prevents chunk loading and huge chains.
            int dx=facing==2?-1:facing==3?1:0,dz=facing==4?1:facing==5?-1:0;
            int[] group=group(w,x,y,z,facing);
            for(int n=group[0];n<group[1];n++) {
                TileEntity tile=w.getTileEntity(x+n*dx,y,z+n*dz);
                if(!(tile instanceof TileEntityShelf))return true;
                shelves.add((TileEntityShelf)tile);
            }
        } else shelves.add((TileEntityShelf)w.getTileEntity(x,y,z));
        for(TileEntityShelf shelf:shelves)if(!BlockInteractionAccess.allowed(player,w,shelf.xCoord,shelf.yCoord,shelf.zCoord,facing))return true;
        if((meta&8)!=0) {
            int slot=9-shelves.size()*3;
            for(TileEntityShelf shelf:shelves)for(int i=0;i<3;i++,slot++)player.inventory.setInventorySlotContents(slot,shelf.swap(i,player.inventory.getStackInSlot(slot)));
        } else {
            float hit=facing==2?1-hx:facing==3?hx:facing==4?hz:1-hz;
            int slot=Math.min(2,Math.max(0,(int)(hit*3)));
            player.inventory.setInventorySlotContents(player.inventory.currentItem,shelves.get(0).swap(slot,player.getHeldItem()));
        }
        player.inventory.markDirty();player.inventoryContainer.detectAndSendChanges();
        w.playSoundEffect(x+.5,y+.5,z+.5,"random.click",.5F,1);
        return true;
    }
    @Override public void breakBlock(World w,int x,int y,int z,Block block,int meta) {
        TileEntity tile=w.getTileEntity(x,y,z);
        if(!w.isRemote&&tile instanceof TileEntityShelf) for(int i=0;i<3;i++) {
            ItemStack stack=((TileEntityShelf)tile).get(i);
            if(stack!=null)w.spawnEntityInWorld(new EntityItem(w,x+.5,y+.5,z+.5,stack.copy()));
        }
        super.breakBlock(w,x,y,z,block,meta);
    }
    @Override public boolean hasComparatorInputOverride() { return true; }
    @Override public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        // 1.7.10 comparator input uses horizontal Direction indices rather than Facing indices.
        if (net.minecraft.util.Direction.directionToFacing[side & 3] != net.minecraft.util.Facing.oppositeSide[world.getBlockMetadata(x, y, z) & 7]) return 0;
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileEntityShelf ? ((TileEntityShelf) tile).comparatorSignal() : 0;
    }
    @Override public int damageDropped(int meta){return 0;}
}
