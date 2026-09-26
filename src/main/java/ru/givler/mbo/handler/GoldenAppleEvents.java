package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import ru.givler.mbo.config.GameplayConfig;

/** Replaces both golden apple variants' potion effects while preserving pre-existing buffs. */
public final class GoldenAppleEvents {
  private static final int[] APPLE_EFFECTS = {
    Potion.regeneration.id,
    Potion.field_76444_x.id,
    Potion.resistance.id,
    Potion.fireResistance.id
  };
  private final Map<EntityPlayer, EffectSnapshot> snapshots =
      new WeakHashMap<EntityPlayer, EffectSnapshot>();

  @SubscribeEvent
  public void onStart(PlayerUseItemEvent.Start event) {
    if (!enabledApple(event.item) || event.entityPlayer.worldObj.isRemote) return;
    snapshots.put(event.entityPlayer, EffectSnapshot.capture(event.entityPlayer));
  }

  @SubscribeEvent
  public void onStop(PlayerUseItemEvent.Stop event) {
    if (isGoldenApple(event.item)) snapshots.remove(event.entityPlayer);
  }

  @SubscribeEvent
  public void onFinish(PlayerUseItemEvent.Finish event) {
    EntityPlayer player = event.entityPlayer;
    if (!enabledApple(event.item) || player.worldObj.isRemote) return;
    EffectSnapshot snapshot = snapshots.remove(player);
    for (int potionId : APPLE_EFFECTS) player.removePotionEffect(potionId);
    if (snapshot != null) snapshot.restore(player);
    player.attackEntityFrom(DamageSource.magic, 6.0F);
  }

  private static boolean enabledApple(ItemStack stack) {
    return GameplayConfig.goldenAppleInstantDamage && isGoldenApple(stack);
  }

  private static boolean isGoldenApple(ItemStack stack) {
    return stack != null && stack.getItem() == Items.golden_apple;
  }

  private static final class EffectSnapshot {
    final long tick;
    final Map<Integer, PotionEffect> effects = new HashMap<Integer, PotionEffect>();

    EffectSnapshot(long tick) { this.tick = tick; }

    static EffectSnapshot capture(EntityPlayer player) {
      EffectSnapshot result = new EffectSnapshot(player.worldObj.getTotalWorldTime());
      for (int potionId : APPLE_EFFECTS) {
        PotionEffect effect = player.getActivePotionEffect(Potion.potionTypes[potionId]);
        if (effect != null) result.effects.put(potionId, new PotionEffect(effect));
      }
      return result;
    }

    void restore(EntityPlayer player) {
      int elapsed = (int) Math.max(0L, player.worldObj.getTotalWorldTime() - tick);
      for (PotionEffect saved : effects.values()) {
        int duration = saved.getDuration() - elapsed;
        if (duration > 0)
          player.addPotionEffect(
              new PotionEffect(
                  saved.getPotionID(), duration, saved.getAmplifier(), saved.getIsAmbient()));
      }
    }
  }
}
