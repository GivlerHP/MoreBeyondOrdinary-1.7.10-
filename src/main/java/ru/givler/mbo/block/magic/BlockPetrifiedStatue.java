package ru.givler.mbo.block.magic;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.tileentity.TileEntityPetrifiedStatue;

/** Breakable stone shell which restores the captured creature when any section is broken. */
public final class BlockPetrifiedStatue extends BlockContainer {
  private boolean removingSections;
  private final boolean ice;

  public BlockPetrifiedStatue() {
    this(false);
  }

  public BlockPetrifiedStatue(boolean ice) {
    super(ice ? Material.ice : Material.rock);
    this.ice = ice;
    setBlockName(ice ? "frozen_statue" : "petrified_statue");
    setBlockTextureName(ice ? "minecraft:ice" : "minecraft:stone");
    setHardness(ice ? .5F : 1.5F);
    setResistance(10.0F);
    if (ice) {
      slipperiness = .98F;
      setLightOpacity(3);
    }
  }

  public boolean isIce() {
    return ice;
  }

  @Override
  public TileEntity createNewTileEntity(World world, int metadata) {
    return new TileEntityPetrifiedStatue();
  }

  @Override
  public boolean renderAsNormalBlock() {
    return false;
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  @Override
  public boolean canRenderInPass(int pass) {
    return ice && pass == 1;
  }

  @Override
  public int getRenderBlockPass() {
    return ice ? 1 : 0;
  }

  @Override
  public int quantityDropped(Random random) {
    return 0;
  }

  @Override
  public void setBlockBoundsBasedOnState(IBlockAccess access, int x, int y, int z) {
    TileEntity tile = access.getTileEntity(x, y, z);
    if (!(tile instanceof TileEntityPetrifiedStatue)) return;
    TileEntityPetrifiedStatue statue = (TileEntityPetrifiedStatue) tile;
    EntityLiving creature = statue.creature();
    if (creature == null) return;
    float half = Math.min(0.5F, creature.width / 2.0F);
    float height =
        statue.section() == statue.sections()
            ? Math.min(1.0F, creature.height - statue.sections() + 1.0F)
            : 1.0F;
    setBlockBounds(0.5F - half, 0, 0.5F - half, 0.5F + half, Math.max(0.1F, height), 0.5F + half);
  }

  @Override
  public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
    setBlockBoundsBasedOnState(world, x, y, z);
    return super.getCollisionBoundingBoxFromPool(world, x, y, z);
  }

  @Override
  public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
    TileEntity tile = world.getTileEntity(x, y, z);
    if (!world.isRemote && !removingSections && tile instanceof TileEntityPetrifiedStatue) {
      TileEntityPetrifiedStatue statue = (TileEntityPetrifiedStatue) tile;
      int bottomY = y - statue.section() + 1;
      EntityLiving creature = statue.creature();
      removingSections = true;
      for (int part = 0; part < statue.sections(); part++) {
        int partY = bottomY + part;
        if (partY != y && world.getBlock(x, partY, z) == this) {
          world.setBlockToAir(x, partY, z);
        }
      }
      removingSections = false;
      if (creature != null) {
        creature.isDead = false;
        creature.setPosition(x + 0.5D, bottomY, z + 0.5D);
        world.spawnEntityInWorld(creature);
      }
    }
    super.breakBlock(world, x, y, z, block, metadata);
  }
}
