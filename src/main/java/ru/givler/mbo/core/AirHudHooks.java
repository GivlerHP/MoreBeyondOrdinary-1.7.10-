package ru.givler.mbo.core;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;

/** Keeps the vanilla air bar visible while the player refills it. */
public final class AirHudHooks {
  private AirHudHooks() {}

  public static boolean shouldRender(Entity entity, Material material) {
    return entity.isInsideOfMaterial(material) || entity.getAir() < 300;
  }
}
