package ru.givler.mbo.block.fauna;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockBase;
import ru.givler.mbo.registry.ItemRegistry;

/** Seagrass retains source-water material, so liquid physics and fish see its water. */
public final class BlockSeagrass extends BlockBase {
  private int renderType = 1;

  public BlockSeagrass() {
    super(Material.water, "mbo.seagrass", "fauna/seagrass", false);
    setHarvestLevel(null, -1);
    setResistance(0);
    setHardness(0);
    setLightOpacity(3);
  }

  public void setRenderType(int type) {
    renderType = type;
  }

  @Override
  public int getRenderType() {
    return renderType;
  }

  @Override
  public int getRenderBlockPass() {
    return 1;
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  @Override
  public boolean renderAsNormalBlock() {
    return false;
  }

  @Override
  public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
    return null;
  }

  @Override
  public boolean canPlaceBlockAt(World world, int x, int y, int z) {
    return world.getBlock(x, y, z).getMaterial() == Material.water
        && world.getBlockMetadata(x, y, z) == 0
        && world.getBlock(x, y - 1, z).getMaterial().isSolid();
  }

  @Override
  public void onNeighborBlockChange(World world, int x, int y, int z, Block changed) {
    if (!world.getBlock(x, y - 1, z).getMaterial().isSolid())
      world.setBlock(x, y, z, Blocks.water, 0, 3);
  }

  @Override
  public boolean removedByPlayer(
      World world, EntityPlayer player, int x, int y, int z, boolean harvest) {
    return world.setBlock(x, y, z, Blocks.water, 0, 3);
  }

  @Override
  public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int metadata) {
    ItemStack tool = player.getCurrentEquippedItem();
    if (!world.isRemote && tool != null && tool.getItem() == Items.shears)
      dropBlockAsItem(world, x, y, z, new ItemStack(ItemRegistry.seagrass));
  }

  @Override
  public Item getItemDropped(int metadata, Random random, int fortune) {
    return null;
  }
}
