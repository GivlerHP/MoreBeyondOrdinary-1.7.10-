package ru.givler.mbo.item.fauna;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import ru.givler.mbo.item.ItemBase;
import ru.givler.mbo.registry.BlockRegistry;

public final class ItemPowderSnowBucket extends ItemBase {
  public ItemPowderSnowBucket() {
    super("mbo.powder_snow_bucket", "fauna/powder_snow_bucket", 1, false);
    setContainerItem(Items.bucket);
  }

  @Override
  public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
    MovingObjectPosition hit = getMovingObjectPositionFromPlayer(world, player, false);
    if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return stack;
    int x = hit.blockX, y = hit.blockY, z = hit.blockZ;
    if (!world.canMineBlock(player, x, y, z)) return stack;
    if (!world.getBlock(x, y, z).isReplaceable(world, x, y, z)) {
      x += Facing.offsetsXForSide[hit.sideHit];
      y += Facing.offsetsYForSide[hit.sideHit];
      z += Facing.offsetsZForSide[hit.sideHit];
    }
    if (!player.canPlayerEdit(x, y, z, hit.sideHit, stack)
        || !world.getBlock(x, y, z).isReplaceable(world, x, y, z)) return stack;
    if (!world.isRemote && world.setBlock(x, y, z, BlockRegistry.powderSnow, 0, 3)) {
      world.playSoundEffect(x + .5, y + .5, z + .5, "mbo:item.bucket.empty_powder_snow", 1, 1);
      if (!player.capabilities.isCreativeMode) return new ItemStack(Items.bucket);
    }
    return stack;
  }
}
