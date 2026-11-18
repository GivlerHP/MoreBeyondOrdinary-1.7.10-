package ru.givler.mbo.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class ContainerLockpicking extends Container {
    private final ru.givler.mbo.lockable.ILockableTile lock;

    public ContainerLockpicking(InventoryPlayer inventory) {
        this(inventory, null);
    }

    public ContainerLockpicking(InventoryPlayer inventory, ru.givler.mbo.lockable.ILockableTile lock) {
        this.lock = lock;
        // NetHandlerPlayServer expects the newly opened container to contain the
        // currently held inventory slot while it finishes processing the click.
        // The GUI itself is a GuiScreen, so keep these synchronization slots off-screen.
        for (int slot = 0; slot < 36; slot++) {
            addSlotToContainer(new Slot(inventory, slot, -10000, -10000));
        }
    }

    public boolean matches(EntityPlayer player, ru.givler.mbo.lockable.ILockableTile target) {
        return lock != null && lock == target && canInteractWith(player);
    }

    @Override public boolean canInteractWith(EntityPlayer player) {
        if (lock == null) return true;
        net.minecraft.tileentity.TileEntity tile = lock.asTileEntity();
        return !tile.isInvalid() && tile.getWorldObj() == player.worldObj
                && player.getDistanceSq(tile.xCoord + .5, tile.yCoord + .5, tile.zCoord + .5) <= 64
                && ru.givler.mbo.lockable.LockableAccess.hasLockpick(player);
    }

    @Override public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (lock != null) lock.getLockData().clearAttempt(player.getUniqueID());
    }
}
