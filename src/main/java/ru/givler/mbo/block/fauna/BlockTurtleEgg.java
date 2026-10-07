package ru.givler.mbo.block.fauna;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockBase;
import ru.givler.mbo.entity.fauna.EntityMBOTurtle;

/** Bits 0..1 hold count minus one; bits 2..3 hold the hatch stage. */
public final class BlockTurtleEgg extends BlockBase {
  private IIcon[] icons;
  private int renderType;

  public void setRenderType(int value) {
    renderType = value;
  }

  @Override
  public int getRenderType() {
    return renderType;
  }

  public BlockTurtleEgg() {
    super(Material.dragonEgg, "mbo.turtle_egg", "fauna/turtle_egg", false);
    setHarvestLevel(null, -1);
    setResistance(0);
    setHardness(.5F);
    setTickRandomly(true);
    setLightOpacity(0);
  }

  public static int count(int metadata) {
    return (metadata & 3) + 1;
  }

  public static int stage(int metadata) {
    return Math.min(2, (metadata >> 2) & 3);
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
  public void setBlockBoundsForItemRender() {
    setBlockBounds(.1875F, 0, .1875F, .75F, .4375F, .75F);
  }

  @Override
  public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
    if (count(world.getBlockMetadata(x, y, z)) == 1) setBlockBoundsForItemRender();
    else setBlockBounds(.0625F, 0, .0625F, .9375F, .4375F, .9375F);
  }

  @Override
  public void registerBlockIcons(IIconRegister register) {
    icons =
        new IIcon[] {
          register.registerIcon("mbo:fauna/turtle_egg"),
          register.registerIcon("mbo:fauna/turtle_egg_slightly_cracked"),
          register.registerIcon("mbo:fauna/turtle_egg_very_cracked")
        };
  }

  @Override
  public IIcon getIcon(int side, int metadata) {
    return icons[stage(metadata)];
  }

  @Override
  public Item getItemDropped(int metadata, Random random, int fortune) {
    return null;
  }

  @Override
  public int damageDropped(int metadata) {
    return 0;
  }

  @Override
  protected boolean canSilkHarvest() {
    return true;
  }

  @Override
  public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
    if (!entity.isSneaking()) trample(world, x, y, z, entity, 100);
  }

  @Override
  public void onFallenUpon(World world, int x, int y, int z, Entity entity, float distance) {
    if (!(entity instanceof EntityZombie)) trample(world, x, y, z, entity, 3);
    super.onFallenUpon(world, x, y, z, entity, distance);
  }

  private void trample(World world, int x, int y, int z, Entity entity, int chance) {
    if (world.isRemote
        || entity instanceof EntityMBOTurtle
        || entity instanceof EntityBat
        || !(entity instanceof EntityLivingBase)) return;
    if (entity instanceof EntityPlayer) {
      if (!world.canMineBlock((EntityPlayer) entity, x, y, z)) return;
    } else if (!world.getGameRules().getGameRuleBooleanValue("mobGriefing")) return;
    if (world.rand.nextInt(chance) == 0) decrease(world, x, y, z);
  }

  private boolean decrease(World world, int x, int y, int z) {
    int metadata = world.getBlockMetadata(x, y, z);
    world.playSoundEffect(
        x + .5,
        y + .5,
        z + .5,
        "mbo:block.turtle_egg.break",
        .7F,
        .9F + world.rand.nextFloat() * .2F);
    return count(metadata) == 1
        ? world.setBlockToAir(x, y, z)
        : world.setBlockMetadataWithNotify(x, y, z, metadata - 1, 3);
  }

  @Override
  public boolean removedByPlayer(
      World world, EntityPlayer player, int x, int y, int z, boolean harvest) {
    return decrease(world, x, y, z);
  }

  @Override
  public void updateTick(World world, int x, int y, int z, Random random) {
    if (world.isRemote || world.getBlock(x, y - 1, z) != Blocks.sand) return;
    float time = (world.getWorldTime() % 24000L) / 24000F;
    if (!(time > .65F && time < .69F) && random.nextInt(500) != 0) return;
    int metadata = world.getBlockMetadata(x, y, z), hatch = stage(metadata);
    if (hatch < 2) {
      world.playSoundEffect(
          x + .5,
          y + .5,
          z + .5,
          "mbo:block.turtle_egg.crack",
          .7F,
          .9F + random.nextFloat() * .2F);
      world.setBlockMetadataWithNotify(x, y, z, (metadata & 3) | ((hatch + 1) << 2), 3);
    } else {
      int spawned = 0;
      for (int i = 0; i < count(metadata); i++) {
        EntityMBOTurtle turtle = new EntityMBOTurtle(world);
        turtle.setGrowingAge(-24000);
        turtle.setHome(x, y, z);
        turtle.setLocationAndAngles(x + .3 + i * .2, y, z + .3, 0, 0);
        if (world.spawnEntityInWorld(turtle)) spawned++;
      }
      if (spawned == 0) return; // Cancelled spawn must not silently consume the clutch.
      if (spawned == count(metadata)) world.setBlockToAir(x, y, z);
      else world.setBlockMetadataWithNotify(x, y, z, 8 + count(metadata) - spawned - 1, 3);
      world.playSoundEffect(x + .5, y + .5, z + .5, "mbo:block.turtle_egg.hatch", .7F, 1F);
    }
  }
}
