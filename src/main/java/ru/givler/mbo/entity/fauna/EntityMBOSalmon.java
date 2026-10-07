package ru.givler.mbo.entity.fauna;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.world.World;

public final class EntityMBOSalmon extends EntityMBOFish {
  public EntityMBOSalmon(World world) {
    super(world);
    setVariant(1);
    setSize(.7F, .4F);
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    int value = rand.nextInt(95);
    setVariant(value < 30 ? 0 : value < 80 ? 1 : 2);
    return super.onSpawnWithEgg(data);
  }

  public String species() {
    return "salmon";
  }

  public int foodMeta() {
    return 1;
  }
}
