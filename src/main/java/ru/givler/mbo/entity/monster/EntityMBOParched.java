package ru.givler.mbo.entity.monster;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFleeSun;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public final class EntityMBOParched extends EntityMBOVariantSkeleton {
  public EntityMBOParched(World world) {
    super(world);
    List<EntityAIBase> sunlight = new ArrayList<EntityAIBase>();
    for (Object raw : tasks.taskEntries) {
      EntityAIBase task = ((EntityAITaskEntry) raw).action;
      if (task instanceof EntityAIFleeSun || task instanceof EntityAIRestrictSun)
        sunlight.add(task);
    }
    for (EntityAIBase task : sunlight) tasks.removeTask(task);
  }

  @Override
  public String variant() {
    return "parched";
  }

  @Override
  protected double variantHealth() {
    return 16D;
  }

  @Override
  public PotionEffect arrowEffect() {
    return new PotionEffect(Potion.weakness.id, 600);
  }

  @Override
  public int bowCooldown(boolean hard) {
    return hard ? 50 : 70;
  }

  @Override
  public boolean isPotionApplicable(PotionEffect effect) {
    return effect.getPotionID() != Potion.weakness.id && super.isPotionApplicable(effect);
  }
}
