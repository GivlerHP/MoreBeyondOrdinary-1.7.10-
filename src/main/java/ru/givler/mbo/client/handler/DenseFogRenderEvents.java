package ru.givler.mbo.client.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.material.Material;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.registry.PotionRegistry;

/** Uses Minecraft's world fog while the camera player has Dense Fog. */
public final class DenseFogRenderEvents {
    private boolean active(Entity entity) {
        return entity instanceof EntityPlayer
                && entity == Minecraft.getMinecraft().renderViewEntity
                && ((EntityPlayer) entity).isPotionActive(PotionRegistry.DenseFog)
                && !entity.isInsideOfMaterial(Material.water)
                && !entity.isInsideOfMaterial(Material.lava);
    }

    @SubscribeEvent
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (!active(event.entity)) return;
        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP2);
        int amplifier = ((EntityPlayer) event.entity)
                .getActivePotionEffect(PotionRegistry.DenseFog).getAmplifier();
        event.density = Math.min(0.16F, 0.04F * (amplifier + 1));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onFogColors(EntityViewRenderEvent.FogColors event) {
        if (!active(event.entity)) return;
        event.red = 0.67F;
        event.green = 0.72F;
        event.blue = 0.71F;
    }
}
