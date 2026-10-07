package ru.givler.mbo.client.render.fauna;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.client.model.fauna.ModelMBOPolarBear;
import ru.givler.mbo.entity.fauna.EntityMBOPolarBear;

public final class RenderMBOPolarBear extends RenderLiving {
  private final ModelMBOPolarBear adult = new ModelMBOPolarBear(false),
      cub = new ModelMBOPolarBear(true);

  public RenderMBOPolarBear() {
    super(new ModelMBOPolarBear(false), .7F);
  }

  @Override
  public void doRender(
      EntityLiving entity, double x, double y, double z, float yaw, float partial) {
    mainModel = ((EntityMBOPolarBear) entity).isChild() ? cub : adult;
    super.doRender(entity, x, y, z, yaw, partial);
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return new ResourceLocation(
        "mbo",
        "textures/entity/bear/polarbear"
            + (((EntityMBOPolarBear) entity).isChild() ? "_baby" : "")
            + ".png");
  }
}
