package ru.givler.mbo.item;

import baubles.api.IBauble;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

/** Common behaviour and tooltip API for every MBO Baubles item. */
public abstract class ItemBaubleBase extends Item implements IBauble {
    private String descriptionKey;
    private EnumChatFormatting descriptionColor = EnumChatFormatting.GRAY;

    protected ItemBaubleBase() { setMaxStackSize(1); }

    public int getDamagePerUse() { return 1; }
    @Override public void onWornTick(ItemStack stack, EntityLivingBase entity) { }
    @Override public void onEquipped(ItemStack stack, EntityLivingBase entity) { }
    @Override public void onUnequipped(ItemStack stack, EntityLivingBase entity) { }
    @Override public boolean canEquip(ItemStack stack, EntityLivingBase entity) { return true; }
    @Override public boolean canUnequip(ItemStack stack, EntityLivingBase entity) { return true; }

    public ItemBaubleBase setDescription(String key) {
        return setDescription(key, EnumChatFormatting.GRAY);
    }

    public ItemBaubleBase setDescription(String key, EnumChatFormatting color) {
        descriptionKey = key;
        descriptionColor = color == null ? EnumChatFormatting.GRAY : color;
        return this;
    }

    @Override public ItemBaubleBase setMaxDamage(int damage) {
        super.setMaxDamage(damage);
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        super.addInformation(stack, player, list, advanced);
        if (descriptionKey != null) addLocalizedDescription(list, descriptionKey, descriptionColor);
    }

    @SideOnly(Side.CLIENT)
    protected final void addLocalizedDescription(List list, String key, EnumChatFormatting color) {
        if (!StatCollector.canTranslate(key)) return;
        for (String line : StatCollector.translateToLocal(key).replace("\\n", "\n").split("\n")) {
            list.add(color + line);
        }
    }

    @Override public EnumRarity getRarity(ItemStack stack) { return EnumRarity.rare; }
}
