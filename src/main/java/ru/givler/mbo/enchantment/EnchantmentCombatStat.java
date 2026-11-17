package ru.givler.mbo.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.item.WeaponSettings;

/** Permanent melee enchantment, available at the table and on enchanted books. */
public final class EnchantmentCombatStat extends Enchantment {
    private final boolean negative;
    public EnchantmentCombatStat(int id, String name) { this(id,name,false); }
    public EnchantmentCombatStat(int id, String name, boolean negative) {
        super(id, 3, EnumEnchantmentType.weapon); setName(name); this.negative=negative;
    }
    @Override public int getMaxLevel() { return 3; }
    @Override public int getMinEnchantability(int level) { return 10 + (level - 1) * 9; }
    @Override public int getMaxEnchantability(int level) { return getMinEnchantability(level) + 20; }
    @Override public boolean canApply(ItemStack stack) { return WeaponSettings.supports(stack); }
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return !negative && canApply(stack); }
    @Override public boolean isAllowedOnBooks() { return true; }
    @Override public String getTranslatedName(int level) {
        return (negative ? net.minecraft.util.EnumChatFormatting.RED.toString() : "") + super.getTranslatedName(level);
    }
    @Override public boolean canApplyTogether(Enchantment other) {
        if(!super.canApplyTogether(other))return false;
        net.minecraft.enchantment.Enchantment speed=ru.givler.mbo.registry.EnchantmentRegistry.ATTACK_SPEED;
        net.minecraft.enchantment.Enchantment slow=ru.givler.mbo.registry.EnchantmentRegistry.ATTACK_SLOWNESS;
        net.minecraft.enchantment.Enchantment reach=ru.givler.mbo.registry.EnchantmentRegistry.EXTENDED_REACH;
        net.minecraft.enchantment.Enchantment shorter=ru.givler.mbo.registry.EnchantmentRegistry.SHORT_REACH;
        return !((this==speed && other==slow)||(this==slow && other==speed)
                ||(this==reach && other==shorter)||(this==shorter && other==reach));
    }
}
