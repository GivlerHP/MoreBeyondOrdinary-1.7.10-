package ru.givler.mbo.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class ContainerLockpicking extends Container {
    public ContainerLockpicking(InventoryPlayer inventory) {
        // NetHandlerPlayServer expects the newly opened container to contain the
        // currently held inventory slot while it finishes processing the click.
        // The GUI itself is a GuiScreen, so keep these synchronization slots off-screen.
        for (int slot = 0; slot < 36; slot++) {
            addSlotToContainer(new Slot(inventory, slot, -10000, -10000));
        }
    }

    @Override public boolean canInteractWith(EntityPlayer player) { return true; }
}
