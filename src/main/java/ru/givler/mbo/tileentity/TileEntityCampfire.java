package ru.givler.mbo.tileentity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.MoreBeyondOrdinary;

public final class TileEntityCampfire extends TileEntity {
    private static final int COOK_TIME = 600;
    private final ItemStack[] food = new ItemStack[4];
    private final int[] progress = new int[4];

    public ItemStack getFood(int slot) { return food[slot]; }

    private void changed() {
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public boolean addFood(ItemStack stack) {
        for (int i = 0; i < food.length; i++) if (food[i] == null) {
            food[i] = stack.copy();
            food[i].stackSize = 1;
            progress[i] = 0;
            changed();
            return true;
        }
        return false;
    }

    public void takeFood(EntityPlayer player) {
        for (int i = 0; i < food.length; i++) if (food[i] != null) {
            if (!player.inventory.addItemStackToInventory(food[i])) drop(food[i], i);
            food[i] = null;
            progress[i] = 0;
            changed();
            return;
        }
    }

    @Override public void updateEntity() {
        if ((worldObj.getBlockMetadata(xCoord, yCoord, zCoord) & 1) == 0) return;
        if (worldObj.isRemote) {
            MoreBeyondOrdinary.proxy.spawnCampfireParticles(worldObj, xCoord, yCoord, zCoord, food);
            return;
        }
        for (int i = 0; i < food.length; i++) if (food[i] != null && ++progress[i] >= COOK_TIME) {
            ItemStack result = FurnaceRecipes.smelting().getSmeltingResult(food[i]);
            drop(result == null ? food[i] : result.copy(), i);
            food[i] = null;
            progress[i] = 0;
            changed();
        }
    }

    public void dropFood() {
        for (int i = 0; i < food.length; i++) if (food[i] != null) {
            drop(food[i], i);
            food[i] = null;
        }
    }

    private void drop(ItemStack stack, int slot) {
        double dx = (slot & 1) == 0 ? .3 : .7;
        double dz = (slot & 2) == 0 ? .3 : .7;
        worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord + dx, yCoord + .55, zCoord + dz, stack));
    }

    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        for (int i = 0; i < 4; i++) if (food[i] != null) {
            NBTTagCompound item = new NBTTagCompound();
            food[i].writeToNBT(item);
            tag.setTag("Food" + i, item);
            tag.setInteger("Progress" + i, progress[i]);
        }
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        for (int i = 0; i < 4; i++) {
            food[i] = tag.hasKey("Food" + i) ? ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Food" + i)) : null;
            progress[i] = tag.getInteger("Progress" + i);
        }
    }

    @Override public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    @Override public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }
}
