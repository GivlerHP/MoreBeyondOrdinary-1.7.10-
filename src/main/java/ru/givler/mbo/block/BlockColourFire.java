package ru.givler.mbo.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.client.render.ColoredFireSprite;

/** Permanent coloured fire: it ignites entities but never spreads or consumes blocks. */
public final class BlockColourFire extends BlockFire {
  public static final int[] COLORS = {
    0xF9FFFE, 0xF9801D, 0xC74EBD, 0x3AB3DA,
    0xFED83D, 0x80C71F, 0xF38BAA, 0x474F52,
    0x9D9D97, 0x169C9C, 0x8932B8, 0x3C44AA,
    0x835432, 0x5E7C16, 0xB02E26, 0x1D1D21
  };
  private final IIcon[][] icons = new IIcon[16][2];
  private final boolean colored;
  private int renderType;
  private int renderingMetadata;

  public BlockColourFire(boolean colored) {
    this.colored = colored;
    setBlockName(colored ? "AdminFire" : "AdminFireVanilla");
    setLightLevel(1.0F);
    setTickRandomly(false);
    disableStats();
  }

  public void setRenderType(int value) { renderType = value; }

  @Override public int getRenderType() { return renderType; }
  @Override public boolean isOpaqueCube() { return false; }
  @Override public boolean renderAsNormalBlock() { return false; }
  @Override public int quantityDropped(Random random) { return 0; }
  @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World w, int x, int y, int z) {
    return null;
  }

  @Override
  public boolean canPlaceBlockAt(World world, int x, int y, int z) {
    return World.doesBlockHaveSolidTopSurface(world, x, y - 1, z);
  }

  @Override
  public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
    if (!canPlaceBlockAt(world, x, y, z)) world.setBlockToAir(x, y, z);
  }

  @Override public void onBlockAdded(World world, int x, int y, int z) {}
  @Override public void updateTick(World world, int x, int y, int z, Random random) {}

  @Override
  public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
    if (!entity.isImmuneToFire()) {
      entity.attackEntityFrom(DamageSource.inFire, 1.0F);
      entity.setFire(8);
      ru.givler.mbo.fire.ColoredBurning.onFireContact(entity,
          colored ? world.getBlockMetadata(x, y, z) & 15 : -1);
    }
  }

  @SideOnly(Side.CLIENT)
  public IIcon getColoredFireIcon(int color, int layer) {
    return icons[color & 15][layer & 1];
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerBlockIcons(IIconRegister register) {
    if (!colored) {
      for (int color = 0; color < 16; color++) {
        icons[color][0] = register.registerIcon("minecraft:fire_layer_0");
        icons[color][1] = register.registerIcon("minecraft:fire_layer_1");
      }
      blockIcon = icons[0][0];
      return;
    }
    TextureMap atlas = (TextureMap) register;
    for (int color = 0; color < 16; color++)
      for (int layer = 0; layer < 2; layer++) {
        String name = "mbo:admin_fire/generated_" + layer + "_" + color;
        ColoredFireSprite sprite = new ColoredFireSprite(name, layer, COLORS[color], color == 15);
        atlas.setTextureEntry(name, sprite);
        icons[color][layer] = sprite;
      }
    blockIcon = icons[0][0];
  }

  @SideOnly(Side.CLIENT)
  @Override
  public IIcon getFireIcon(int layer) {
    return icons[renderingMetadata & 15][layer & 1];
  }

  /**
   * BlockFire's implementation reads its own private icon array. We deliberately register our
   * own icons, so block particles (created when an entity is burning) must use that array too.
   */
  @SideOnly(Side.CLIENT)
  @Override
  public IIcon getIcon(int side, int metadata) {
    IIcon icon = icons[metadata & 15][0];
    return icon != null ? icon : blockIcon;
  }

  @SideOnly(Side.CLIENT)
  public void beginRender(IBlockAccess world, int x, int y, int z) {
    renderingMetadata = world.getBlockMetadata(x, y, z);
  }
}
