package ru.givler.mbo.item;

import baubles.api.BaubleType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public abstract class ItemAmuletBase extends ItemBaubleBase implements IActivatableAmulet {
    @Override public BaubleType getBaubleType(ItemStack stack) { return BaubleType.AMULET; }
    @Override public ItemAmuletBase setDescription(String key) { super.setDescription(key); return this; }
    @Override public ItemAmuletBase setDescription(String key, EnumChatFormatting color) { super.setDescription(key, color); return this; }
    @Override public ItemAmuletBase setMaxDamage(int damage) { super.setMaxDamage(damage); return this; }
}
