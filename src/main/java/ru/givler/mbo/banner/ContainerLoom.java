package ru.givler.mbo.banner;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.givler.mbo.registry.BannerRegistry;

public class ContainerLoom extends Container {
    private final InventoryBasic inputs;
    private final InventoryBasic output = new InventoryBasic("loom.result", false, 1);
    private int selectedPattern = -1;
    private final World world;
    private final int x, y, z;

    public ContainerLoom(InventoryPlayer playerInventory, World world, int x, int y, int z) {
        this.world=world; this.x=x; this.y=y; this.z=z;
        inputs = new InventoryBasic("loom.input", false, 3) {
            @Override public void markDirty() { super.markDirty(); updateResult(); }
        };
        addSlotToContainer(new Slot(inputs, 0, 13, 26) {
            @Override public boolean isItemValid(ItemStack stack) { return stack.getItem() == Item.getItemFromBlock(BannerRegistry.banner); }
        });
        addSlotToContainer(new Slot(inputs, 1, 33, 26) {
            @Override public boolean isItemValid(ItemStack stack) { return stack.getItem() == Items.dye; }
        });
        addSlotToContainer(new Slot(inputs, 2, 23, 45));
        addSlotToContainer(new Slot(output, 0, 143, 58) {
            @Override public boolean isItemValid(ItemStack stack) { return false; }
            @Override public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
                ItemStack banner = inputs.getStackInSlot(0);
                ItemStack dye = inputs.getStackInSlot(1);
                if (banner != null && dye != null) {
                    inputs.decrStackSize(0, 1);
                    inputs.decrStackSize(1, 1);
                }
                selectedPattern = -1;
                updateResult();
                super.onPickupFromSlot(player, stack);
            }
        });
        for (int row=0; row<3; row++) for (int col=0; col<9; col++)
            addSlotToContainer(new Slot(playerInventory, col + row*9 + 9, 8 + col*18, 84 + row*18));
        for (int col=0; col<9; col++) addSlotToContainer(new Slot(playerInventory, col, 8 + col*18, 142));
    }

    public int getSelectedPattern() { return selectedPattern; }
    public ItemStack getBanner() { return inputs.getStackInSlot(0); }
    public ItemStack getDye() { return inputs.getStackInSlot(1); }
    public ItemStack getPreview() {
        ItemStack result = output.getStackInSlot(0);
        return result != null ? result : inputs.getStackInSlot(0);
    }

    @Override public boolean enchantItem(EntityPlayer player, int id) {
        BannerPattern[] patterns = BannerPattern.values();
        if (id <= 0 || id >= patterns.length) return false;
        selectedPattern = id;
        updateResult();
        return output.getStackInSlot(0) != null;
    }

    private void updateResult() {
        ItemStack banner = inputs.getStackInSlot(0);
        ItemStack dye = inputs.getStackInSlot(1);
        ItemStack template = inputs.getStackInSlot(2);
        if (banner == null || dye == null || dye.getItem() != Items.dye || selectedPattern <= 0
                || selectedPattern >= BannerPattern.values().length
                || BannerData.getPatternCount(banner) >= BannerData.MAX_PATTERNS
                || !validTemplate(BannerPattern.values()[selectedPattern], template)) {
            output.setInventorySlotContents(0, null);
            return;
        }
        output.setInventorySlotContents(0, BannerData.addPattern(banner, BannerPattern.values()[selectedPattern], dye.getItemDamage()));
    }

    private boolean validTemplate(BannerPattern pattern, ItemStack stack) {
        switch (pattern) {
            case CURLY_BORDER: return stack != null && stack.getItem() == Item.getItemFromBlock(net.minecraft.init.Blocks.vine);
            case CREEPER: return stack != null && stack.getItem() == Items.skull && stack.getItemDamage() == 4;
            case BRICKS: return stack != null && stack.getItem() == Item.getItemFromBlock(net.minecraft.init.Blocks.brick_block);
            case SKULL: return stack != null && stack.getItem() == Items.skull && stack.getItemDamage() == 1;
            case FLOWER: return stack != null && stack.getItem() == Item.getItemFromBlock(net.minecraft.init.Blocks.red_flower) && stack.getItemDamage() == 8;
            case MOJANG: return stack != null && stack.getItem() == Items.golden_apple && stack.getItemDamage() == 1;
            default: return true;
        }
    }

    @Override public boolean canInteractWith(EntityPlayer player) {
        return world.getBlock(x,y,z) == BannerRegistry.loom
                && player.getDistanceSq(x+.5,y+.5,z+.5) <= 64;
    }

    @Override public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!player.worldObj.isRemote) {
            for (int i=0; i<3; i++) {
                ItemStack stack = inputs.getStackInSlotOnClosing(i);
                if (stack != null) player.dropPlayerItemWithRandomChoice(stack, false);
            }
        }
    }

    @Override public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = null;
        Slot slot = (Slot) inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return null;
        ItemStack stack = slot.getStack();
        result = stack.copy();
        if (index == 3) {
            if (!mergeItemStack(stack, 4, 40, true)) return null;
            slot.onSlotChange(stack, result);
        } else if (index >= 4) {
            if (stack.getItem() == Item.getItemFromBlock(BannerRegistry.banner)) {
                if (!mergeItemStack(stack, 0, 1, false)) return null;
            } else if (stack.getItem() == Items.dye) {
                if (!mergeItemStack(stack, 1, 2, false)) return null;
            } else if (!mergeItemStack(stack, 2, 3, false)) return null;
        } else if (!mergeItemStack(stack, 4, 40, false)) return null;
        if (stack.stackSize == 0) slot.putStack(null); else slot.onSlotChanged();
        if (stack.stackSize == result.stackSize) return null;
        slot.onPickupFromSlot(player, stack);
        return result;
    }
}
