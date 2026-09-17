package ru.givler.mbo.magic.item;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import ru.givler.mbo.entity.magic.EntitySpectralArrow;
import ru.givler.mbo.item.magic.ItemSpectralAxe;
import ru.givler.mbo.item.magic.ItemSpectralBow;
import ru.givler.mbo.item.magic.ItemSpectralPickaxe;
import ru.givler.mbo.item.magic.ItemSpectralWeapon;

/** Runtime rules common to spectral items and arrows. */
public class SpectralItemEvents {
  @SubscribeEvent
  public void onLivingAttack(LivingAttackEvent event) {
    DamageSource source = event.source;
    if (source == null || !(source.getSourceOfDamage() instanceof EntitySpectralArrow)) return;
    EntitySpectralArrow arrow = (EntitySpectralArrow) source.getSourceOfDamage();
    EntityLivingBase attacker =
        source.getEntity() instanceof EntityLivingBase
            ? (EntityLivingBase) source.getEntity()
            : null;
    SpectralEffects.get(arrow.getImpactEffectId()).apply(attacker, event.entityLiving);
  }

  @SubscribeEvent
  public void onToss(ItemTossEvent event) {
    if (!isSpectral(event.entityItem.getEntityItem())) return;
    event.setCanceled(true);
    event.player.inventory.addItemStackToInventory(event.entityItem.getEntityItem());
  }

  @SubscribeEvent
  public void onDrops(LivingDropsEvent event) {
    for (int i = event.drops.size() - 1; i >= 0; i--) {
      if (isSpectral(event.drops.get(i).getEntityItem())) event.drops.get(i).setDead();
    }
  }

  private static boolean isSpectral(net.minecraft.item.ItemStack stack) {
    return stack != null
        && (stack.getItem() instanceof ItemSpectralWeapon
            || stack.getItem() instanceof ItemSpectralBow
            || stack.getItem() instanceof ItemSpectralAxe
            || stack.getItem() instanceof ItemSpectralPickaxe);
  }
}
