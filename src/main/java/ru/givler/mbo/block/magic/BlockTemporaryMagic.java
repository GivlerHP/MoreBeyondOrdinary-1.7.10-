package ru.givler.mbo.block.magic;

import java.util.Random;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import ru.givler.mbo.tileentity.TileEntityTemporaryMagicBlock;

/** One timed block implementation shared by spell-created webs and frost walls. */
public final class BlockTemporaryMagic extends BlockContainer {
  public enum Kind {
    COBWEB,
    FROST,
    LIGHT,
    SPECTRAL
  }

  private final Kind kind;

  public BlockTemporaryMagic(Kind kind) {
    super(
        kind == Kind.COBWEB
            ? Material.web
            : kind == Kind.FROST
                ? Material.ice
                : kind == Kind.SPECTRAL ? Material.glass : Material.circuits);
    this.kind = kind;
    setBlockName(
        kind == Kind.COBWEB
            ? "temporary_cobweb"
            : kind == Kind.FROST
                ? "temporary_frost"
                : kind == Kind.SPECTRAL ? "temporary_spectral" : "temporary_light");
    setBlockTextureName(
        kind == Kind.COBWEB
            ? "minecraft:web"
            : kind == Kind.SPECTRAL ? "mbo:magic/spectral_block" : "minecraft:ice");
    setHardness(kind == Kind.COBWEB ? 4.0F : kind == Kind.FROST ? 0.5F : 0.0F);
    setLightOpacity(kind == Kind.COBWEB ? 1 : kind == Kind.FROST ? 3 : 0);
    if (kind == Kind.LIGHT) {
      setLightLevel(1.0F);
      setBlockBounds(0, 0, 0, 0, 0, 0);
    }
    if (kind == Kind.SPECTRAL) {
      setLightLevel(0.7F);
      setBlockUnbreakable();
      setResistance(6000000.0F);
    }
    if (kind == Kind.FROST) slipperiness = 0.98F;
  }

  @Override
  public TileEntity createNewTileEntity(World world, int metadata) {
    return new TileEntityTemporaryMagicBlock();
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  @Override
  public int getRenderType() {
    return kind == Kind.COBWEB ? 1 : kind == Kind.LIGHT ? -1 : 0;
  }

  @Override
  public boolean renderAsNormalBlock() {
    return kind != Kind.LIGHT;
  }

  @Override
  public int getRenderBlockPass() {
    return kind == Kind.FROST || kind == Kind.SPECTRAL ? 1 : 0;
  }

  @Override
  public int quantityDropped(Random random) {
    return 0;
  }

  @Override
  public boolean shouldSideBeRendered(
      net.minecraft.world.IBlockAccess world, int x, int y, int z, int side) {
    return kind != Kind.SPECTRAL || world.getBlock(x, y, z) != this;
  }

  @Override
  public int getMixedBrightnessForBlock(
      net.minecraft.world.IBlockAccess world, int x, int y, int z) {
    return kind == Kind.SPECTRAL ? 15728880 : super.getMixedBrightnessForBlock(world, x, y, z);
  }

  @Override
  public void randomDisplayTick(World world, int x, int y, int z, Random random) {
    if (kind != Kind.SPECTRAL) return;
    ru.givler.mbo.MoreBeyondOrdinary.proxy.spawnParticle(
        ru.givler.mbo.particles.EnumParticleType.DUST,
        world,
        x + random.nextDouble(),
        y + random.nextDouble(),
        z + random.nextDouble(),
        0,
        0.01D,
        0,
        new ru.givler.mbo.particles.ParticleSettings(25, 0.7F, 0.8F, 1.0F, 0.6F, false));
  }

  @Override
  public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
    return kind == Kind.COBWEB || kind == Kind.LIGHT
        ? null
        : super.getCollisionBoundingBoxFromPool(world, x, y, z);
  }

  @Override
  public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
    if (kind == Kind.COBWEB) entity.setInWeb();
  }
}
