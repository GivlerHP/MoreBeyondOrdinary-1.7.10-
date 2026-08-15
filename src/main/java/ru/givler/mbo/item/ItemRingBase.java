package ru.givler.mbo.item;

import baubles.api.BaubleType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public abstract class ItemRingBase extends ItemBaubleBase {
    @Override public BaubleType getBaubleType(ItemStack stack) { return BaubleType.RING; }
    @Override public ItemRingBase setDescription(String key) { super.setDescription(key); return this; }
    @Override public ItemRingBase setDescription(String key, EnumChatFormatting color) { super.setDescription(key, color); return this; }
    @Override public ItemRingBase setMaxDamage(int damage) { super.setMaxDamage(damage); return this; }
}
