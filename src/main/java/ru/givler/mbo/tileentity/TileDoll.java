package ru.givler.mbo.tileentity;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public final class TileDoll extends TileEntity implements IInventory {
    public static final int SIZE = 6, PARTS = 6, POSE_VALUES = PARTS * 3, VALUES = POSE_VALUES + 3;
    private final ItemStack[] equipment = new ItemStack[SIZE];
    private final int[] pose = new int[VALUES];
    private GameProfile profile;
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public Object clientModel;

    @Override public boolean canUpdate() { return false; }
    public GameProfile profile() { return profile; }
    public void setProfile(GameProfile profile) { this.profile = profile; markDirty(); }
    public int pose(int axis) { return pose[axis]; }

    public void setPose(int axis, int value) {
        if (axis < 0 || axis >= VALUES) return;
        int limit = axis < POSE_VALUES ? 180 : 10;
        pose[axis] = Math.max(-limit, Math.min(limit, value));
        markDirty();
    }

    public void resetPose() {
        java.util.Arrays.fill(pose, 0);
        markDirty();
    }

    public NBTTagCompound appearance() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setIntArray("Pose", pose.clone());
        if (profile != null) {
            NBTTagCompound owner = new NBTTagCompound();
            NBTUtil.func_152460_a(owner, profile);
            tag.setTag("Profile", owner);
        }
        return tag;
    }

    public void readAppearance(NBTTagCompound tag) {
        profile = tag.hasKey("Profile", 10) ? NBTUtil.func_152459_a(tag.getCompoundTag("Profile")) : null;
        int[] values = tag.getIntArray("Pose");
        java.util.Arrays.fill(pose, 0);
        for (int i = 0; i < pose.length && i < values.length; i++) {
            int limit = i < POSE_VALUES ? 180 : 10;
            pose[i] = Math.max(-limit, Math.min(limit, values[i]));
        }
    }

    @Override public void markDirty() {
        super.markDirty();
        if (worldObj != null && !worldObj.isRemote) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readAppearance(tag);
        for (int i = 0; i < SIZE; i++) equipment[i] = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Equipment" + i));
    }

    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagCompound view = appearance();
        tag.setIntArray("Pose", view.getIntArray("Pose"));
        if (view.hasKey("Profile", 10)) tag.setTag("Profile", view.getCompoundTag("Profile"));
        for (int i = 0; i < SIZE; i++) {
            if (equipment[i] != null) tag.setTag("Equipment" + i, equipment[i].writeToNBT(new NBTTagCompound()));
        }
    }

    @Override public Packet getDescriptionPacket() {
        NBTTagCompound data = new NBTTagCompound();
        writeToNBT(data);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, data);
    }

    @Override public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }

    @Override public int getSizeInventory() { return SIZE; }
    @Override public ItemStack getStackInSlot(int slot) { return equipment[slot]; }
    @Override public ItemStack decrStackSize(int slot, int count) {
        if (equipment[slot] == null || count < 1) return null;
        if (equipment[slot].stackSize <= count) return getStackInSlotOnClosing(slot);
        ItemStack removed = equipment[slot].splitStack(count);
        markDirty();
        return removed;
    }
    @Override public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack removed = equipment[slot];
        setInventorySlotContents(slot, null);
        return removed;
    }
    @Override public void setInventorySlotContents(int slot, ItemStack stack) {
        equipment[slot] = stack;
        if (stack != null && stack.stackSize > 1) stack.stackSize = 1;
        markDirty();
    }
    @Override public String getInventoryName() { return "mbo.doll.title"; }
    @Override public boolean hasCustomInventoryName() { return false; }
    @Override public int getInventoryStackLimit() { return 1; }
    @Override public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj != null && player.worldObj == worldObj && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + .5, yCoord + .5, zCoord + .5) <= 64;
    }
    @Override public void openInventory() {}
    @Override public void closeInventory() {}
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }
}
