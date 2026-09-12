package ru.givler.mbo.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Random;

/** A metadata coral block which dies after spending a few seconds away from water. */
public class BlockCoral extends BlockMeta {
    private static final int MIN_DRY_TICKS = 60;
    private static final int EXTRA_DRY_TICKS = 40;

    private final Block deadCoral;

    public BlockCoral(String name, String[] textures, Block deadCoral) {
        super(Material.rock, name, textures);
        this.deadCoral = deadCoral;
        this.setHardness(1.5F);
        this.setResistance(6.0F);
        this.setHarvestLevel("pickaxe", 0);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        scheduleDryingIfNeeded(world, x, y, z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        scheduleDryingIfNeeded(world, x, y, z);
    }

    private void scheduleDryingIfNeeded(World world, int x, int y, int z) {
        if (!world.isRemote && !hasAdjacentWater(world, x, y, z)) {
            world.scheduleBlockUpdate(
                    x, y, z, this, MIN_DRY_TICKS + world.rand.nextInt(EXTRA_DRY_TICKS + 1));
        }
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (!world.isRemote && world.getBlock(x, y, z) == this
                && !hasAdjacentWater(world, x, y, z)) {
            world.setBlock(x, y, z, deadCoral, world.getBlockMetadata(x, y, z), 3);
        }
    }

    private boolean hasAdjacentWater(World world, int x, int y, int z) {
        return isWater(world, x - 1, y, z)
                || isWater(world, x + 1, y, z)
                || isWater(world, x, y - 1, z)
                || isWater(world, x, y + 1, z)
                || isWater(world, x, y, z - 1)
                || isWater(world, x, y, z + 1);
    }

    private boolean isWater(World world, int x, int y, int z) {
        return world.getBlock(x, y, z).getMaterial() == Material.water;
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return Item.getItemFromBlock(deadCoral);
    }

    @Override
    protected boolean canSilkHarvest() {
        return true;
    }

    @Override
    public boolean canSilkHarvest(
            World world, EntityPlayer player, int x, int y, int z, int metadata) {
        ItemStack held = player == null ? null : player.getCurrentEquippedItem();
        return held != null && held.getItem().getToolClasses(held).contains("pickaxe");
    }
}
