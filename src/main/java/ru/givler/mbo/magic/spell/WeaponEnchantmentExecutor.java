package ru.givler.mbo.magic.spell;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import ru.givler.mbo.enchantment.TemporaryEnchantments;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.registry.EnchantmentRegistry;

/** Applies one temporary MBO enchantment to the first compatible hotbar weapon. */
public final class WeaponEnchantmentExecutor implements SpellExecutor {
  public enum Kind {
    IMBUEMENT,
    FIRE,
    FROST
  }

  private final Kind kind;

  public WeaponEnchantmentExecutor(Kind kind) {
    this.kind = kind;
  }

  @Override
  public SpellResult cast(SpellContext context) {
    if (!(context.caster() instanceof EntityPlayer)) return SpellResult.INVALID;
    EntityPlayer player = (EntityPlayer) context.caster();
    Enchantment existing =
        kind == Kind.FIRE
            ? EnchantmentRegistry.FLAMING_WEAPON
            : kind == Kind.FROST ? EnchantmentRegistry.FREEZING_WEAPON : null;
    if (alreadyHasEffect(player, existing)) return SpellResult.BLOCKED;

    for (int slot = 0; slot < 9; slot++) {
      ItemStack stack = player.inventory.getStackInSlot(slot);
      Enchantment enchantment = enchantmentFor(stack);
      if (enchantment == null
          || EnchantmentHelper.getEnchantmentLevel(enchantment.effectId, stack) > 0) continue;
      if (context.world().isRemote) return SpellResult.SUCCESS;
      int level =
          context.power() == 1.0F
              ? 1
              : Math.max(1, Math.min(4, Math.round((context.power() - 1.0F) / 0.15F)));
      if (!TemporaryEnchantments.apply(
          stack,
          enchantment,
          level,
          Math.max(1, (int) (900 * context.duration())),
          context.world())) {
        return SpellResult.BLOCKED;
      }
      SpellEffects.sparkleBurst(player, 10, 0.9F, 0.7F, 1.0F);
      context.world().playSoundAtEntity(player, "mbo:aura", 1.0F, 1.0F);
      return SpellResult.SUCCESS;
    }
    return SpellResult.BLOCKED;
  }

  private Enchantment enchantmentFor(ItemStack stack) {
    if (stack == null) return null;
    boolean sword = stack.getItem() instanceof ItemSword;
    boolean bow = stack.getItem() instanceof ItemBow;
    if (!sword && !bow) return null;
    if (kind == Kind.FIRE) return EnchantmentRegistry.FLAMING_WEAPON;
    if (kind == Kind.FROST) return EnchantmentRegistry.FREEZING_WEAPON;
    return sword ? EnchantmentRegistry.WEAPON_IMBUEMENT : EnchantmentRegistry.BOW_IMBUEMENT;
  }

  private boolean alreadyHasEffect(EntityPlayer player, Enchantment enchantment) {
    for (ItemStack stack : player.inventory.mainInventory) {
      if (stack == null) continue;
      if (enchantment != null
          && EnchantmentHelper.getEnchantmentLevel(enchantment.effectId, stack) > 0) return true;
      if (kind == Kind.IMBUEMENT
          && (EnchantmentHelper.getEnchantmentLevel(
                      EnchantmentRegistry.WEAPON_IMBUEMENT.effectId, stack)
                  > 0
              || EnchantmentHelper.getEnchantmentLevel(
                      EnchantmentRegistry.BOW_IMBUEMENT.effectId, stack)
                  > 0)) return true;
    }
    return false;
  }
}
