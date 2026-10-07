package ru.givler.mbo.client.render.fauna;

import net.minecraft.client.renderer.entity.RenderSquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.client.model.fauna.ModelMBOGlowSquid;
import ru.givler.mbo.entity.fauna.EntityMBOGlowSquid;

public final class RenderMBOGlowSquid extends RenderSquid {
  private final ModelMBOGlowSquid adult, cub = new ModelMBOGlowSquid(true);

  public RenderMBOGlowSquid() {
    super(new ModelMBOGlowSquid(false), .4F);
    adult = (ModelMBOGlowSquid) mainModel;
  }

  @Override
  public void doRender(EntitySquid entity, double x, double y, double z, float yaw, float partial) {
    mainModel = ((EntityMBOGlowSquid) entity).baby() ? cub : adult;
    super.doRender(entity, x, y, z, yaw, partial);
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return new ResourceLocation(
        "mbo",
        "textures/entity/squid/glow_squid"
            + (((EntityMBOGlowSquid) entity).baby() ? "_baby" : "")
            + ".png");
  }
}
