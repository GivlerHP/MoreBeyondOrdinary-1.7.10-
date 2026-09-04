package ru.givler.mbo.spectator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSettings;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketSpectatorState;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class SpectatorManager {
    private static final String ACTIVE = "MBO_Spectator";
    private static final Set<UUID> CLIENT_SPECTATORS = Collections.synchronizedSet(new HashSet<UUID>());

    private SpectatorManager() {}

    public static boolean isSpectator(EntityPlayer player) {
        if (player.worldObj != null && player.worldObj.isRemote) return CLIENT_SPECTATORS.contains(player.getUniqueID());
        return persisted(player).getBoolean(ACTIVE);
    }

    public static void enter(EntityPlayerMP player) {
        if (isSpectator(player)) return;
        persisted(player).setBoolean(ACTIVE, true);
        player.setGameType(WorldSettings.GameType.ADVENTURE);
        apply(player);
        syncAll(player, true);
    }

    public static void leave(EntityPlayerMP player) {
        persisted(player).setBoolean(ACTIVE, false);
        moveOutOfBlocks(player);
        player.noClip = false;
        player.setInvisible(false);
        player.capabilities.disableDamage = false;
        player.capabilities.isFlying = false;
        player.capabilities.allowFlying = false;
        player.capabilities.allowEdit = true;
        player.sendPlayerAbilities();
        syncAll(player, false);
    }

    private static void moveOutOfBlocks(EntityPlayerMP player) {
        int attempts = 0;
        while (!player.worldObj.getCollidingBoundingBoxes(player, player.boundingBox).isEmpty()
                && player.posY < player.worldObj.getHeight() - 2 && attempts++ < 256) {
            player.setPositionAndUpdate(player.posX, Math.floor(player.posY) + 1.01D, player.posZ);
        }
        player.motionX = 0;
        player.motionY = 0;
        player.motionZ = 0;
        player.fallDistance = 0;
    }

    public static void apply(EntityPlayer player) {
        player.noClip = true;
        player.fallDistance = 0;
        player.setInvisible(true);
        player.capabilities.disableDamage = true;
        player.capabilities.allowFlying = true;
        player.capabilities.isFlying = true;
        player.capabilities.allowEdit = false;
        if (player instanceof EntityPlayerMP && player.ticksExisted % 20 == 0) {
            ((EntityPlayerMP) player).sendPlayerAbilities();
        }
    }

    public static void syncTo(EntityPlayerMP receiver) {
        for (Object object : receiver.mcServer.getConfigurationManager().playerEntityList) {
            EntityPlayerMP player = (EntityPlayerMP) object;
            PacketManager.INSTANCE.sendTo(new PacketSpectatorState(player.getUniqueID(), isSpectator(player)), receiver);
        }
    }

    public static void setClientState(UUID id, boolean active) {
        if (active) CLIENT_SPECTATORS.add(id); else CLIENT_SPECTATORS.remove(id);
    }

    private static void syncAll(EntityPlayerMP player, boolean active) {
        PacketManager.INSTANCE.sendToAll(new PacketSpectatorState(player.getUniqueID(), active));
    }

    private static NBTTagCompound persisted(EntityPlayer player) {
        NBTTagCompound root = player.getEntityData();
        if (!root.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) root.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
        return root.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
    }
}
