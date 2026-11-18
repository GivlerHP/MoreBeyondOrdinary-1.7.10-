package ru.givler.mbo.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class TileEntityDescriptionWorkbench extends ModelTileBase implements IInventory {
    private ItemStack item;
    public TileEntityDescriptionWorkbench() { super("description_workbench", "description_workbench"); }
    @Override public boolean canUpdate() { return false; }
    public static boolean canEdit(EntityPlayer player) {
        return player != null && (ru.givler.mbo.config.WorkbenchConfig.publicHistory || canEditWeapon(player));
    }
    public static boolean canEditWeapon(EntityPlayer player) {
        return ru.givler.mbo.editor.BuilderAccess.canEdit(player, "ariamis.editor.weapon");
    }
    @Override public int getSizeInventory() { return 1; }
    @Override public ItemStack getStackInSlot(int slot) { return slot == 0 ? item : null; }
    @Override public ItemStack decrStackSize(int slot, int count) {
        if (slot != 0 || item == null || count <= 0) return null;
        ItemStack result;
        if (count >= item.stackSize) { result = item; item = null; }
        else result = item.splitStack(count);
        markDirty();
        return result;
    }
    @Override public ItemStack getStackInSlotOnClosing(int slot) {
        if (slot != 0) return null;
        ItemStack result = item; item = null; markDirty(); return result;
    }
    @Override public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot != 0) return;
        item = stack;
        if (item != null && item.stackSize > 1) item.stackSize = 1;
        markDirty();
    }
    @Override public String getInventoryName() { return "container.mbo.description_workbench"; }
    @Override public boolean hasCustomInventoryName() { return false; }
    @Override public int getInventoryStackLimit() { return 1; }
    @Override public boolean isUseableByPlayer(EntityPlayer player) {
        return canEdit(player) && worldObj != null && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + .5, yCoord + .5, zCoord + .5) <= 64;
    }
    @Override public void openInventory() {}
    @Override public void closeInventory() {}
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return slot == 0; }
    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (item != null) tag.setTag("Item", item.writeToNBT(new NBTTagCompound()));
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        item = tag.hasKey("Item", 10) ? ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Item")) : null;
    }
}
