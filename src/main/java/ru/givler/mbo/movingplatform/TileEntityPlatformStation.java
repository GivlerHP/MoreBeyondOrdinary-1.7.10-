package ru.givler.mbo.movingplatform;

import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityPlatformStation extends TileEntity {
    private UUID platformId; private boolean targetB, powered;
    public void readLink(NBTTagCompound tag){if(tag.hasKey("PlatformId"))platformId=UUID.fromString(tag.getString("PlatformId"));targetB=tag.getBoolean("TargetB");markDirty();}
    public void updatePower(){
        if(worldObj==null||worldObj.isRemote)return;boolean now=worldObj.isBlockIndirectlyGettingPowered(xCoord,yCoord,zCoord);
        if(now&&!powered&&platformId!=null)for(Object o:new ArrayList(worldObj.loadedEntityList))if(o instanceof EntityMovingPlatform&&platformId.equals(((EntityMovingPlatform)o).getPlatformId())){((EntityMovingPlatform)o).start(targetB,null);break;}
        powered=now;markDirty();
    }
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);readLink(tag);powered=tag.getBoolean("Powered");}
    @Override public void writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);if(platformId!=null)tag.setString("PlatformId",platformId.toString());tag.setBoolean("TargetB",targetB);tag.setBoolean("Powered",powered);}
}
