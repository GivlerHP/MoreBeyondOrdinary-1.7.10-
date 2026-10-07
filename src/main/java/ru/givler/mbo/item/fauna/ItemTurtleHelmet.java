package ru.givler.mbo.item.fauna;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.givler.mbo.item.ItemCustomArmor;
import ru.givler.mbo.registry.ItemRegistry;

public final class ItemTurtleHelmet extends ItemCustomArmor {
  private static final ArmorMaterial SHELL =
      createArmorMaterial("MBOTurtleShell", 25, new int[] {2, 6, 5, 2}, 9);

  public ItemTurtleHelmet() {
    super("mbo.turtle_helmet", "turtle_helmet", "turtle_shell", SHELL, 0, 0);
  }

  @Override
  public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
    if (!world.isRemote && !player.isInsideOfMaterial(Material.water))
      player.addPotionEffect(new PotionEffect(Potion.waterBreathing.id, 200, 0, true));
  }

  @Override
  public boolean getIsRepairable(ItemStack armor, ItemStack ingredient) {
    return ingredient != null && ingredient.getItem() == ItemRegistry.turtleScute;
  }
}
