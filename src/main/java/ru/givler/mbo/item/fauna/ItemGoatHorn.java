package ru.givler.mbo.item.fauna;

import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.givler.mbo.item.ItemBase;

/** Eight original instruments; cooldown belongs to the player, not the held stack. */
public final class ItemGoatHorn extends ItemBase {
  public ItemGoatHorn() {
    super("mbo.goat_horn", "fauna/goat_horn", 1, false);
    setHasSubtypes(true);
  }

  @Override
  public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
    if (!world.isRemote) {
      long now = world.getTotalWorldTime();
      if (!player.getEntityData().hasKey("MBOHornReady")
          || now >= player.getEntityData().getLong("MBOHornReady")) {
        player.getEntityData().setLong("MBOHornReady", now + 140);
        world.playSoundAtEntity(
            player,
            "mbo:item.goat_horn.sound." + Math.max(0, Math.min(7, stack.getItemDamage())),
            16F,
            1F);
      }
    }
    return stack;
  }

  @Override
  public String getUnlocalizedName(ItemStack stack) {
    return "item.mbo.goat_horn." + Math.max(0, Math.min(7, stack.getItemDamage()));
  }

  @Override
  public void getSubItems(Item item, CreativeTabs tab, List items) {
    for (int i = 0; i < 8; i++) items.add(new ItemStack(item, 1, i));
  }
}
