package ru.givler.mbo.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public final class TileMirror extends TileEntity {
    @Override public boolean canUpdate() { return false; }
    @Override public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord,xCoord+1,yCoord+2,zCoord+1);
    }
}
