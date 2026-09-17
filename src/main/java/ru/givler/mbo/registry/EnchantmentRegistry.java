package ru.givler.mbo.registry;

import net.minecraft.enchantment.Enchantment;
import ru.givler.mbo.enchantment.EnchantmentTimed;
import ru.givler.mbo.enchantment.EnchantmentWeaponImbuement;

/** Enchantments used internally by MBO spells. IDs are allocated from free vanilla slots. */
public final class EnchantmentRegistry {
  public static Enchantment WEAPON_IMBUEMENT;
  public static Enchantment BOW_IMBUEMENT;
  public static Enchantment FLAMING_WEAPON;
  public static Enchantment FREEZING_WEAPON;

  private EnchantmentRegistry() {}

  public static void register() {
    if (WEAPON_IMBUEMENT != null) return;
    WEAPON_IMBUEMENT = new EnchantmentWeaponImbuement(nextFreeId());
    BOW_IMBUEMENT = new EnchantmentTimed(nextFreeId(), "mbo.imbuement");
    FLAMING_WEAPON = new EnchantmentTimed(nextFreeId(), "mbo.flamingWeapon");
    FREEZING_WEAPON = new EnchantmentTimed(nextFreeId(), "mbo.freezingWeapon");
  }

  private static int nextFreeId() {
    for (int id = Enchantment.enchantmentsList.length - 1; id >= 0; id--) {
      if (Enchantment.enchantmentsList[id] == null) return id;
    }
    throw new IllegalStateException("No free enchantment IDs remain for MBO magic");
  }

  public static boolean isTemporary(int id) {
    return idOf(WEAPON_IMBUEMENT) == id || idOf(BOW_IMBUEMENT) == id
        || idOf(FLAMING_WEAPON) == id || idOf(FREEZING_WEAPON) == id;
  }

  private static int idOf(Enchantment enchantment) { return enchantment == null ? -1 : enchantment.effectId; }
}
