package ru.givler.mbo.entity.fauna;

import net.minecraft.nbt.NBTTagCompound;

public interface IBucketableCreature {
  NBTTagCompound bucketData();

  void readBucketData(NBTTagCompound data);
}
