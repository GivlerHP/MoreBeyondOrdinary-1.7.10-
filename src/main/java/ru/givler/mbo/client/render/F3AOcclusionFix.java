package ru.givler.mbo.client.render;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;
import java.util.Collection;

/** Prevents entities being rendered through temporarily empty chunk meshes after F3+A. */
public final class F3AOcclusionFix {
    private static final int FALLBACK_TIMEOUT = 40;
    private static final int STABLE_TICKS_REQUIRED = 2;

    public static final class ReloadGuard {
        private final Field renderersToUpdate;
        private boolean comboWasDown;
        private boolean active;
        private int activeTicks;
        private int stableTicks;

        private ReloadGuard() {
            Field field;
            try {
                field = ReflectionHelper.findField(RenderGlobal.class,
                        "worldRenderersToUpdate", "field_72767_j");
            } catch (RuntimeException ignored) {
                field = null;
            }
            renderersToUpdate = field;
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            Minecraft mc = Minecraft.getMinecraft();

            if (event.phase == TickEvent.Phase.START) {
                boolean comboDown = Keyboard.isKeyDown(Keyboard.KEY_F3)
                        && Keyboard.isKeyDown(Keyboard.KEY_A);
                if (comboDown && !comboWasDown) {
                    active = true;
                    activeTicks = 0;
                    stableTicks = 0;
                }
                comboWasDown = comboDown;
                return;
            }

            if (!active) return;
            if (mc.theWorld == null || mc.renderGlobal == null) {
                reset();
                return;
            }

            activeTicks++;
            Boolean pending = hasPendingChunkUpdates(mc.renderGlobal);
            if (Boolean.FALSE.equals(pending)) {
                if (++stableTicks >= STABLE_TICKS_REQUIRED) reset();
            } else {
                stableTicks = 0;
            }

            if (activeTicks >= FALLBACK_TIMEOUT) reset();
        }

        private Boolean hasPendingChunkUpdates(RenderGlobal renderGlobal) {
            if (renderersToUpdate == null) return null;
            try {
                Object value = renderersToUpdate.get(renderGlobal);
                return value instanceof Collection && !((Collection) value).isEmpty();
            } catch (IllegalAccessException ignored) {
                return null;
            }
        }

        private void reset() {
            active = false;
            activeTicks = 0;
            stableTicks = 0;
        }

        private boolean isActive() {
            return active;
        }
    }

    public static final class PartialTickTracker {
        private volatile float partialTicks = 1F;

        @SubscribeEvent
        public void onRenderTick(TickEvent.RenderTickEvent event) {
            if (event.phase == TickEvent.Phase.START) partialTicks = event.renderTickTime;
        }
    }

    public static final class EntityRenderGuard {
        private final ReloadGuard guard;
        private final PartialTickTracker tickTracker;

        private EntityRenderGuard(ReloadGuard guard, PartialTickTracker tickTracker) {
            this.guard = guard;
            this.tickTracker = tickTracker;
        }

        @SubscribeEvent
        public void onRenderLiving(RenderLivingEvent.Pre event) {
            if (!guard.isActive()) return;

            Entity target = event.entity;
            EntityPlayer viewer = Minecraft.getMinecraft().thePlayer;
            if (viewer != null && target != viewer && !hasLineOfSight(viewer, target)) {
                event.setCanceled(true);
            }
        }

        private boolean hasLineOfSight(EntityPlayer viewer, Entity target) {
            float partialTicks = tickTracker.partialTicks;
            double vx = interpolate(viewer.lastTickPosX, viewer.posX, partialTicks);
            double vy = interpolate(viewer.lastTickPosY, viewer.posY, partialTicks) + viewer.getEyeHeight();
            double vz = interpolate(viewer.lastTickPosZ, viewer.posZ, partialTicks);
            Vec3 from = Vec3.createVectorHelper(vx, vy, vz);

            double tx = interpolate(target.lastTickPosX, target.posX, partialTicks);
            double ty = interpolate(target.lastTickPosY, target.posY, partialTicks);
            double tz = interpolate(target.lastTickPosZ, target.posZ, partialTicks);
            double halfWidth = Math.max(0.1D, target.width * 0.4D);

            return clear(viewer, from, tx, ty + target.height * 0.1D, tz)
                    || clear(viewer, from, tx, ty + target.height * 0.5D, tz)
                    || clear(viewer, from, tx, ty + target.height * 0.9D, tz)
                    || clear(viewer, from, tx - halfWidth, ty + target.height * 0.6D, tz)
                    || clear(viewer, from, tx + halfWidth, ty + target.height * 0.6D, tz);
        }

        private boolean clear(EntityPlayer viewer, Vec3 from, double x, double y, double z) {
            MovingObjectPosition hit = viewer.worldObj.func_147447_a(from,
                    Vec3.createVectorHelper(x, y, z), false, true, false);
            return hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK;
        }

        private double interpolate(double previous, double current, float partialTicks) {
            return previous + (current - previous) * partialTicks;
        }
    }

    private F3AOcclusionFix() {}

    public static void register() {
        ReloadGuard guard = new ReloadGuard();
        PartialTickTracker tickTracker = new PartialTickTracker();
        FMLCommonHandler.instance().bus().register(guard);
        FMLCommonHandler.instance().bus().register(tickTracker);
        MinecraftForge.EVENT_BUS.register(new EntityRenderGuard(guard, tickTracker));
    }
}
