package ru.givler.mbo.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.block.BlockDoll;
import ru.givler.mbo.tileentity.TileDoll;

public final class ContainerDoll extends Container {
    private final TileDoll doll;
    private final EntityPlayer owner;
    public static final int RESET = TileDoll.VALUES * 2, ROTATE = RESET + 1;

    public ContainerDoll(EntityPlayer player, TileDoll doll) {
        this.doll = doll;
        owner = player;
        for (int i = 0; i < TileDoll.SIZE; i++) addSlotToContainer(new Slot(doll, i, 8 + i * 24, 138));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(player.inventory, 9 + row * 9 + col, 153 + col * 18, 105 + row * 18));
        }
        for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(player.inventory, col, 153 + col * 18, 163));
    }

    @Override public boolean canInteractWith(EntityPlayer player) { return player == owner && doll.isUseableByPlayer(player); }

    private boolean canEdit(EntityPlayer player) {
        return canInteractWith(player) && (player.worldObj.isRemote
            || BlockDoll.canEdit(player, player.worldObj, doll.xCoord, doll.yCoord, doll.zCoord, 1));
    }

    @Override public ItemStack slotClick(int index, int button, int mode, EntityPlayer player) {
        return canEdit(player) ? super.slotClick(index, button, mode, player) : null;
    }

    @Override public boolean enchantItem(EntityPlayer player, int command) {
        if ((command != RESET && command != ROTATE) || !canEdit(player)) return false;
        if (player.worldObj.isRemote) return true;
        if (command == RESET) doll.resetPose();
        else {
            player.worldObj.setBlockMetadataWithNotify(doll.xCoord, doll.yCoord, doll.zCoord, (doll.getBlockMetadata() + 1) & 15, 3);
        }
        return true;
    }

    public void setPose(EntityPlayer player, int axis, int value) {
        if (axis >= 0 && axis < TileDoll.VALUES && canEdit(player) && !player.worldObj.isRemote) {
            doll.setPose(axis, value);
        }
    }

    @Override public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index < 0 || index >= inventorySlots.size()) return null;
        Slot slot = getSlot(index);
        if (!slot.getHasStack()) return null;
        ItemStack current = slot.getStack(), original = current.copy();
        if (index < TileDoll.SIZE) {
            if (!mergeItemStack(current, TileDoll.SIZE, inventorySlots.size(), true)) return null;
        } else {
            int target = firstFreeSlot(current);
            if (target < 0) return null;
            getSlot(target).putStack(current.splitStack(1));
        }
        if (current.stackSize == 0) slot.putStack(null);
        else slot.onSlotChanged();
        slot.onPickupFromSlot(player, current);
        return original;
    }

    private int firstFreeSlot(ItemStack stack) {
        ru.givler.mbo.item.skin.WardrobeSlot type = ru.givler.mbo.item.skin.ItemSkinTemplate.slot(stack);
        int part = stack.getItem() instanceof net.minecraft.item.ItemArmor
            ? ((net.minecraft.item.ItemArmor) stack.getItem()).armorType : type != null && type.armor() ? type.part() : -1;
        if (part >= 0 && part < 4 && !getSlot(part).getHasStack()) return part;
        for (int index = 4; index < TileDoll.SIZE; index++) if (!getSlot(index).getHasStack()) return index;
        for (int index = 0; index < 4; index++) if (!getSlot(index).getHasStack()) return index;
        return -1;
    }
}
