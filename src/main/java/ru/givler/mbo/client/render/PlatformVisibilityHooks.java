package ru.givler.mbo.client.render;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;

public final class PlatformVisibilityHooks {
  private PlatformVisibilityHooks() {}

  public static AxisAlignedBB renderBounds(Entity entity) {
    return entity instanceof EntityMovingPlatform
        ? ((EntityMovingPlatform) entity).getPlatformRenderBounds() : entity.getBoundingBox();
  }
}
