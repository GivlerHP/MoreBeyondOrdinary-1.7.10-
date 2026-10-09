package ru.givler.mbo.handler;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import ru.givler.mbo.integration.minefantasy2.UndeadMineFantasy;
import ru.givler.mbo.item.weapon.ItemBowMBO;
import ru.givler.mbo.item.weapon.ItemEffectArrow;

public final class EffectArrowEvents {
  private static ItemStack ammunition(EntityPlayer player) {
    for (ItemStack stack : player.inventory.mainInventory)
      if (stack != null
          && stack.stackSize > 0
          && stack.getItem() instanceof ItemEffectArrow
          && !ItemEffectArrow.effects(stack).isEmpty()) return stack;
    return null;
  }

  private static boolean managedByMF2(ItemStack bow) {
    return Loader.isModLoaded("minefantasy2") && UndeadMineFantasy.isBow(bow);
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void nock(ArrowNockEvent event) {
    if (event.isCanceled()
        || (event.result.getItem().getClass() != ItemBow.class
            && !(event.result.getItem() instanceof ItemBowMBO))
        || managedByMF2(event.result)
        || ammunition(event.entityPlayer) == null) return;
    event.entityPlayer.setItemInUse(
        event.result, event.result.getItem().getMaxItemUseDuration(event.result));
    event.setCanceled(true);
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void loose(ArrowLooseEvent event) {
    if (event.isCanceled()
        || (event.bow.getItem().getClass() != ItemBow.class
            && !(event.bow.getItem() instanceof ItemBowMBO))
        || managedByMF2(event.bow)) return;
    EntityPlayer player = event.entityPlayer;
    ItemStack ammo = ammunition(player);
    if (ammo == null) return;
    event.setCanceled(true);
    float charge = event.charge / 20F;
    charge = (charge * charge + charge * 2F) / 3F;
    if (event.bow.getItem() instanceof ItemBowMBO)
      charge *= ((ItemBowMBO) event.bow.getItem()).getDrawSpeed();
    charge = Math.min(1F, charge);
    if (charge < .1F || player.worldObj.isRemote) return;
    EntityArrow arrow = new EntityArrow(player.worldObj, player, charge * 2F);
    if (event.bow.getItem() instanceof ItemBowMBO)
      arrow.setDamage(arrow.getDamage() * ((ItemBowMBO) event.bow.getItem()).getDamageMultiplier());
    if (charge == 1F) arrow.setIsCritical(true);
    int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, event.bow);
    if (power > 0) arrow.setDamage(arrow.getDamage() + power * .5D + .5D);
    int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, event.bow);
    if (punch > 0) arrow.setKnockbackStrength(punch);
    if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, event.bow) > 0)
      arrow.setFire(100);
    UndeadEvents.markArrow(arrow, ammo);
    // Potion arrows are consumed even with Infinity, matching the modern item.
    arrow.canBePickedUp = player.capabilities.isCreativeMode ? 2 : 1;
    if (!player.worldObj.spawnEntityInWorld(arrow)) return;
    event.bow.damageItem(1, player);
    if (!player.capabilities.isCreativeMode) {
      --ammo.stackSize;
      for (int slot = 0; slot < player.inventory.mainInventory.length; slot++)
        if (player.inventory.mainInventory[slot] == ammo && ammo.stackSize <= 0)
          player.inventory.mainInventory[slot] = null;
    }
    player.worldObj.playSoundAtEntity(
        player, "random.bow", 1F, 1F / (player.getRNG().nextFloat() * .4F + 1.2F) + charge * .5F);
  }
}
