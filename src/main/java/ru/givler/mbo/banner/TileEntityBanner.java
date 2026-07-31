package ru.givler.mbo.banner;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.registry.BannerRegistry;

public class TileEntityBanner extends TileEntity {
    private int baseColor;
    private NBTTagList patterns = new NBTTagList();
    public boolean standing;

    public void setItemValues(ItemStack stack) {
        baseColor = BannerData.getBaseColor(stack);
        NBTTagList source = BannerData.getPatterns(stack, false);
        patterns = source == null ? new NBTTagList() : (NBTTagList) source.copy();
        markDirty();
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int getBaseColor() { return baseColor; }
    public NBTTagList getPatterns() { return patterns; }

    public ItemStack createStack() {
        ItemStack stack = new ItemStack(BannerRegistry.banner, 1, baseColor);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Base", baseColor);
        if (patterns != null && patterns.tagCount() > 0) tag.setTag("Patterns", patterns.copy());
        stack.setTagInfo("BlockEntityTag", tag);
        return stack;
    }

    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Base", baseColor);
        tag.setBoolean("Standing", standing);
        if (patterns != null) tag.setTag("Patterns", patterns);
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        baseColor = tag.getInteger("Base") & 15;
        standing = tag.getBoolean("Standing");
        patterns = tag.getTagList("Patterns", 10);
    }

    @Override public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }
}
