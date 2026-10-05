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
    public static final float RED = 0.67F;
    public static final float GREEN = 0.72F;
    public static final float BLUE = 0.71F;
    private static final Fade FADE = new Fade();
    private static Entity trackedEntity;
    private static Object trackedWorld;

    /** Two seconds, measured once across all render passes. */
    public static final class Fade {
        private static final long DURATION = 2_000_000_000L;
        private long started = -1L;
        private float strength;

        public void reset() { started = -1L; strength = 0F; }

        public float update(boolean affected, int amplifier, long now) {
            if (affected) {
                strength = Math.min(0.16F, 0.04F * (amplifier + 1));
                started = -1L;
                return strength;
            }
            if (strength == 0F) return 0F;
            if (started == -1L) started = now;
            float progress = Math.min(1F, Math.max(0F, (float)(now-started)/DURATION));
            if (progress == 1F) { reset(); return 0F; }
            return strength * (1F-progress*progress*(3F-2F*progress));
        }

        public float colorWeight(float density) { return strength == 0F ? 0F : density/strength; }
    }

    private static float update(Entity entity) {
        if (entity != trackedEntity || (entity != null && entity.worldObj != trackedWorld)) {
            FADE.reset(); trackedEntity = entity;
            trackedWorld = entity == null ? null : entity.worldObj;
        }
        if (!(entity instanceof EntityPlayer)) { FADE.reset(); return 0F; }
        net.minecraft.potion.PotionEffect effect = ((EntityPlayer)entity).getActivePotionEffect(PotionRegistry.DenseFog);
        return FADE.update(effect != null, effect == null ? 0 : effect.getAmplifier(), System.nanoTime());
    }

    public static boolean active(Entity entity) {
        return entity instanceof EntityPlayer
                && entity == Minecraft.getMinecraft().renderViewEntity
                && update(entity) > 0F
                && !entity.isInsideOfMaterial(Material.water)
                && !entity.isInsideOfMaterial(Material.lava);
    }

    public static float density(EntityPlayer player) {
        return update(player);
    }

    @SubscribeEvent
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (!active(event.entity)) return;
        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP2);
        event.density = density((EntityPlayer) event.entity);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onFogColors(EntityViewRenderEvent.FogColors event) {
        if (!active(event.entity)) return;
        float weight = FADE.colorWeight(update(event.entity));
        event.red += (RED-event.red)*weight;
        event.green += (GREEN-event.green)*weight;
        event.blue += (BLUE-event.blue)*weight;
    }
}
