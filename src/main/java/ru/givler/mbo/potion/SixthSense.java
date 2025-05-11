package ru.givler.mbo.potion;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import ru.givler.mbo.main;
import org.lwjgl.opengl.GL11;

public class SixthSense extends Potion {

    private static final ResourceLocation icon = new ResourceLocation(main.MODID, "textures/gui/sixth_sense_icon.png");
    private static final ResourceLocation overlay = new ResourceLocation(main.MODID, "textures/gui/sixth_sense_overlay.png");
    private static final ResourceLocation marker = new ResourceLocation(main.MODID, "textures/entity/sixth_sense.png");

    public static int potionId;

    public SixthSense(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.sixth_sense");
        potionId = id;
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        // Обработка реализуется на клиенте через события
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }

    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        mc.renderEngine.bindTexture(icon);
        drawTexturedRect(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
    }

    public static void drawTexturedRect(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        float f = 1F / (float)textureWidth;
        float f1 = 1F / (float)textureHeight;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0, u * f, (v + height) * f1);
        tessellator.addVertexWithUV(x + width, y + height, 0, (u + width) * f, (v + height) * f1);
        tessellator.addVertexWithUV(x + width, y, 0, (u + width) * f, v * f1);
        tessellator.addVertexWithUV(x, y, 0, u * f, v * f1);
        tessellator.draw();
    }

    // Внутренний обработчик визуального эффекта
    public static class RenderHandler {

        @SubscribeEvent
        public void onRenderLiving(RenderLivingEvent.Post event) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.thePlayer;

            if (player == null || !player.isPotionActive(potionId) || event.entity == player) return;

            int amplifier = player.getActivePotionEffect(Potion.potionTypes[potionId]).getAmplifier();
            float range = 20 * (1 + amplifier * 0.5F); // Примерный множитель

            if (event.entity.getDistanceToEntity(player) >= range) return;

            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_LIGHTING);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            GL11.glTranslated(event.x, event.y + event.entity.height * 0.6, event.z);
            float yaw = mc.gameSettings.thirdPersonView == 2
                    ? RenderManager.instance.playerViewX
                    : -RenderManager.instance.playerViewX;
            GL11.glRotatef(180 - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(yaw, 1.0F, 0.0F, 0.0F);

            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            mc.renderEngine.bindTexture(marker);
            tessellator.addVertexWithUV(-0.6, 0.6, 0, 0, 0);
            tessellator.addVertexWithUV(0.6, 0.6, 0, 1, 0);
            tessellator.addVertexWithUV(0.6, -0.6, 0, 1, 1);
            tessellator.addVertexWithUV(-0.6, -0.6, 0, 0, 1);
            tessellator.draw();

            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glPopMatrix();
        }

        @SubscribeEvent
        public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
            Minecraft mc = Minecraft.getMinecraft();
            if (event.type != RenderGameOverlayEvent.ElementType.HELMET) return;
            if (mc.thePlayer == null || !mc.thePlayer.isPotionActive(potionId)) return;

            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(GL11.GL_ALPHA_TEST);

            mc.renderEngine.bindTexture(overlay);
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            int w = event.resolution.getScaledWidth();
            int h = event.resolution.getScaledHeight();
            tessellator.addVertexWithUV(0, h, -90, 0, 1);
            tessellator.addVertexWithUV(w, h, -90, 1, 1);
            tessellator.addVertexWithUV(w, 0, -90, 1, 0);
            tessellator.addVertexWithUV(0, 0, -90, 0, 0);
            tessellator.draw();

            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glColor4f(1F, 1F, 1F, 1F);
            GL11.glPopMatrix();
        }
    }
}
