package ru.givler.mbo.item.fauna;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

/** Like a lily pad, the placement ray must include the surface of water. */
public final class ItemFrogspawn extends ItemBlock {
  public ItemFrogspawn(Block block) {
    super(block);
  }

  @Override
  public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
    MovingObjectPosition hit = getMovingObjectPositionFromPlayer(world, player, true);
    if (hit == null
        || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
        || stack.stackSize <= 0) return stack;
    int x = hit.blockX, y = hit.blockY + 1, z = hit.blockZ;
    if (!world.canMineBlock(player, x, hit.blockY, z)
        || !world.canMineBlock(player, x, y, z)
        || !player.canPlayerEdit(x, y, z, 1, stack)
        || !world.isAirBlock(x, y, z)
        || !field_150939_a.canPlaceBlockAt(world, x, y, z)
        || world.isRemote) return stack;
    BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(world, x, y, z);
    if (!world.setBlock(x, y, z, field_150939_a, 0, 3)) return stack;
    if (ForgeEventFactory.onPlayerBlockPlace(player, snapshot, ForgeDirection.UP).isCanceled()) {
      snapshot.restore(true, false);
      return stack;
    }
    if (!player.capabilities.isCreativeMode) stack.stackSize--;
    return stack;
  }
}
