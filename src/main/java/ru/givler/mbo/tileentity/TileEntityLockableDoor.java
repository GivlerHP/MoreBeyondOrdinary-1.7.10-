package ru.givler.mbo.tileentity;

import net.minecraft.entity.player.EntityPlayer;

public class TileEntityLockableDoor extends TileEntityLockableBarrier {
    private int bottomY() { return (worldObj.getBlockMetadata(xCoord, yCoord, zCoord) & 8) != 0 ? yCoord - 1 : yCoord; }

    @Override protected void openBarrier(EntityPlayer player) { setOpen(true, player); }
    @Override protected void closeBarrier() { setOpen(false, null); }

    private void setOpen(boolean open, EntityPlayer player) {
        int y = bottomY();
        int meta = worldObj.getBlockMetadata(xCoord, y, zCoord);
        if (((meta & 4) != 0) != open) {
            worldObj.setBlockMetadataWithNotify(xCoord, y, zCoord, open ? meta | 4 : meta & ~4, 2);
            worldObj.playAuxSFXAtEntity(null, 1003, xCoord, y, zCoord, 0);
        }
    }
}
