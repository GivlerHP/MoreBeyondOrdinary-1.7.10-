package ru.givler.mbo.client.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import ru.givler.mbo.entity.EntityHangingAnchor;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketHangingEscape;

public final class HangingClientEvents {
    public static boolean hideFirstPersonPlayer(net.minecraft.entity.Entity entity) {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.gameSettings.thirdPersonView == 0 && entity == mc.renderViewEntity
            && entity.ridingEntity instanceof EntityHangingAnchor;
    }
    @SubscribeEvent public void renderPlayerModel(RenderPlayerEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.thirdPersonView == 0 && event.entityPlayer == mc.renderViewEntity
                && event.entityPlayer.ridingEntity instanceof EntityHangingAnchor) event.setCanceled(true);
    }
    @SubscribeEvent public void renderPlayer(RenderLivingEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        // Mounted players can be rendered through their riding link even in first person.
        // The camera lies inside our own head; the separately rendered hand is unaffected.
        if (mc.gameSettings.thirdPersonView == 0 && event.entity == mc.renderViewEntity
                && event.entity.ridingEntity instanceof EntityHangingAnchor) event.setCanceled(true);
    }
    public static void beginCamera() {
        ru.givler.mbo.core.HangingCameraContext.begin(Minecraft.getMinecraft().renderViewEntity);
    }

    public static boolean hideAttachment(net.minecraft.tileentity.TileEntity tile) {
        // The hook now passes through the chest, below the eye, so it stays visible.
        if (tile.getWorldObj() != null
                && tile.getBlockType() == ru.givler.mbo.registry.ModelRegistry.ModelTortureHook) return false;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.thirdPersonView != 0 || mc.renderViewEntity == null
                || !(mc.renderViewEntity.ridingEntity instanceof EntityHangingAnchor)) return false;
        if (tile.getWorldObj() != mc.renderViewEntity.worldObj) return false;
        if (((EntityHangingAnchor) mc.renderViewEntity.ridingEntity)
                .belongsTo(tile.xCoord, tile.yCoord, tile.zCoord)) return true;
        // The strand above a noose is a different tile, but can enclose the same camera.
        if (!(tile.getBlockType() instanceof ru.givler.mbo.block.model.BlockConnectedRope)
                && tile.getBlockType() != ru.givler.mbo.registry.ModelRegistry.ModelTortureRopeAroundTheNeck)
            return false;
        net.minecraft.entity.Entity view = mc.renderViewEntity;
        double eyeY = view.posY - view.yOffset + 1.62;
        double dx = Math.max(0, Math.max(tile.xCoord - view.posX, view.posX - tile.xCoord - 1));
        double dy = Math.max(0, Math.max(tile.yCoord - eyeY, eyeY - tile.yCoord - 1));
        double dz = Math.max(0, Math.max(tile.zCoord - view.posZ, view.posZ - tile.zCoord - 1));
        return dx * dx + dy * dy + dz * dz < .35 * .35;
    }
    private boolean previousSneak;
    private EntityHangingAnchor previousAnchor;

    /** Called after vanilla reads keyboard movement, leaving all living timers running. */
    public static void blockMovement(EntityPlayerSP player) {
        if (!(player.ridingEntity instanceof EntityHangingAnchor)) return;
        player.movementInput.moveForward = player.movementInput.moveStrafe = 0;
        player.movementInput.jump = player.movementInput.sneak = false;
        player.setSneaking(false);
    }

    @SubscribeEvent public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) { previousAnchor = null; previousSneak = false; return; }
        if (!(mc.thePlayer.ridingEntity instanceof EntityHangingAnchor)) {
            previousAnchor = null;
            previousSneak = false;
            return;
        }
        EntityHangingAnchor anchor = (EntityHangingAnchor) mc.thePlayer.ridingEntity;
        boolean sneak = mc.currentScreen == null && mc.gameSettings.keyBindSneak.getIsKeyPressed();
        if (previousAnchor != anchor) { previousAnchor = anchor; previousSneak = sneak; }
        if (sneak && !previousSneak) PacketManager.INSTANCE.sendToServer(new PacketHangingEscape());
        previousSneak = sneak;
        mc.thePlayer.setSneaking(false);
        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
        anchor.updateRiderPosition();
    }

}
