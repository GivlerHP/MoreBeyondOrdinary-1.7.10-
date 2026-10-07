package ru.givler.mbo.client.render.fauna;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.client.model.fauna.ModelMBOAdultTurtle;
import ru.givler.mbo.client.model.fauna.ModelMBOBabyTurtle;
import ru.givler.mbo.entity.fauna.EntityMBOTurtle;

public final class RenderMBOTurtle extends RenderLiving {
  private final ModelMBOAdultTurtle adult = new ModelMBOAdultTurtle();
  private final ModelMBOBabyTurtle baby = new ModelMBOBabyTurtle();

  public RenderMBOTurtle() {
    super(new ModelMBOAdultTurtle(), .5F);
  }

  @Override
  public void doRender(
      EntityLiving entity, double x, double y, double z, float yaw, float partial) {
    mainModel = ((EntityMBOTurtle) entity).isChild() ? baby : adult;
    super.doRender(entity, x, y, z, yaw, partial);
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return new ResourceLocation(
        "mbo",
        "textures/entity/turtle/"
            + (((EntityMBOTurtle) entity).isChild() ? "turtle_baby" : "turtle")
            + ".png");
  }
}
