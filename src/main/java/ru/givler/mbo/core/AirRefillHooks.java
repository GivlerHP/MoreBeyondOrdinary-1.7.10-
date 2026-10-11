package ru.givler.mbo.core;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.block.material.Material;
import ru.givler.mbo.entity.EntityHangingAnchor;
import ru.givler.mbo.handler.HangingRules;

/** Matches the gradual air recovery used by modern Minecraft. */
public final class AirRefillHooks {
  private AirRefillHooks() {}

  public static boolean isSuffocating(net.minecraft.entity.Entity entity) {
    return entity.ridingEntity instanceof EntityHangingAnchor
        && ((EntityHangingAnchor) entity.ridingEntity).isSuffocating();
  }

  public static boolean needsAir(EntityLivingBase entity, Material material) {
    return material == Material.water && isSuffocating(entity)
        || entity.isInsideOfMaterial(material);
  }

  public static int drain(int vanilla, EntityLivingBase entity) {
    return isSuffocating(entity) ? HangingRules.nextAir(entity.getAir()) : vanilla;
  }

  public static int refill(EntityLivingBase entity) {
    return Math.min(300, entity.getAir() + 4);
  }
}
