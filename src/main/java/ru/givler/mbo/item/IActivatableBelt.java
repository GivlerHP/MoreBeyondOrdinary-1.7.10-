package ru.givler.mbo.item;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
/** Server-side activation of an equipped Baubles belt. */
public interface IActivatableBelt {
    void activateBelt(EntityPlayer player, ItemStack stack);
}
