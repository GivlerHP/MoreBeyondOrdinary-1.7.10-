package ru.givler.mbo.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class TileEntityBarrel extends TileEntity implements IInventory {
    private final ItemStack[] contents = new ItemStack[27];
    private String customName;
    private int playersUsing;

    @Override public int getSizeInventory() { return contents.length; }
    @Override public ItemStack getStackInSlot(int slot) { return contents[slot]; }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = contents[slot];
        if (stack == null) return null;
        if (stack.stackSize <= amount) {
            contents[slot] = null;
            markDirty();
            return stack;
        }
        ItemStack result = stack.splitStack(amount);
        if (stack.stackSize == 0) contents[slot] = null;
        markDirty();
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = contents[slot];
        contents[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        contents[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) stack.stackSize = getInventoryStackLimit();
        markDirty();
    }

    @Override public String getInventoryName() { return hasCustomInventoryName() ? customName : "container.mbo.barrel"; }
    @Override public boolean hasCustomInventoryName() { return customName != null && !customName.isEmpty(); }
    public void setCustomName(String name) { customName = name; }
    @Override public int getInventoryStackLimit() { return 64; }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj != null && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {
        if (worldObj == null) return;
        if (playersUsing++ == 0 && !worldObj.isRemote) {
            setOpen(true);
            worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D,
                    "random.chestopen", 0.5F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
        }
    }

    @Override
    public void closeInventory() {
        if (worldObj == null) return;
        if (playersUsing > 0) --playersUsing;
        if (playersUsing == 0 && !worldObj.isRemote) {
            setOpen(false);
            worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D,
                    "random.chestclosed", 0.5F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
        }
    }

    private void setOpen(boolean open) {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (meta & 7) | (open ? 8 : 0), 3);
    }

    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList items = new NBTTagList();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null) continue;
            NBTTagCompound item = new NBTTagCompound();
            item.setByte("Slot", (byte) i);
            contents[i].writeToNBT(item);
            items.appendTag(item);
        }
        tag.setTag("Items", items);
        if (hasCustomInventoryName()) tag.setString("CustomName", customName);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList items = tag.getTagList("Items", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound item = items.getCompoundTagAt(i);
            int slot = item.getByte("Slot") & 255;
            if (slot < contents.length) contents[slot] = ItemStack.loadItemStackFromNBT(item);
        }
        if (tag.hasKey("CustomName", 8)) customName = tag.getString("CustomName");
    }
}
