package ru.givler.mbo.block.model;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.tileentity.TileEntityModelCollision;

public class BlockModelCollision extends Block implements ITileEntityProvider {
    public BlockModelCollision() {
        super(Material.rock);
        setBlockName("ModelCollisionPart");
        setHardness(0.0F);
        setResistance(6000000.0F);
    }

    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public int getRenderType() { return -1; }
    @Override public boolean hasTileEntity(int meta) { return true; }
    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityModelCollision(); }
    @Override public int quantityDropped(java.util.Random random) { return 0; }

    private TileEntityModelCollision tile(IBlockAccess world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileEntityModelCollision ? (TileEntityModelCollision) tile : null;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        TileEntityModelCollision tile = tile(world, x, y, z);
        float[] b = tile == null ? new float[]{0, 0, 0, 1, 1, 1} : tile.getBounds();
        setBlockBounds(b[0], b[1], b[2], b[3], b[4], b[5]);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        TileEntityModelCollision tile = tile(world, x, y, z);
        BlockModels owner = tile == null ? null : tile.getOwnerBlock();
        if (owner == null || !owner.isModelCollisionEnabled()) return null;
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        TileEntityModelCollision tile = tile(world, x, y, z);
        if (tile != null && tile.getOwnerBlock() != null) {
            world.func_147480_a(tile.getOwnerX(), tile.getOwnerY(), tile.getOwnerZ(), true);
            return true;
        }
        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    @Override
    public ArrayList<net.minecraft.item.ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        return new ArrayList<net.minecraft.item.ItemStack>();
    }
}
