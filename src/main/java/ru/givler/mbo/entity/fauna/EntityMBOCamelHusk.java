package ru.givler.mbo.entity.fauna;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import ru.givler.mbo.entity.ai.EntityAIUndeadCamelRider;

/** Separately registered variant so vanilla spawn eggs keep the camel husk type. */
public final class EntityMBOCamelHusk extends EntityMBOCamel {
  @Override
  protected boolean canDespawn() {
    return getEntityData().getBoolean("MBOHostileCamel")
        && !isTame()
        && !(riddenByEntity instanceof EntityPlayer);
  }

  @Override
  public boolean isCreatureType(EnumCreatureType type, boolean forSpawnCount) {
    if (getEntityData().getBoolean("MBOHostileCamel") && !isTame())
      return type == EnumCreatureType.monster;
    return super.isCreatureType(type, forSpawnCount);
  }

  public EntityMBOCamelHusk(World world) {
    super(world);
    setHorseType(3);
    tasks.addTask(0, new EntityAIUndeadCamelRider(this));
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
