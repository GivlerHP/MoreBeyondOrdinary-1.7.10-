package ru.givler.mbo.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IActivatableAmulet {

    void activate(EntityPlayer player, ItemStack stack);

    int getCooldownTicks();

    int getExperienceCost();
}