package ru.givler.mbo.client.render.fauna;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.client.model.fauna.ModelMBOFrog;
import ru.givler.mbo.entity.fauna.EntityMBOFrog;

public final class RenderMBOFrog extends RenderLiving {
  private final boolean tadpole;

  public RenderMBOFrog(boolean tadpole) {
    super(new ModelMBOFrog(tadpole), tadpole ? .15F : .3F);
    this.tadpole = tadpole;
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    if (tadpole) return new ResourceLocation("mbo", "textures/entity/tadpole/tadpole.png");
    String[] variants = {"temperate", "warm", "cold"};
    return new ResourceLocation(
        "mbo",
        "textures/entity/frog/frog_" + variants[((EntityMBOFrog) entity).variant()] + ".png");
  }
}
