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
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockBase;
import ru.givler.mbo.entity.EntityRabbit;

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
