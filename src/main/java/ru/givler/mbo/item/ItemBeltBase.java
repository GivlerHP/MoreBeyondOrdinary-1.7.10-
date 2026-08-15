package ru.givler.mbo.item;

import baubles.api.BaubleType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public abstract class ItemBeltBase extends ItemBaubleBase {
    @Override public BaubleType getBaubleType(ItemStack stack) { return BaubleType.BELT; }
    @Override public ItemBeltBase setDescription(String key) { super.setDescription(key); return this; }
    @Override public ItemBeltBase setDescription(String key, EnumChatFormatting color) { super.setDescription(key, color); return this; }
    @Override public ItemBeltBase setMaxDamage(int damage) { super.setMaxDamage(damage); return this; }
}
