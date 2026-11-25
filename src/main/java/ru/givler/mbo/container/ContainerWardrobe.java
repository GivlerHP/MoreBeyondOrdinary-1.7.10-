package ru.givler.mbo.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ru.givler.mbo.item.skin.ItemSkinTemplate;
import ru.givler.mbo.item.skin.WardrobeData;
import ru.givler.mbo.item.skin.WardrobeSlot;
import ru.givler.mbo.network.packet.PacketWardrobe;

public final class ContainerWardrobe extends Container {
    private final EntityPlayer owner;
    private final InventoryBasic templates = new InventoryBasic("mbo.wardrobe", false, WardrobeSlot.ALL.length);
    private final boolean[] disabled = new boolean[WardrobeSlot.ALL.length];

    public ContainerWardrobe(EntityPlayer owner) {
        this.owner = owner;
        NBTTagCompound saved = owner.worldObj.isRemote ? new NBTTagCompound() : WardrobeData.inventory(owner);
        for (WardrobeSlot type : WardrobeSlot.ALL) {
            int index = type.ordinal();
            ItemStack stack = ItemStack.loadItemStackFromNBT(saved.getCompoundTag(type.key()));
            disabled[index] = WardrobeData.isDisabled(saved, type, stack);
            templates.setInventorySlotContents(index, stack);
            addSlotToContainer(new Slot(templates, index, -1000, -1000) {
                @Override public boolean isItemValid(ItemStack candidate) { return ItemSkinTemplate.slot(candidate) == type; }
                @Override public int getSlotStackLimit() { return 1; }
                @Override public boolean func_111238_b() { return !owner.worldObj.isRemote || xDisplayPosition >= 0; }
            });
        }
        if (owner instanceof EntityPlayerMP) templates.func_110134_a(inventory -> save());
        for (int index = 0; index < 36; index++) {
            int slot = index < 27 ? index + 9 : index - 27;
            addSlotToContainer(new Slot(owner.inventory, slot, 154 + index % 9 * 18, 158 + index / 9 * 18 + (index >= 27 ? 4 : 0)));
        }
    }

    private void save() {
        NBTTagCompound saved = WardrobeData.inventory(owner);
        for (WardrobeSlot type : WardrobeSlot.ALL) {
            ItemStack stack = templates.getStackInSlot(type.ordinal());
            if (stack == null) saved.removeTag(type.key());
            else saved.setTag(type.key(), stack.writeToNBT(new NBTTagCompound()));
            syncDisabled(type, WardrobeData.isDisabled(saved, type, stack));
        }
        PacketWardrobe.broadcast((EntityPlayerMP) owner);
    }

    public ItemStack template(WardrobeSlot slot) { return templates.getStackInSlot(slot.ordinal()); }

    public boolean isDisabled(WardrobeSlot slot) { return disabled[slot.ordinal()]; }

    @Override public void addCraftingToCrafters(ICrafting listener) {
        super.addCraftingToCrafters(listener);
        for (int i = 0; i < disabled.length; i++) {
            if (disabled[i]) listener.sendProgressBarUpdate(this, i, 1);
        }
    }

    @Override public void updateProgressBar(int id, int value) {
        if (id >= 0 && id < disabled.length) disabled[id] = value != 0;
    }

    private void syncDisabled(WardrobeSlot type, boolean value) {
        int index = type.ordinal();
        if (disabled[index] == value) return;
        disabled[index] = value;
        for (Object listener : crafters) ((ICrafting) listener).sendProgressBarUpdate(this, index, value ? 1 : 0);
    }

    @Override public boolean enchantItem(EntityPlayer player, int index) {
        if (!(player instanceof EntityPlayerMP) || player.openContainer != this || !canInteractWith(player)
                || index < 0 || index >= disabled.length) return false;
        WardrobeSlot slot = WardrobeSlot.ALL[index];
        ItemStack stack = template(slot);
        if (ItemSkinTemplate.slot(stack) != slot) return false;
        NBTTagCompound saved = WardrobeData.inventory(player);
        boolean value = !WardrobeData.isDisabled(saved, slot, stack);
        WardrobeData.setDisabled(saved, slot, stack, value);
        syncDisabled(slot, value);
        PacketWardrobe.broadcast((EntityPlayerMP) player);
        return true;
    }

    @Override public boolean canInteractWith(EntityPlayer player) { return player == owner && player.isEntityAlive(); }

    @Override public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = getSlot(index);
        if (slot == null || !slot.getHasStack()) return null;
        ItemStack current = slot.getStack(), previous = current.copy();
        if (index < WardrobeSlot.ALL.length) {
            if (!mergeItemStack(current, WardrobeSlot.ALL.length, inventorySlots.size(), true)) return null;
        } else {
            WardrobeSlot target = ItemSkinTemplate.slot(current);
            if (target == null || template(target) != null) return null;
            getSlot(target.ordinal()).putStack(current.splitStack(1));
        }
        if (current.stackSize == 0) slot.putStack(null);
        else slot.onSlotChanged();
        slot.onPickupFromSlot(player, current);
        return previous;
    }
}
