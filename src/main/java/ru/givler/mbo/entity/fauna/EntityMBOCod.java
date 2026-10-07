package ru.givler.mbo.entity.fauna;

import net.minecraft.world.World;

public final class EntityMBOCod extends EntityMBOFish {
  public EntityMBOCod(World world) {
    super(world);
  }

  public String species() {
    return "cod";
  }

  public int foodMeta() {
    return 0;
  }
}
