package ru.givler.mbo.spectator;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;

/** Controls observer visibility without changing scoreboard teams. */
public final class SpectatorClientHandler {
    @SubscribeEvent
    public void beforeRender(RenderPlayerEvent.Pre event) {
        if (!SpectatorManager.isSpectator(event.entityPlayer)) return;
        if (event.entityPlayer == Minecraft.getMinecraft().thePlayer
                || !SpectatorManager.isSpectator(Minecraft.getMinecraft().thePlayer)) {
            event.setCanceled(true);
        } else {
            // The vanilla renderer skips invisible models. Reveal only for this render pass.
            event.entityPlayer.setInvisible(false);
        }
    }

    @SubscribeEvent
    public void afterRender(RenderPlayerEvent.Post event) {
        if (SpectatorManager.isSpectator(event.entityPlayer)) event.entityPlayer.setInvisible(true);
    }

    @SubscribeEvent
    public void onBlockOverlay(RenderBlockOverlayEvent event) {
        if (event.overlayType == RenderBlockOverlayEvent.OverlayType.BLOCK
                && SpectatorManager.isSpectator(event.player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onHud(RenderGameOverlayEvent.Pre event) {
        if ((event.type == RenderGameOverlayEvent.ElementType.HEALTH
                || event.type == RenderGameOverlayEvent.ElementType.FOOD)
                && SpectatorManager.isSpectator(Minecraft.getMinecraft().thePlayer)) {
            event.setCanceled(true);
        }
    }
}
