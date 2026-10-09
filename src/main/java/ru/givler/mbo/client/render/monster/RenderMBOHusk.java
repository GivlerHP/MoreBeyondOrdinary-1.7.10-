package ru.givler.mbo.client.render.monster;

import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.client.model.monster.ModelMBOHusk;

public final class RenderMBOHusk extends RenderBiped {
  private static final ResourceLocation TEXTURE =
      new ResourceLocation("mbo:textures/entity/zombie/husk.png");

  public RenderMBOHusk() {
    super(new ModelMBOHusk(), .5F);
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return TEXTURE;
  }
}
