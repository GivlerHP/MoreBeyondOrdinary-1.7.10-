package ru.givler.mbo.entity.fauna;

import net.minecraft.world.World;

public final class EntityMBOPufferfish extends EntityMBOFish {
  public EntityMBOPufferfish(World world) {
    super(world);
    setSize(.175F, .175F);
  }

  public String species() {
    return "pufferfish";
  }

  public int foodMeta() {
    return 3;
  }
}
