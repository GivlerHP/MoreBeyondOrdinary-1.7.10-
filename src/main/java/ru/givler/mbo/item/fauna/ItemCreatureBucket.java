package ru.givler.mbo.item.fauna;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Facing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import ru.givler.mbo.entity.fauna.IBucketableCreature;
import ru.givler.mbo.item.ItemBase;

public final class ItemCreatureBucket<T extends EntityLiving & IBucketableCreature>
    extends ItemBase {
  public interface CreatureFactory<T> {
    T create(World world);
  }

  private static final ItemBucket WATER = new ItemBucket(Blocks.flowing_water);
  private final CreatureFactory<T> factory;
  private final float spawnOffset;
  private final String releaseSound;

  public ItemCreatureBucket(
      String name,
      String texture,
      CreatureFactory<T> factory,
      float spawnOffset,
      String releaseSound) {
    super(name, texture, 1, false);
    this.factory = factory;
    this.spawnOffset = spawnOffset;
    this.releaseSound = releaseSound;
    setContainerItem(Items.bucket);
  }

  @Override
  public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
    MovingObjectPosition hit = getMovingObjectPositionFromPlayer(world, player, false);
    if (hit == null
        || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
        || world.provider.isHellWorld) return stack;
    if (!world.canMineBlock(player, hit.blockX, hit.blockY, hit.blockZ)) return stack;

    int x = hit.blockX + Facing.offsetsXForSide[hit.sideHit];
    int y = hit.blockY + Facing.offsetsYForSide[hit.sideHit];
    int z = hit.blockZ + Facing.offsetsZForSide[hit.sideHit];
    if (!world.canMineBlock(player, x, y, z)
        || !player.canPlayerEdit(x, y, z, hit.sideHit, stack)
        || world.isRemote) return stack;

    Block previous = world.getBlock(x, y, z);
    int metadata = world.getBlockMetadata(x, y, z);
    if (!WATER.tryPlaceContainedLiquid(world, x, y, z)) return stack;

    T creature = factory.create(world);
    creature.readBucketData(stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound());
    if (stack.hasDisplayName()) creature.setCustomNameTag(stack.getDisplayName());
    creature.setPosition(x + .5D, y + spawnOffset, z + .5D);
    if (!world.spawnEntityInWorld(creature)) {
      world.setBlock(x, y, z, previous, metadata, 3);
      return stack;
    }
    if (releaseSound != null)
      world.playSoundEffect(x + .5D, y + .5D, z + .5D, releaseSound, 1F, 1F);
    return player.capabilities.isCreativeMode ? stack : new ItemStack(Items.bucket);
  }
}
