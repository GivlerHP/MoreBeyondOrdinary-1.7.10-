package ru.givler.mbo.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import ru.givler.mbo.movingplatform.PlatformAccess;
import ru.givler.mbo.tileentity.TileEntityPlatformStation;

public final class ContainerPlatformStation extends Container {
  private final TileEntityPlatformStation station;

  public ContainerPlatformStation(InventoryPlayer inventory, TileEntityPlatformStation station) {
    this.station = station;
    for (int slot = 0; slot < 36; slot++)
      addSlotToContainer(new Slot(inventory, slot, -10000, -10000));
  }

  @Override
  public boolean canInteractWith(EntityPlayer player) {
    return PlatformAccess.canEdit(player)
        && station.getWorldObj() != null
        && station.getWorldObj().getTileEntity(
            station.xCoord, station.yCoord, station.zCoord) == station
        && player.getDistanceSq(
            station.xCoord + 0.5D, station.yCoord + 0.5D, station.zCoord + 0.5D) <= 64D;
  }
}
