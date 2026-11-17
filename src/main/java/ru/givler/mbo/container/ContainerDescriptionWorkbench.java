package ru.givler.mbo.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench;

public final class ContainerDescriptionWorkbench extends Container {
    public final TileEntityDescriptionWorkbench tile;
    private final EntityPlayer owner;
    public boolean weaponAccess, historyAccess;
    private int lastAccess = -1;
    public ContainerDescriptionWorkbench(InventoryPlayer inventory, TileEntityDescriptionWorkbench tile) {
        this.tile = tile; this.owner = inventory.player;
        addSlotToContainer(new Slot(tile, 0, 18, 35));
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlotToContainer(new Slot(inventory, col + row * 9 + 9, 60 + col * 18, 205 + row * 18));
        for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(inventory, col, 60 + col * 18, 263));
    }
    @Override public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if(owner.worldObj != null && owner.worldObj.isRemote) return;
        weaponAccess = TileEntityDescriptionWorkbench.canEditWeapon(owner);
        historyAccess = ru.givler.mbo.config.WorkbenchConfig.publicHistory || weaponAccess;
        int access=(weaponAccess?1:0)|(historyAccess?2:0);
        if(access==lastAccess)return;
        lastAccess=access;
        for(Object object:crafters) {
            net.minecraft.inventory.ICrafting listener=(net.minecraft.inventory.ICrafting)object;
            listener.sendProgressBarUpdate(this,0,weaponAccess?1:0);
            listener.sendProgressBarUpdate(this,1,historyAccess?1:0);
        }
    }
    @Override public void updateProgressBar(int id,int value) {
        if(id==0) weaponAccess=value!=0;
        if(id==1) historyAccess=value!=0;
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
