package ru.givler.mbo.item;

import baubles.api.BaubleType;
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

public abstract class ItemBeltBase extends Item implements IBauble {

    private String descriptionKey;
    private EnumChatFormatting descriptionColor = EnumChatFormatting.GRAY;

    public ItemBeltBase() {
        this.setMaxStackSize(1);
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.BELT;
    }

    public int getDamagePerUse() {
        return 1;
    }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {

    }

    @Override
    public void onEquipped(ItemStack itemstack, EntityLivingBase player) {

    }

    @Override
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {

    }

    @Override
    public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
        return true;
    }

    @Override
    public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
        return true;
    }

    public ItemBeltBase setDescription(String langKey) {
        this.descriptionKey = langKey;
        return this;
    }

    @Override
    public ItemBeltBase setMaxDamage(int maxDamage) {
        super.setMaxDamage(maxDamage);
        return this;
    }

    public ItemBeltBase setDescription(String langKey, EnumChatFormatting color) {
        this.descriptionKey = langKey;
        this.descriptionColor = color;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        super.addInformation(stack, player, list, advanced);
        if (descriptionKey != null) {
            String translated = StatCollector.translateToLocal(descriptionKey);
            for (String line : translated.replace("\\n", "\n").split("\n")) {
                list.add(descriptionColor + line);
            }
        }
    }

    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.rare;
    }

}