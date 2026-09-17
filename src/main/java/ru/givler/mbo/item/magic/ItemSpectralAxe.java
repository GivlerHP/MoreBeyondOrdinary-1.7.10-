package ru.givler.mbo.item.magic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.givler.mbo.magic.item.SpectralEffects;
import ru.givler.mbo.magic.item.SpectralImpactEffect;
import ru.givler.mbo.magic.item.SpectralLifetime;

/** Axe-shaped spectral item; lifetime and impact behavior use the shared MBO components. */
public class ItemSpectralAxe extends ItemAxe {
  private final int lifetime;
  private final SpectralImpactEffect effect;

  public ItemSpectralAxe(ToolMaterial material, int lifetime, SpectralImpactEffect effect) {
    super(material);
    this.lifetime = lifetime;
    this.effect = effect == null ? SpectralEffects.NONE : effect;
    setMaxDamage(lifetime);
    setMaxStackSize(1);
    setNoRepair();
  }

  @Override
  public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
    boolean result = super.hitEntity(stack, target, attacker);
    if (!attacker.worldObj.isRemote) effect.apply(attacker, target);
    return result;
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
