package ru.givler.mbo.client.render.magic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.client.model.magic.ModelHammer;
import ru.givler.mbo.entity.magic.EntityMagicConstruct;

@SideOnly(Side.CLIENT)
public class RenderMagicConstruct extends Render {
  private static final ResourceLocation HAMMER_TEXTURE =
      new ResourceLocation("mbo", "textures/entity/magic/lightning_hammer.png");
  private final ModelHammer hammerModel = new ModelHammer();

  public RenderMagicConstruct() {
    this.shadowSize = 0.5F;
  }

  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
    if (!(entity instanceof EntityMagicConstruct)) return;
    EntityMagicConstruct construct = (EntityMagicConstruct) entity;

    if (construct.getKind() == EntityMagicConstruct.Kind.HAMMER) {
      GL11.glPushMatrix();
      GL11.glTranslated(x, y + 1.5, z);
      GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);

      this.bindTexture(HAMMER_TEXTURE);
      hammerModel.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);

      GL11.glPopMatrix();
    }
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return HAMMER_TEXTURE;
  }
}
