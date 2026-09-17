package ru.givler.mbo.client.render.magic;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/** Particle-only projectile renderer. */
public final class RenderInvisibleProjectile extends Render {
  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) {}

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return null;
  }
}
