package ru.givler.mbo.client.render.magic;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.magic.EntityMagicDecoy;

public final class RenderMagicDecoy extends RenderBiped {
  private static final ResourceLocation STEVE = new ResourceLocation("textures/entity/steve.png");

  public RenderMagicDecoy() {
    super(new ModelBiped(0), .5F);
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity e) {
    EntityLivingBase c = ((EntityMagicDecoy) e).caster();
    return c instanceof AbstractClientPlayer ? ((AbstractClientPlayer) c).getLocationSkin() : STEVE;
  }

  @Override
  protected void preRenderCallback(EntityLivingBase e, float p) {
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glColor4f(1, 1, 1, .65F);
  }
}
