package ru.givler.mbo.lockable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public interface ILockableTile {
    LockData getLockData();

    TileEntity asTileEntity();

    void onUnlocked(EntityPlayer player);
}
