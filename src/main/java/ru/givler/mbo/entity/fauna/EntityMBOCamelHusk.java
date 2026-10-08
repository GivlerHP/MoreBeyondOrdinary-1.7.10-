package ru.givler.mbo.entity.fauna;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** Separately registered variant so vanilla spawn eggs keep the camel husk type. */
public final class EntityMBOCamelHusk extends EntityMBOCamel {
  public EntityMBOCamelHusk(World world) {
    super(world);
    setHorseType(3);
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    IEntityLivingData result = super.onSpawnWithEgg(data);
    setHorseType(3);
    return result;
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    setHorseType(3);
  }
}
