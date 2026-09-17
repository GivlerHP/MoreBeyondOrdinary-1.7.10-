package ru.givler.mbo.client.render.magic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelHorse;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public final class RenderSpiritHorse extends RenderLiving {
  private static final ResourceLocation TEXTURE =
      new ResourceLocation("mbo", "textures/entity/magic/spirit_horse.png");

  public RenderSpiritHorse() {
    super(new ModelHorse(), 0.75F);
  }

  @Override
  protected void preRenderCallback(
      net.minecraft.entity.EntityLivingBase entity, float partialTick) {
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return TEXTURE;
  }
}
