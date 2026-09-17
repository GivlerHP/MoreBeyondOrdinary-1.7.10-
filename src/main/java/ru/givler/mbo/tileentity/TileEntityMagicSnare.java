package ru.givler.mbo.tileentity;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/** Stores the caster of a placed magic snare without a fixed entity id. */
public final class TileEntityMagicSnare extends TileEntity {
  private UUID casterId;

  public void setCaster(EntityLivingBase caster) {
    casterId = caster == null ? null : caster.getUniqueID();
    markDirty();
  }

  public EntityLivingBase getCaster() {
    if (casterId == null || worldObj == null) return null;
    for (Object object : worldObj.loadedEntityList) {
      if (object instanceof EntityLivingBase
          && casterId.equals(((Entity) object).getUniqueID())) {
        return (EntityLivingBase) object;
      }
    }
    return null;
  }

  @Override
  public void writeToNBT(NBTTagCompound tag) {
    super.writeToNBT(tag);
    if (casterId != null) tag.setString("Caster", casterId.toString());
  }

  @Override
  public void readFromNBT(NBTTagCompound tag) {
    super.readFromNBT(tag);
    try {
      casterId = UUID.fromString(tag.getString("Caster"));
    } catch (IllegalArgumentException ignored) {
      casterId = null;
    }
  }
}
