package ru.givler.mbo.entity.boat;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import ru.givler.mbo.registry.BoatRegistry;

public class EntityMBOChestBoat extends EntityMBOBoat implements IInventory {
    private ItemStack[] contents = new ItemStack[27];
    private boolean dropContents = true;

    public EntityMBOChestBoat(World world) { super(world); }

    public EntityMBOChestBoat(World world, double x, double y, double z, int type) {
        super(world, x, y, z, type);
    }

    @Override
    protected boolean hasSecondSeat() { return false; }

    @Override
    protected ItemStack getBoatDrop() {
        boolean bop = getBoatType() >= 6;
        return new ItemStack(bop ? BoatRegistry.bopChestBoats : BoatRegistry.vanillaChestBoats,
                1, bop ? getBoatType() - 6 : getBoatType());
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (player.isSneaking()) {
            if (!worldObj.isRemote) player.displayGUIChest(this);
            return true;
        }
        return super.interactFirst(player);
    }

    public int getSizeInventory() { return contents.length; }
    public ItemStack getStackInSlot(int slot) { return contents[slot]; }
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = contents[slot];
        if (stack == null) return null;
        if (stack.stackSize <= amount) { contents[slot] = null; return stack; }
        ItemStack result = stack.splitStack(amount);
        if (stack.stackSize == 0) contents[slot] = null;
        return result;
    }
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = contents[slot]; contents[slot] = null; return stack;
    }
    public void setInventorySlotContents(int slot, ItemStack stack) {
        contents[slot] = stack;
        if (stack != null && stack.stackSize > 64) stack.stackSize = 64;
    }
    public String getInventoryName() { return "container.mbo.chest_boat"; }
    public boolean hasCustomInventoryName() { return false; }
    public int getInventoryStackLimit() { return 64; }
    public void markDirty() { }
    public boolean isUseableByPlayer(EntityPlayer player) {
        return !isDead && player.getDistanceSqToEntity(this) <= 64.0D;
    }
    public void openInventory() { }
    public void closeInventory() { }
    public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }

    @Override
    public void travelToDimension(int dimension) {
        dropContents = false;
        super.travelToDimension(dimension);
    }

    @Override
    public void setDead() {
        if (!worldObj.isRemote && dropContents) {
            for (ItemStack stack : contents) {
                if (stack != null) worldObj.spawnEntityInWorld(
                        new EntityItem(worldObj, posX, posY, posZ, stack.copy()));
            }
        }
        super.setDead();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        NBTTagList items = new NBTTagList();
        for (int i = 0; i < contents.length; i++) if (contents[i] != null) {
            NBTTagCompound item = new NBTTagCompound();
            item.setByte("Slot", (byte)i);
            contents[i].writeToNBT(item);
            items.appendTag(item);
        }
        tag.setTag("Items", items);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        contents = new ItemStack[27];
        NBTTagList items = tag.getTagList("Items", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound item = items.getCompoundTagAt(i);
            int slot = item.getByte("Slot") & 255;
            if (slot < contents.length) contents[slot] = ItemStack.loadItemStackFromNBT(item);
        }
    }
}
