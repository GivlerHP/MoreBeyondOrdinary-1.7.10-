package ru.givler.mbo.magic.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Immutable parameters of one server-authoritative spell attempt. */
public final class SpellContext {
  private final World world;
  private final EntityLivingBase caster;
  private final EntityLivingBase target;
  private final ItemStack sourceItem;
  private final int ticksInUse;
  private final float power;
  private final float range;
  private final float duration;
  private final float area;

  public SpellContext(
      World world,
      EntityLivingBase caster,
      EntityLivingBase target,
      ItemStack sourceItem,
      int ticksInUse,
      float power,
      float range,
      float duration,
      float area) {
    if (world == null) throw new IllegalArgumentException("world must not be null");
    if (caster == null) throw new IllegalArgumentException("caster must not be null");
    this.world = world;
    this.caster = caster;
    this.target = target;
    this.sourceItem = sourceItem;
    this.ticksInUse = ticksInUse;
    this.power = power;
    this.range = range;
    this.duration = duration;
    this.area = area;
  }

  public World world() {
    return world;
  }

  public EntityLivingBase caster() {
    return caster;
  }

  public EntityLivingBase target() {
    return target;
  }

  public ItemStack sourceItem() {
    return sourceItem;
  }

  public int ticksInUse() {
    return ticksInUse;
  }

  public float power() {
    return power;
  }

  public float range() {
    return range;
  }

  public float duration() {
    return duration;
  }

  public float area() {
    return area;
  }
}
