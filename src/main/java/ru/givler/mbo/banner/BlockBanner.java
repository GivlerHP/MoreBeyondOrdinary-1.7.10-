package ru.givler.mbo.banner;

import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBanner extends BlockContainer {
    public BlockBanner() {
        super(Material.wood);
        setBlockName("Banner");
        setHardness(1.0F);
        setStepSound(soundTypeWood);
        setBlockBounds(.25F, 0, .25F, .75F, 1, .75F);
    }

    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityBanner(); }
    @Override public int getRenderType() { return -1; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World w, int x, int y, int z) { return null; }
    @Override public boolean getBlocksMovement(IBlockAccess w, int x, int y, int z) { return true; }

    @Override public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
        ArrayList<ItemStack> result = new ArrayList<ItemStack>();
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityBanner) result.add(((TileEntityBanner) tile).createStack());
        return result;
    }

    @Override public ItemStack getPickBlock(MovingObjectPosition hit, World world, int x, int y, int z, EntityPlayer player) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileEntityBanner ? ((TileEntityBanner) tile).createStack() : null;
    }

    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityBanner)) return;
        TileEntityBanner banner = (TileEntityBanner) tile;
        int meta = world.getBlockMetadata(x, y, z);
        boolean supported = banner.standing ? World.doesBlockHaveSolidTopSurface(world, x, y - 1, z)
                : meta == 2 ? world.getBlock(x, y, z + 1).getMaterial().isSolid()
                : meta == 3 ? world.getBlock(x, y, z - 1).getMaterial().isSolid()
                : meta == 4 ? world.getBlock(x + 1, y, z).getMaterial().isSolid()
                : world.getBlock(x - 1, y, z).getMaterial().isSolid();
        if (!supported) {
            for (ItemStack drop : getDrops(world, x, y, z, meta, 0)) dropBlockAsItem(world, x, y, z, drop);
            world.setBlockToAir(x, y, z);
        }
    }
}
