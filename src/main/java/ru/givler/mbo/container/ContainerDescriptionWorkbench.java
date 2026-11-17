package ru.givler.mbo.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench;

public final class ContainerDescriptionWorkbench extends Container {
    public final TileEntityDescriptionWorkbench tile;
    public ContainerDescriptionWorkbench(InventoryPlayer inventory, TileEntityDescriptionWorkbench tile) {
        this.tile = tile;
        addSlotToContainer(new Slot(tile, 0, 18, 35));
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlotToContainer(new Slot(inventory, col + row * 9 + 9, 60 + col * 18, 205 + row * 18));
        for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(inventory, col, 60 + col * 18, 263));
    }
    @Override public boolean canInteractWith(EntityPlayer player) { return tile.isUseableByPlayer(player); }
    @Override public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (!canInteractWith(player)) return null;
        Slot slot = (Slot) inventorySlots.get(index);
        if (!slot.getHasStack()) return null;
        ItemStack stack = slot.getStack(), original = stack.copy();
        if (index == 0) {
            if (!mergeItemStack(stack, 1, inventorySlots.size(), true)) return null;
        } else {
            if (tile.getStackInSlot(0) != null || !tile.isItemValidForSlot(0, stack)) return null;
            tile.setInventorySlotContents(0, stack.splitStack(1));
        }
        if (stack.stackSize == 0) slot.putStack(null); else slot.onSlotChanged();
        slot.onPickupFromSlot(player, stack);
        return original;
    }
}
