package ru.givler.mbo.stonecutter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

final class SlotStonecutterResult extends Slot {
    private final ContainerStonecutter container;

    SlotStonecutterResult(ContainerStonecutter container, IInventory inventory,
                          int index, int x, int y) {
        super(inventory, index, x, y);
        this.container = container;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
        container.onResultTaken();
        super.onPickupFromSlot(player, stack);
    }
}
