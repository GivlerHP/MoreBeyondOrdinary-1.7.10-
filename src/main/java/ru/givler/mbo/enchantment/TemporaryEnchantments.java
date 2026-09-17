package ru.givler.mbo.enchantment;

import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import ru.givler.mbo.registry.EnchantmentRegistry;

/** Item-level contract used by weapon-enchanting spells. */
public final class TemporaryEnchantments {
  private static final String EXPIRATIONS = "mboMagicEnchantments";
  private TemporaryEnchantments() {}

  public static boolean apply(ItemStack stack, Enchantment enchantment, int level,
      int durationTicks, World world) {
    if (stack == null || enchantment == null || durationTicks <= 0 || world == null) return false;
    if (EnchantmentHelper.getEnchantmentLevel(enchantment.effectId, stack) > 0) return false;
    stack.addEnchantment(enchantment, Math.max(1, Math.min(level, enchantment.getMaxLevel())));
    NBTTagCompound root = getOrCreateTag(stack);
    NBTTagCompound times = root.hasKey(EXPIRATIONS, 10) ? root.getCompoundTag(EXPIRATIONS) : new NBTTagCompound();
    times.setInteger(Integer.toString(enchantment.effectId), durationTicks);
    root.setTag(EXPIRATIONS, times);
    return true;
  }

  public static void removeExpired(ItemStack stack, World world) {
    if (stack == null || world == null || !stack.hasTagCompound()) return;
    NBTTagCompound root = stack.getTagCompound();
    if (!root.hasKey(EXPIRATIONS, 10)) return;
    NBTTagCompound times = root.getCompoundTag(EXPIRATIONS);
    Map enchantments = EnchantmentHelper.getEnchantments(stack);
    boolean changed = false;
    for (Object key : enchantments.keySet().toArray()) {
      int id = ((Number) key).intValue();
      String timer = Integer.toString(id);
      if (EnchantmentRegistry.isTemporary(id) && times.hasKey(timer)) {
        int remaining = times.getInteger(timer);
        if (remaining <= 1) {
          enchantments.remove(key); times.removeTag(timer); changed = true;
        } else {
          times.setInteger(timer, remaining - 1);
        }
      }
    }
    if (changed) EnchantmentHelper.setEnchantments(enchantments, stack);
    if (times.hasNoTags()) root.removeTag(EXPIRATIONS);
  }

  public static void removeAll(ItemStack stack) {
    if (stack == null) return;
    Map enchantments = EnchantmentHelper.getEnchantments(stack);
    boolean changed = false;
    for (Object key : enchantments.keySet().toArray()) {
      if (EnchantmentRegistry.isTemporary(((Number) key).intValue())) { enchantments.remove(key); changed = true; }
    }
    if (changed) EnchantmentHelper.setEnchantments(enchantments, stack);
    if (stack.hasTagCompound()) stack.getTagCompound().removeTag(EXPIRATIONS);
  }

  private static NBTTagCompound getOrCreateTag(ItemStack stack) {
    if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
    return stack.getTagCompound();
  }
}
