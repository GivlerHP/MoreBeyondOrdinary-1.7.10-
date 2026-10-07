package ru.givler.mbo.item.fauna;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public final class ItemTurtleEgg extends ItemBlock {
  public ItemTurtleEgg(Block block) {
    super(block);
  }

  @Override
  public void registerIcons(IIconRegister register) {
    itemIcon = register.registerIcon("mbo:fauna/turtle_egg");
  }

  @Override
  public IIcon getIconFromDamage(int damage) {
    return itemIcon;
  }

  @Override
  public boolean onItemUse(
      ItemStack stack,
      EntityPlayer player,
      World world,
      int x,
      int y,
      int z,
      int side,
      float hitX,
      float hitY,
      float hitZ) {
    if (!player.isSneaking() && stack.stackSize > 0 && world.getBlock(x, y, z) == field_150939_a) {
      int metadata = world.getBlockMetadata(x, y, z);
      if ((metadata & 3) < 3
          && world.canMineBlock(player, x, y, z)
          && player.canPlayerEdit(x, y, z, side, stack)) {
        if (!world.isRemote) {
          world.setBlockMetadataWithNotify(x, y, z, metadata + 1, 3);
          if (!player.capabilities.isCreativeMode) stack.stackSize--;
        }
        return true;
      }
    }
    return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
  }
}
