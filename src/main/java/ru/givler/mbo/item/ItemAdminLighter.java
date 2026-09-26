package ru.givler.mbo.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.editor.BuilderAccess;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.registry.CreativeTabRegistry;

public final class ItemAdminLighter extends Item {
  public ItemAdminLighter() {
    setUnlocalizedName("AdminLighter");
    setTextureName("minecraft:flint_and_steel");
    setCreativeTab(CreativeTabRegistry.tabMBOitems);
    setMaxStackSize(1);
    setHasSubtypes(true);
  }

  @Override
  public boolean onItemUse(
      ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
      float hitX, float hitY, float hitZ) {
    if (player.isSneaking()) {
      if (BuilderAccess.canUseTool(player, this) && world.isRemote)
        MoreBeyondOrdinary.proxy.openAdminFireColorGui(stack);
      return true;
    }
    if (!BuilderAccess.canUseTool(player, this)) return false;
    x += Facing.offsetsXForSide[side];
    y += Facing.offsetsYForSide[side];
    z += Facing.offsetsZForSide[side];
    if (!player.canPlayerEdit(x, y, z, side, stack)
        || !world.isAirBlock(x, y, z)
        || !BlockRegistry.ColourFire.canPlaceBlockAt(world, x, y, z)) return false;
    if (!world.isRemote) {
      int setting = stack.getItemDamage();
      world.setBlock(
          x, y, z,
          setting == 0 ? BlockRegistry.ColourFireVanilla : BlockRegistry.ColourFire,
          setting == 0 ? 0 : (setting - 1) & 15,
          3);
      world.playSoundEffect(x + .5D, y + .5D, z + .5D, "fire.ignite", 1F,
          itemRand.nextFloat() * .4F + .8F);
    }
    return true;
  }

  @Override
  public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
    if (player.isSneaking() && BuilderAccess.canUseTool(player, this) && world.isRemote)
      MoreBeyondOrdinary.proxy.openAdminFireColorGui(stack);
    return stack;
  }
}
