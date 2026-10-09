package ru.givler.mbo.entity.monster;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public final class EntityMBOStray extends EntityMBOVariantSkeleton {
  public EntityMBOStray(World world) {
    super(world);
  }

  @Override
  public String variant() {
    return "stray";
  }

  @Override
  public PotionEffect arrowEffect() {
    return new PotionEffect(Potion.moveSlowdown.id, 600);
  }
}
