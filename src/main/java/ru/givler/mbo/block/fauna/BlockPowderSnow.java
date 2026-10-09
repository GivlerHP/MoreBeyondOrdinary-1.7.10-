package ru.givler.mbo.block.fauna;

import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockBase;
import ru.givler.mbo.entity.fauna.EntityRabbit;
import ru.givler.mbo.registry.BlockRegistry;

/** Sinking snow with entity-dependent support; ordinary snow is unchanged. */
public final class BlockPowderSnow extends BlockBase {
  public BlockPowderSnow() {
    super(Material.snow, "mbo.powder_snow", "fauna/powder_snow", false);
    setHarvestLevel(null, -1);
    setResistance(0);
    setLightOpacity(255);
    setHardness(.25F);
    setStepSound(soundTypeSnow);
  }

  @Override
  public boolean isOpaqueCube() {
    // Neighbour faces must stay visible when the camera sinks into this block.
    return false;
  }

  @Override
  public boolean renderAsNormalBlock() {
    return false;
  }

  /** Actual body contact, including the edges of the entity rather than just its centre. */
  public static boolean contains(Entity entity) {
    if (BlockRegistry.powderSnow == null) return false;
    AxisAlignedBB box = entity.boundingBox;
    int minX = MathHelper.floor_double(box.minX + .000001D);
    int minY = MathHelper.floor_double(box.minY + .000001D);
    int minZ = MathHelper.floor_double(box.minZ + .000001D);
    int maxX = MathHelper.floor_double(box.maxX - .000001D);
    int maxY = MathHelper.floor_double(box.maxY - .000001D);
    int maxZ = MathHelper.floor_double(box.maxZ - .000001D);
    for (int x = minX; x <= maxX; x++)
      for (int y = minY; y <= maxY; y++)
        for (int z = minZ; z <= maxZ; z++)
          if (entity.worldObj.blockExists(x, y, z)
              && entity.worldObj.getBlock(x, y, z) == BlockRegistry.powderSnow) return true;
    return false;
  }

  public static boolean canWalk(Entity entity) {
    if (entity instanceof EntityRabbit
        || entity instanceof EntitySilverfish
        || entity.getClass().getSimpleName().equals("EntityMBOFox")) return true;
    if (entity instanceof EntityLivingBase) {
      ItemStack boots = ((EntityLivingBase) entity).getEquipmentInSlot(1);
      return boots != null && boots.getItem() == Items.leather_boots;
    }
    return false;
  }

  @Override
  public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
    return null;
  }

  @Override
  public void addCollisionBoxesToList(
      World world, int x, int y, int z, AxisAlignedBB query, List boxes, Entity entity) {
    if (entity == null) return;
    AxisAlignedBB support = null;
    if (entity.fallDistance > 2.5F)
      support = AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + .9D, z + 1);
    else if (entity instanceof EntityFallingBlock
        || canWalk(entity) && !entity.isSneaking() && entity.boundingBox.minY >= y + .99D)
      support = AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1);
    if (support != null && query.intersectsWith(support)) boxes.add(support);
  }

  @Override
  public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
    if (entity.boundingBox.minY >= y + 1D) return;
    entity.motionX *= .9D;
    entity.motionZ *= .9D;
    if (entity.motionY < 0) entity.motionY = Math.max(-.05D, entity.motionY * .5D);
    entity.fallDistance = 0;
    if (entity.isBurning()) {
      if (!world.isRemote
          && (entity instanceof EntityPlayer
              || world.getGameRules().getGameRuleBooleanValue("mobGriefing")))
        world.setBlockToAir(x, y, z);
      entity.extinguish();
    }
  }

  @Override
  public void onFallenUpon(World world, int x, int y, int z, Entity entity, float distance) {
    entity.fallDistance = 0;
  }

  @Override
  public Item getItemDropped(int meta, Random random, int fortune) {
    return null;
  }

  @Override
  public int quantityDropped(Random random) {
    return 0;
  }

  @Override
  public boolean canSilkHarvest(World world, EntityPlayer player, int x, int y, int z, int meta) {
    return false;
  }
}
