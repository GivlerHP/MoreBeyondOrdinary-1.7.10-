package ru.givler.mbo.client.render.fauna;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.client.model.fauna.ModelMBOCamel;
import ru.givler.mbo.entity.fauna.EntityMBOCamel;

public final class RenderMBOCamel extends RenderLiving {
  private final ModelMBOCamel adult, baby = new ModelMBOCamel(true);
  private final ModelMBOCamel saddle = new ModelMBOCamel(false);

  public RenderMBOCamel() {
    super(new ModelMBOCamel(false), .8F);
    adult = (ModelMBOCamel) mainModel;
    saddle.saddlePass = true;
    setRenderPassModel(saddle);
  }

  @Override
  public void doRender(
      EntityLiving entity, double x, double y, double z, float yaw, float partial) {
    mainModel = ((EntityMBOCamel) entity).isChild() ? baby : adult;
    super.doRender(entity, x, y, z, yaw, partial);
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return new ResourceLocation(
        "mbo",
        "textures/entity/camel/camel"
            + (((EntityMBOCamel) entity).isChild() ? "_baby" : "")
            + ".png");
  }

  @Override
  protected int shouldRenderPass(EntityLivingBase entity, int pass, float partial) {
    EntityMBOCamel camel = (EntityMBOCamel) entity;
    if (pass != 0 || camel.isChild() || !camel.isHorseSaddled()) return -1;
    bindTexture(new ResourceLocation("mbo", "textures/entity/camel/saddle.png"));
    return 1;
  }
}
