package ru.givler.mbo.block.special;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDaylightDetector;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public final class BlockInvertedDaylightDetector extends BlockDaylightDetector {
  private IIcon topIcon;
  private IIcon sideIcon;

  public BlockInvertedDaylightDetector() {
    setBlockName("daylightDetector");
    setHardness(0.2F);
    setStepSound(Block.soundTypeWood);
    setCreativeTab(null);
  }

  @Override
  public void func_149957_e(World world, int x, int y, int z) {
    if (world.provider.hasNoSky) return;
    int oldPower = world.getBlockMetadata(x, y, z);
    int daylight = world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) - world.skylightSubtracted;
    float angle = world.getCelestialAngleRadians(1.0F);
    angle = angle < (float) Math.PI
        ? angle + (0.0F - angle) * 0.2F
        : angle + (((float) Math.PI * 2.0F) - angle) * 0.2F;
    daylight = MathHelper.clamp_int(Math.round(daylight * MathHelper.cos(angle)), 0, 15);
    int power = 15 - daylight;
    if (oldPower != power) world.setBlockMetadataWithNotify(x, y, z, power, 3);
  }

  @Override
  public boolean onBlockActivated(
      World world, int x, int y, int z, net.minecraft.entity.player.EntityPlayer player,
      int side, float hitX, float hitY, float hitZ) {
    int power = world.getBlockMetadata(x, y, z);
    if (!world.isRemote) {
      world.setBlock(x, y, z, Blocks.daylight_detector, power, 3);
      ((BlockDaylightDetector) Blocks.daylight_detector).func_149957_e(world, x, y, z);
    }
    return true;
  }

  @Override
  public Item getItemDropped(int metadata, Random random, int fortune) {
    return Item.getItemFromBlock(Blocks.daylight_detector);
  }

  @Override
  public Item getItem(World world, int x, int y, int z) {
    return Item.getItemFromBlock(Blocks.daylight_detector);
  }

  @Override
  public void registerBlockIcons(IIconRegister register) {
    topIcon = register.registerIcon("mbo:daylightdetector/daylight_detector_inverted_top");
    sideIcon = register.registerIcon("mbo:daylightdetector/daylight_detector_side");
  }

  @Override
  public IIcon getIcon(int side, int metadata) {
    return side == 1 ? topIcon : sideIcon;
  }
}
