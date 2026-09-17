package ru.givler.mbo.item.magic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.givler.mbo.magic.item.SpectralLifetime;

/** Pickaxe-shaped spectral item using the shared NBT lifetime. */
public class ItemSpectralPickaxe extends ItemPickaxe {
  private final int lifetime;

  public ItemSpectralPickaxe(ToolMaterial material, int lifetime) {
    super(material);
    this.lifetime = lifetime;
    setMaxDamage(lifetime);
    setMaxStackSize(1);
    setNoRepair();
  }

  @Override
  public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {
    SpectralLifetime.tick(stack, world, entity, slot, lifetime);
  }

  @Override
  public int getMaxDamage(ItemStack stack) {
    return SpectralLifetime.getLifetime(stack, lifetime);
  }

  @Override
  public int getDisplayDamage(ItemStack stack) {
    return SpectralLifetime.getAge(stack, lifetime);
  }

  @Override
  public boolean showDurabilityBar(ItemStack stack) {
    return true;
  }

  @Override
  public double getDurabilityForDisplay(ItemStack stack) {
    return SpectralLifetime.getProgress(stack, lifetime);
  }

  @Override
  public boolean hasEffect(ItemStack stack, int pass) {
    return true;
  }

  @Override
  public boolean getIsRepairable(ItemStack first, ItemStack second) {
    return false;
  }

  @Override
  public int getItemEnchantability() {
    return 0;
  }

  @Override
  public boolean onDroppedByPlayer(ItemStack stack, EntityPlayer player) {
    return false;
  }
}
