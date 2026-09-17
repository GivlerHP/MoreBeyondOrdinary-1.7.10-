package ru.givler.mbo.magic.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;

/** NBT-backed lifetime used by every temporary spectral item. */
public final class SpectralLifetime {
  private static final String EXPIRES_AT_TAG = "MBO_SpectralExpiresAt";
  private static final String DURATION_MULTIPLIER_TAG = "durationMultiplier";

  private SpectralLifetime() {}

  public static void tick(ItemStack stack, World world, Entity holder, int slot, int baseLifetime) {
    if (!(holder instanceof EntityPlayer) || world.isRemote) return;
    NBTTagCompound tag = getOrCreateTag(stack);
    long expiresAt = tag.getLong(EXPIRES_AT_TAG);
    if (expiresAt <= 0L) {
      expiresAt = world.getTotalWorldTime() + getLifetime(stack, baseLifetime);
      tag.setLong(EXPIRES_AT_TAG, expiresAt);
    }
    if (world.getTotalWorldTime() >= expiresAt) {
      EntityPlayer player = (EntityPlayer) holder;
      if (slot >= 0
          && slot < player.inventory.getSizeInventory()
          && player.inventory.getStackInSlot(slot) == stack) {
        player.inventory.setInventorySlotContents(slot, null);
      } else {
        player.inventory.consumeInventoryItem(stack.getItem());
      }
    }
  }

  public static int getAge(ItemStack stack) {
    return getAge(stack, stack.getMaxDamage());
  }

  public static int getAge(ItemStack stack, int baseLifetime) {
    if (!stack.hasTagCompound()) return 0;
    long expiresAt = stack.getTagCompound().getLong(EXPIRES_AT_TAG);
    World clientWorld = MoreBeyondOrdinary.proxy.getClientWorld();
    if (expiresAt <= 0L || clientWorld == null) return 0;
    int lifetime = getLifetime(stack, baseLifetime);
    long remaining = Math.max(0L, expiresAt - clientWorld.getTotalWorldTime());
    return (int) Math.max(0L, Math.min(lifetime, lifetime - remaining));
  }

  public static double getProgress(ItemStack stack, int baseLifetime) {
    int lifetime = getLifetime(stack, baseLifetime);
    return Math.max(0.0D, Math.min(1.0D, getAge(stack, baseLifetime) / (double) lifetime));
  }

  public static int getLifetime(ItemStack stack, int baseLifetime) {
    float multiplier =
        stack.hasTagCompound() ? stack.getTagCompound().getFloat(DURATION_MULTIPLIER_TAG) : 1.0F;
    if (multiplier <= 0.0F) multiplier = 1.0F;
    return Math.max(1, Math.round(baseLifetime * multiplier));
  }

  private static NBTTagCompound getOrCreateTag(ItemStack stack) {
    if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
    return stack.getTagCompound();
  }
}
