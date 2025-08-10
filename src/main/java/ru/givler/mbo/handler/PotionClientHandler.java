package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.potion.PotionEnum;

public class PotionClientHandler {

    private static final ResourceLocation icon = new ResourceLocation(MoreBeyondOrdinary.MODID, "textures/gui/sixth_sense_icon.png");
    private static final ResourceLocation overlay = new ResourceLocation(MoreBeyondOrdinary.MODID, "textures/gui/sixth_sense_overlay.png");
    private static final ResourceLocation marker = new ResourceLocation(MoreBeyondOrdinary.MODID, "textures/entity/sixth_sense.png");

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;

        if (player == null || !player.isPotionActive(PotionEnum.SIXTH) || event.entity == player) return;

        int amplifier = player.getActivePotionEffect(Potion.potionTypes[PotionEnum.SIXTH.id]).getAmplifier();
        float range = 20 * (1 + amplifier * 0.5F);

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
        if (mc.thePlayer == null || !mc.thePlayer.isPotionActive(PotionEnum.SIXTH)) return;

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
