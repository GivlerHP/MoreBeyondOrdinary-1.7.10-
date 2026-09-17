package ru.givler.mbo.block.magic;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.tileentity.TileEntityMagicSnare;

/** A single-use physical snare. Magical sigils use their own entity system. */
public final class BlockMagicSnare extends BlockContainer {
  public BlockMagicSnare() {
    super(Material.plants);
    setBlockName("magicSnare");
    setBlockTextureName(MoreBeyondOrdinary.MODID + ":magic/snare");
    setHardness(0.0F);
    setStepSound(Block.soundTypeGrass);
    setBlockBounds(0, 0, 0, 1, 0.0625F, 1);
  }

  @Override
  public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
    if (world.isRemote || !(entity instanceof EntityLivingBase)) return;
    TileEntity tile = world.getTileEntity(x, y, z);
    if (!(tile instanceof TileEntityMagicSnare)) return;
    EntityLivingBase target = (EntityLivingBase) entity;
    EntityLivingBase caster = ((TileEntityMagicSnare) tile).getCaster();
    if (target == caster || (caster != null && caster.isOnSameTeam(target))) return;

    DamageSource source =
        caster == null ? DamageSource.magic : DamageSource.causeIndirectMagicDamage(caster, caster);
    target.attackEntityFrom(source, 6.0F);
    target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100, 2));
    world.func_147480_a(x, y, z, false);
  }

  @Override
  public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
    return null;
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
  public int getRenderType() {
    return 23;
  }

  @Override
  public Item getItemDropped(int meta, Random random, int fortune) {
    return null;
  }

  @Override
  public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
    if (!world.isSideSolid(x, y - 1, z, ForgeDirection.UP)) world.setBlockToAir(x, y, z);
  }

  @Override
  public TileEntity createNewTileEntity(World world, int metadata) {
    return new TileEntityMagicSnare();
  }
}
