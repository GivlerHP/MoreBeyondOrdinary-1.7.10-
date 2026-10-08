package ru.givler.mbo.entity.fauna;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public final class EntityMBOZombieHorse extends EntityMBOUndeadHorse {
  public EntityMBOZombieHorse(World world) {
    super(world);
    setHorseType(3);
  }

  @Override
  protected double healthBonus() {
    return .6D;
  }

  @Override
  protected double speedBonus() {
    return -.2D;
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
