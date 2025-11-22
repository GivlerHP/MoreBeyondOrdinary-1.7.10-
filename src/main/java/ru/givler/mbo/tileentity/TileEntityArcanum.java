package ru.givler.mbo.tileentity;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import ru.givler.mbo.recipes.ArcanumRecipes;
import ru.givler.mbo.recipes.ArcanumRecipesManager;

import java.util.Arrays;

public class TileEntityArcanum extends TileEntity implements IInventory {
    private ItemStack[] inventory = new ItemStack[11]; // 0–8: крафт, 9: топливо, 10: результат
    private int progress = 0;
    private int maxProgress = 200;
    private int soundTickCounter = 0;
    private ArcanumRecipes currentRecipe;
    private boolean isActive = false;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            // Можно отправить обновление на клиент, если нужно мгновенно обновлять рендер
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public void updateEntity() {
        boolean craftingPossible = canCraft() && hasFuel();
        setActive(craftingPossible); // обновляем активность печи

        if (craftingPossible) {
            progress++;
            soundTickCounter++;

            if (soundTickCounter >= 30) {
                worldObj.playSoundEffect(
                        xCoord + 0.5, yCoord + 0.5, zCoord + 0.5,
                        "fire.fire", 0.5F, 1.0F
                );
                soundTickCounter = 0;
            }
            if (progress >= maxProgress) {
                craftItem();
                worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5,
                        "random.fizz", 1.0F, 1.0F);
                burnFuel();
                progress = 0;
            }
        } else {
            progress = 0;
        }
        markDirty();
    }

    public boolean isCrafting() {
        return progress > 0;
    }

    private boolean canCraft() {
        ArcanumRecipes currentRecipe = ArcanumRecipesManager.getInstance().getMatchingRecipe(getCraftingMatrix());
        if (currentRecipe == null) {
            return false;
        }

        ItemStack outputSlot = inventory[10];
        ItemStack recipeOutput = currentRecipe.getOutput();

        if (outputSlot == null) {
            maxProgress = currentRecipe.getCookTime();
            return true;
        }

        if (outputSlot.isItemEqual(recipeOutput) && ItemStack.areItemStackTagsEqual(outputSlot, recipeOutput)) {
            int resultStackSize = outputSlot.stackSize + recipeOutput.stackSize;
            if (resultStackSize <= getInventoryStackLimit()) {
                maxProgress = currentRecipe.getCookTime();
                return true;
            }
        }
        return false;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory[index] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventory[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (inventory[index] != null) {
            ItemStack itemstack;

            if (inventory[index].stackSize <= count) {
                itemstack = inventory[index];
                inventory[index] = null;
                return itemstack;
            } else {
                itemstack = inventory[index].splitStack(count);

                if (inventory[index].stackSize == 0) {
                    inventory[index] = null;
                }

                return itemstack;
            }
        } else {
            return null;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        if (inventory[index] != null) {
            ItemStack itemstack = inventory[index];
            inventory[index] = null;
            return itemstack;
        }
        return null;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(net.minecraft.entity.player.EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this &&
                player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }

    @Override
    public void openInventory() {}
    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public String getInventoryName() {
        return "container.magic_furnace";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    private boolean hasFuel() {
        return inventory[9] != null && TileEntityFurnace.getItemBurnTime(inventory[9]) > 0;
    }

    private void burnFuel() {
        --inventory[9].stackSize;
        if (inventory[9].stackSize <= 0) inventory[9] = null;
    }

    private void craftItem() {
        ArcanumRecipes recipe = ArcanumRecipesManager.getInstance().getMatchingRecipe(getCraftingMatrix());
        if (recipe == null) return;

        ItemStack result = recipe.getOutput().copy();
        ItemStack current = inventory[10];

        if (current == null) {
            inventory[10] = result;
        } else if (current.isItemEqual(result) && ItemStack.areItemStackTagsEqual(current, result)) {
            int combinedSize = current.stackSize + result.stackSize;
            int maxSize = getInventoryStackLimit();

            if (combinedSize <= maxSize) {
                current.stackSize = combinedSize;
            } else {
                current.stackSize = maxSize;
            }
        } else {
            return;
        }

        for (int i = 0; i < 9; i++) {
            if (inventory[i] != null) {
                --inventory[i].stackSize;
                if (inventory[i].stackSize <= 0) inventory[i] = null;
            }
        }
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public ItemStack[] getCraftingMatrix() {
        return Arrays.copyOfRange(inventory, 0, 9);
    }

    // Реализация IInventory (getSizeInventory, getStackInSlot, setInventorySlotContents и т.д.)

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(itemTag);
                itemList.appendTag(itemTag);
            }
        }
        tag.setTag("Items", itemList);
        tag.setInteger("Progress", progress);
        tag.setInteger("MaxProgress", maxProgress);
        tag.setBoolean("IsActive", isActive);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        NBTTagList itemList = tag.getTagList("Items", 10); // 10 = NBTTagCompound
        inventory = new ItemStack[getSizeInventory()];
        for (int i = 0; i < itemList.tagCount(); i++) {
            NBTTagCompound itemTag = itemList.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }

        progress = tag.getInteger("Progress");
        maxProgress = tag.getInteger("MaxProgress");
        isActive = tag.getBoolean("IsActive");
    }

}

