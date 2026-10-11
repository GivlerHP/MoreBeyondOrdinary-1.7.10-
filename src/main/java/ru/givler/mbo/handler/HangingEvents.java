package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.block.model.BlockConnectedRope;
import ru.givler.mbo.block.model.RopeConnections;
import ru.givler.mbo.entity.EntityHangingAnchor;
import ru.givler.mbo.registry.ModelRegistry;

public final class HangingEvents {
    public static int mode(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block == ModelRegistry.ModelTortureHook) return 1;
        if (block == ModelRegistry.ModelTortureRopeAroundTheNeck) return 2;
        if (block instanceof BlockConnectedRope
                && RopeConnections.decoration(world.getBlockMetadata(x, y, z)) == RopeConnections.NOOSE)
            return 2;
        if (block == ModelRegistry.ModelTortureHangedCorpse) return 2;
        return 0;
    }

    public static boolean activate(World world, int x, int y, int z, EntityPlayer player) {
        int mode = mode(world, x, y, z);
        if (mode == 0) return false;
        if (world.isRemote || player.ridingEntity != null || !player.isEntityAlive()) return true;
        for (Object object : world.loadedEntityList)
            if (object instanceof EntityHangingAnchor && !((EntityHangingAnchor) object).isDead
                    && ((EntityHangingAnchor) object).belongsTo(x, y, z)) return true;
        BlockModels block = (BlockModels) world.getBlock(x, y, z);
        block.setBlockBoundsBasedOnState(world, x, y, z);
        double px = x + (block.getBlockBoundsMinX() + block.getBlockBoundsMaxX()) / 2;
        double pz = z + (block.getBlockBoundsMinZ() + block.getBlockBoundsMaxZ()) / 2;
        // Center the body on the hook or noose.
        int facing = world.getBlockMetadata(x, y, z) & 3;
        double headOffset = block == ModelRegistry.ModelTortureHook ? -.0625 : 0;
        // Connected nooses use the NORTH geometry regardless of the placement axis.
        if (block instanceof BlockConnectedRope) facing = 0;
        if (facing == 0) pz += headOffset;
        else if (facing == 1) px -= headOffset;
        else if (facing == 2) pz -= headOffset;
        else px += headOffset;
        // Multipart owner bounds are clipped; the model offset supplies the true attachment height.
        double py = y + (block == ModelRegistry.ModelTortureHangedCorpse
            ? 1 + block.getModelOffsetY() : mode == 2 ? .5625 : .8125);
        EntityHangingAnchor anchor = new EntityHangingAnchor(world, x, y, z, mode, px, py, pz);
        anchor.setLocationAndAngles(px, py, pz, facing * 90F, 0);
        if (world.spawnEntityInWorld(anchor)) {
            player.setSneaking(false);
            player.mountEntity(anchor);
            anchor.updateRiderPosition();
            if (mode == 1) anchor.damageFromHook();
        }
        return true;
    }

    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        release(event.player);
    }
    @SubscribeEvent public void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        release(event.player);
    }
    private static void release(EntityPlayer player) {
        if (player.ridingEntity instanceof EntityHangingAnchor)
            ((EntityHangingAnchor) player.ridingEntity).release();
    }
}
