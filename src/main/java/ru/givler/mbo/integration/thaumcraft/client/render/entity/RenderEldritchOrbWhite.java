package ru.givler.mbo.integration.thaumcraft.client.render.entity;

import java.util.Random;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.renderers.entity.RenderEldritchOrb;

public class RenderEldritchOrbWhite extends RenderEldritchOrb {

    private Random random = new Random();

    @Override
    public void renderEntityAt(Entity entity, double x, double y, double z, float fq, float pticks) {
        Tessellator tessellator = Tessellator.instance;
        this.random.setSeed(187L);
        GL11.glPushMatrix();
        RenderHelper.disableStandardItemLighting();
        float f1 = (float)entity.ticksExisted / 80.0F;
        float f3 = 0.9F;
        float f2 = 0.0F;
        Random random = new Random((long)entity.getEntityId());
        GL11.glTranslatef((float)x, (float)y, (float)z);
        GL11.glDisable(3553);
        GL11.glShadeModel(7425);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 1);
        GL11.glDisable(3008);
        GL11.glEnable(2884);
        GL11.glDepthMask(false);
        GL11.glPushMatrix();

        for (int i = 0; i < 12; ++i) {
            GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(random.nextFloat() * 360.0F + f1 * 360.0F, 0.0F, 0.0F, 1.0F);
            tessellator.startDrawing(6);
            float fa = random.nextFloat() * 20.0F + 5.0F + f2 * 10.0F;
            float f4 = random.nextFloat() * 2.0F + 1.0F + f2 * 2.0F;
            fa /= 30.0F / ((float)Math.min(entity.ticksExisted, 10) / 10.0F);
            f4 /= 30.0F / ((float)Math.min(entity.ticksExisted, 10) / 10.0F);
            tessellator.setColorRGBA_I(16777215, (int)(255.0F * (1.0F - f2)));
            tessellator.addVertex((double)0.0F, (double)0.0F, (double)0.0F);
            tessellator.setColorRGBA_I(16777215, 0); // белый (вместо BlockCustomOreItem.colors[5])
            tessellator.addVertex(-0.866 * (double)f4, (double)fa, (double)(-0.5F * f4));
            tessellator.addVertex(0.866 * (double)f4, (double)fa, (double)(-0.5F * f4));
            tessellator.addVertex((double)0.0F, (double)fa, (double)(1.0F * f4));
            tessellator.addVertex(-0.866 * (double)f4, (double)fa, (double)(-0.5F * f4));
            tessellator.draw();
        }

        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        GL11.glDisable(2884);
        GL11.glDisable(3042);
        GL11.glShadeModel(7424);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(3553);
        GL11.glEnable(3008);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();

        // --- вторая часть: раньше рисовался тёмный текстурированный спрайт из ParticleEngine ---
        // теперь вместо текстуры рисуем мягкое круглое белое свечение (triangle fan с угасанием альфы к краям)
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 1); // additive blending - даёт мягкое свечение без видимых краёв
        GL11.glDisable(3553); // отключаем текстурирование - больше не берём кадр из particleTexture

        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        float glowRadius = 0.6F;
        int segments = 20;

        tessellator.startDrawing(6); // GL_TRIANGLE_FAN
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        tessellator.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.8F);
        tessellator.addVertex(0.0D, 0.0D, 0.0D); // центр - непрозрачный
        tessellator.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.0F); // край - полностью прозрачный
        for (int i = 0; i <= segments; ++i) {
            double angle = 2.0D * Math.PI * (double)i / (double)segments;
            double px = Math.cos(angle) * (double)glowRadius;
            double py = Math.sin(angle) * (double)glowRadius;
            tessellator.addVertex(px, py, 0.0D);
        }
        tessellator.draw();

        GL11.glEnable(3553); // возвращаем текстурирование для последующих рендеров
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(3042);
        GL11.glDisable(32826);
        GL11.glPopMatrix();
    }
}