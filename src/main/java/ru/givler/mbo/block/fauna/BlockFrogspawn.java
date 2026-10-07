package ru.givler.mbo.block.fauna;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockBase;
import ru.givler.mbo.entity.fauna.EntityMBOTadpole;

public final class BlockFrogspawn extends BlockBase {
  public BlockFrogspawn() {
    super(Material.plants, "mbo.frogspawn", "fauna/frogspawn", false);
    setHarvestLevel(null, -1);
    setHardness(0);
    setResistance(0);
    setBlockBounds(0, 0, 0, 1, 1.5F / 16F, 1);
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
  public Item getItemDropped(int metadata, Random random, int fortune) {
    return null;
  }

  @Override
  public boolean canPlaceBlockAt(World world, int x, int y, int z) {
    return world.getBlock(x, y - 1, z).getMaterial() == Material.water
        && world.getBlock(x, y, z).getMaterial() != Material.water;
  }

  @Override
  public void onBlockAdded(World world, int x, int y, int z) {
    if (!world.isRemote) world.scheduleBlockUpdate(x, y, z, this, 3600 + world.rand.nextInt(8400));
  }

  @Override
  public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
    if (!canPlaceBlockAt(world, x, y, z)) world.setBlockToAir(x, y, z);
  }

  @Override
  public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
    if (!world.isRemote && entity instanceof EntityFallingBlock) world.setBlockToAir(x, y, z);
  }

  @Override
  public void updateTick(World world, int x, int y, int z, Random random) {
    if (world.isRemote) return;
    if (!canPlaceBlockAt(world, x, y, z)) {
      world.setBlockToAir(x, y, z);
      return;
    }
    int remaining = world.getBlockMetadata(x, y, z);
    int count = remaining == 0 ? 2 + random.nextInt(4) : remaining, spawned = 0;
    for (int i = 0; i < count; i++) {
      EntityMBOTadpole tadpole = new EntityMBOTadpole(world);
      tadpole.setLocationAndAngles(
          x + Math.max(.2, Math.min(.8, random.nextDouble())),
          y - .5,
          z + Math.max(.2, Math.min(.8, random.nextDouble())),
          random.nextInt(360),
          0);
      if (world.spawnEntityInWorld(tadpole)) spawned++;
    }
    if (spawned < count) {
      world.setBlockMetadataWithNotify(x, y, z, count - spawned, 3);
      world.scheduleBlockUpdate(x, y, z, this, 200);
      return;
    }
    world.setBlockToAir(x, y, z);
    world.playSoundEffect(x + .5, y, z + .5, "mbo:block.frogspawn.hatch", 1, 1);
  }
}
