package ru.givler.mbo.сontainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.tileentity.TileEntityArcanum;

public class ContainerArcanum extends Container {

    private TileEntityArcanum tile;

    public ContainerArcanum(InventoryPlayer playerInventory, TileEntityArcanum tile) {
        this.tile = tile;

        // Слоты крафта 3x3
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                addSlotToContainer(new Slot(tile, col + row * 3, 36 + col * 20, 8 + row * 20));

        // Топливо
        addSlotToContainer(new Slot(tile, 9, 132, 61));
        // Результат
        addSlotToContainer(new SlotFurnace(playerInventory.player, tile, 10, 132, 28));

        // Инвентарь игрока
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 106 + i * 18));

        for (int i = 0; i < 9; i++)
            addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 164));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            itemstack = stack.copy();

            // Слоты: 0–8 — крафт, 9 — топливо, 10 — результат, 11+ — инвентарь игрока
            if (index == 10) { // слот результата
                if (!this.mergeItemStack(stack, 11, 47, true)) {
                    return null;
                }
                slot.onSlotChange(stack, itemstack);
            } else if (index >= 11) { // инвентарь игрока
                if (!this.mergeItemStack(stack, 0, 9, false)) { // в крафтовую матрицу
                    if (!this.mergeItemStack(stack, 9, 10, false)) { // в слот топлива
                        return null;
                    }
                }
            } else { // из крафта или топлива -> в инвентарь
                if (!this.mergeItemStack(stack, 11, 47, false)) {
                    return null;
                }
            }

            if (stack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (stack.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, stack);
        }

        return itemstack;
    }
}
