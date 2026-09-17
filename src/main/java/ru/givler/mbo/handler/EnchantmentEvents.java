package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import ru.givler.mbo.registry.PotionRegistry;
import ru.givler.mbo.registry.EnchantmentRegistry;
import ru.givler.mbo.enchantment.TemporaryEnchantments;
import ru.givler.mbo.potion.SyncedPotionEffects;

/** Runtime behavior shared by all temporary MBO weapon enchantments. */
public final class EnchantmentEvents {
  public static final String FREEZING_ARROW_LEVEL = "mboFreezingArrowLevel";

  @SubscribeEvent
  public void onPlayerUpdate(LivingUpdateEvent event) {
    if (event.entityLiving.worldObj.isRemote || !(event.entityLiving instanceof EntityPlayer)) return;
    EntityPlayer player = (EntityPlayer) event.entityLiving;
    for (ItemStack stack : player.inventory.mainInventory) {
      TemporaryEnchantments.removeExpired(stack, player.worldObj);
    }
  }

  @SubscribeEvent
  public void onArrowSpawn(EntityJoinWorldEvent event) {
    if (event.world.isRemote || !(event.entity instanceof EntityArrow)) return;
    EntityArrow arrow = (EntityArrow) event.entity;
    if (!(arrow.shootingEntity instanceof EntityLivingBase)) return;
    ItemStack bow = ((EntityLivingBase) arrow.shootingEntity).getHeldItem();
    if (bow == null || !(bow.getItem() instanceof ItemBow)) return;
    int level = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.BOW_IMBUEMENT.effectId, bow);
    if (level > 0) arrow.setDamage(arrow.getDamage() + level * 0.5D + 0.5D);
    if (EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.FLAMING_WEAPON.effectId, bow) > 0) arrow.setFire(100);
    level = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.FREEZING_WEAPON.effectId, bow);
    if (level > 0) arrow.getEntityData().setInteger(FREEZING_ARROW_LEVEL, level);
  }

  @SubscribeEvent
  public void onLivingHurt(LivingHurtEvent event) {
    if (event.entityLiving.worldObj.isRemote) return;
    if (event.source.getEntity() instanceof EntityLivingBase) {
      ItemStack weapon = ((EntityLivingBase) event.source.getEntity()).getHeldItem();
      if (weapon != null && weapon.getItem() instanceof ItemSword) {
        int fire = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.FLAMING_WEAPON.effectId, weapon);
        if (fire > 0) event.entityLiving.setFire(fire * 4);
        int frost = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.FREEZING_WEAPON.effectId, weapon);
        if (frost > 0) SyncedPotionEffects.apply(event.entityLiving,
            new PotionEffect(PotionRegistry.Frost.id, frost * 200, 0, true));
      }
    }
    if (event.source.getSourceOfDamage() instanceof EntityArrow) {
      int frost = event.source.getSourceOfDamage().getEntityData().getInteger(FREEZING_ARROW_LEVEL);
      if (frost > 0) SyncedPotionEffects.apply(event.entityLiving,
          new PotionEffect(PotionRegistry.Frost.id, frost * 150, 0, true));
    }
  }

  @SubscribeEvent public void onItemToss(ItemTossEvent event) {
    TemporaryEnchantments.removeAll(event.entityItem.getEntityItem());
  }

  @SubscribeEvent public void onLivingDrops(LivingDropsEvent event) {
    for (EntityItem item : event.drops) TemporaryEnchantments.removeAll(item.getEntityItem());
  }
}
