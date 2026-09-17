package ru.givler.mbo.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

/** A temporary magic enchantment which can only be applied by MBO magic. */
public class EnchantmentTimed extends Enchantment {
  public EnchantmentTimed(int id, String name) { super(id, 0, null); setName(name); }
  @Override public int getMaxLevel() { return 4; }
  @Override public boolean canApply(ItemStack stack) { return false; }
  @Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return false; }
  @Override public boolean isAllowedOnBooks() { return false; }
}
