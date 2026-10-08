package ru.givler.mbo.entity.fauna;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public final class EntityMBOSkeletonHorse extends EntityMBOUndeadHorse {
  public EntityMBOSkeletonHorse(World world) {
    super(world);
    setHorseType(4);
  }

  @Override
  protected double healthBonus() {
    return -.2D;
  }

  @Override
  protected double speedBonus() {
    return .2D;
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    IEntityLivingData result = super.onSpawnWithEgg(data);
    setHorseType(4);
    return result;
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    setHorseType(4);
  }
}
