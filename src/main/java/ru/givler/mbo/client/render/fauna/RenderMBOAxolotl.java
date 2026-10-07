package ru.givler.mbo.client.render.fauna;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.client.model.fauna.ModelMBOAxolotl;
import ru.givler.mbo.entity.fauna.EntityMBOAxolotl;

public final class RenderMBOAxolotl extends RenderLiving {
  private final ModelMBOAxolotl adult, cub = new ModelMBOAxolotl(true);

  public RenderMBOAxolotl() {
    super(new ModelMBOAxolotl(false), .25F);
    adult = (ModelMBOAxolotl) mainModel;
  }

  @Override
  public void doRender(
      EntityLiving entity, double x, double y, double z, float yaw, float partial) {
    mainModel = ((EntityMBOAxolotl) entity).isChild() ? cub : adult;
    super.doRender(entity, x, y, z, yaw, partial);
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    EntityMBOAxolotl animal = (EntityMBOAxolotl) entity;
    return new ResourceLocation(
        "mbo",
        "textures/entity/axolotl/axolotl_"
            + EntityMBOAxolotl.VARIANTS[animal.variant()]
            + (animal.isChild() ? "_baby" : "")
            + ".png");
  }
}
