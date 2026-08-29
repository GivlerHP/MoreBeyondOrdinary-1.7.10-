package ru.givler.mbo.tileentity;

import net.minecraft.entity.player.EntityPlayer;

public class TileEntityLockableTrapdoor extends TileEntityLockableBarrier {
    @Override protected void openBarrier(EntityPlayer player) { setOpen(true, player); }
    @Override protected void closeBarrier() { setOpen(false, null); }

    private void setOpen(boolean open, EntityPlayer player) {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        if (((meta & 4) != 0) != open) {
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, open ? meta | 4 : meta & ~4, 2);
            worldObj.playAuxSFXAtEntity(null, 1003, xCoord, yCoord, zCoord, 0);
        }
    }
}
