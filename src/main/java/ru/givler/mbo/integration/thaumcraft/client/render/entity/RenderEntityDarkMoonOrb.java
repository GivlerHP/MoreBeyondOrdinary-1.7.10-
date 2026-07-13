package ru.givler.mbo.integration.thaumcraft.client.render.entity;


import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.integration.thaumcraft.entities.EntityDarkMoonOrb;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.lib.UtilsFX;

/**
 * Рендер для EntityDarkMoonOrb - основан на структуре RenderEntityHomingShard,
 * но без зависимости от несинхронизированного поля strength, с более тёмной/фиолетовой окраской.
 */
public class RenderEntityDarkMoonOrb extends Render {

    private static final float FIXED_SCALE = 0.5F;

    public RenderEntityDarkMoonOrb() {
        shadowSize = 0.0F;
    }

    @Override
    public void doRender(final Entity entity, final double x, final double y, final double z, final float f,
                         final float partialTicks) {
        render((EntityDarkMoonOrb) entity, x, y, z, f, partialTicks);
    }

    public void render(final EntityDarkMoonOrb entity, final double x, final double y, final double z, final float f,
                       final float partialTicks) {
        final Tessellator t = Tessellator.instance;

        GL11.glPushMatrix();

        GL11.glDepthMask(false);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        UtilsFX.bindTexture(ParticleEngine.particleTexture);

        final float f2 = (8 + entity.ticksExisted % 8) / 16.0F;
        final float f3 = f2 + 0.0625F;
        final float f4 = 0.25F;
        final float f5 = f4 + 0.0625F;

        final float f6 = 1.0F;
        final float f7 = 0.5F;

        final float red = 0.15F;
        final float green = 0.03F;
        final float blue = 0.22F;
        final float alpha = 1.0F;

        GL11.glColor4f(red, green, blue, alpha);

        GL11.glPushMatrix();

        GL11.glTranslated(x, y, z);

        GL11.glRotatef(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(FIXED_SCALE, FIXED_SCALE, FIXED_SCALE);

        t.startDrawingQuads();

        t.setColorRGBA_F(red, green, blue, alpha);
        t.setNormal(0.0F, 1.0F, 0.0F);
        t.setBrightness(240);

        t.addVertexWithUV(-f7, -f7, 0.0D, f2, f5);
        t.addVertexWithUV(f6 - f7, -f7, 0.0D, f3, f5);
        t.addVertexWithUV(f6 - f7, 1.0F - f7, 0.0D, f3, f4);
        t.addVertexWithUV(-f7, 1.0F - f7, 0.0D, f2, f4);

        t.draw();

        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glDepthMask(true);

        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(final Entity entity) {
        return TextureMap.locationBlocksTexture;
    }
}
