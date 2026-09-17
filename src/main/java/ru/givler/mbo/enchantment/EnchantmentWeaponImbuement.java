package ru.givler.mbo.enchantment;

import net.minecraft.enchantment.EnchantmentDamage;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.item.ItemStack;

/** Melee imbuement whose damage bonus is applied by vanilla combat code. */
public final class EnchantmentWeaponImbuement extends EnchantmentDamage {
  public EnchantmentWeaponImbuement(int id) { super(id, 0, 0); type = null; setName("mbo.imbuement"); }
  @Override public int getMaxLevel() { return 4; }
  @Override public float func_152376_a(int level, EnumCreatureAttribute creature) { return level * 1.25F; }
  @Override public boolean canApply(ItemStack stack) { return false; }
  @Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return false; }
  @Override public boolean isAllowedOnBooks() { return false; }
}
