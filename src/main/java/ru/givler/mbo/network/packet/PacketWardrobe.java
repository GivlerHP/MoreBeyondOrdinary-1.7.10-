package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.gui.MboGuiType;
import ru.givler.mbo.item.skin.WardrobeData;
import ru.givler.mbo.network.EditorPacketAccess;
import ru.givler.mbo.network.PacketManager;

public final class PacketWardrobe implements IMessage {
    private int entityId;
    private long uuidMost, uuidLeast;
    private NBTTagCompound visual;

    public PacketWardrobe() {}

    private PacketWardrobe(EntityPlayer player) {
        entityId = player.getEntityId();
        uuidMost = player.getUniqueID().getMostSignificantBits();
        uuidLeast = player.getUniqueID().getLeastSignificantBits();
        visual = WardrobeData.visual(player);
    }

    public static void broadcast(EntityPlayerMP player) {
        PacketManager.INSTANCE.sendToDimension(new PacketWardrobe(player), player.dimension);
    }

    @Override public void fromBytes(ByteBuf buffer) {
        entityId = buffer.readInt();
        uuidMost = buffer.readLong();
        uuidLeast = buffer.readLong();
        visual = ByteBufUtils.readTag(buffer);
    }

    @Override public void toBytes(ByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeLong(uuidMost);
        buffer.writeLong(uuidLeast);
        ByteBufUtils.writeTag(buffer, visual);
    }

    public static final class ClientHandler implements IMessageHandler<PacketWardrobe, IMessage> {
        @Override public IMessage onMessage(PacketWardrobe message, MessageContext context) {
            EditorPacketAccess.scheduleClient(() -> {
                World world = MoreBeyondOrdinary.proxy.getClientWorld();
                if (world == null || message.visual == null) return;
                Entity entity = world.getEntityByID(message.entityId);
                if (entity instanceof EntityPlayer && entity.getUniqueID().getMostSignificantBits() == message.uuidMost
                        && entity.getUniqueID().getLeastSignificantBits() == message.uuidLeast) {
                    WardrobeData.acceptVisual((EntityPlayer) entity, message.visual);
                }
            });
            return null;
        }
    }

    public static final class Open implements IMessage {
        @Override public void fromBytes(ByteBuf buffer) {}
        @Override public void toBytes(ByteBuf buffer) {}
    }

    public static final class OpenHandler implements IMessageHandler<Open, IMessage> {
        @Override public IMessage onMessage(Open message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().playerEntity;
            EditorPacketAccess.schedule(context, () -> {
                if (player.isEntityAlive() && player.openContainer == player.inventoryContainer) {
                    player.openGui(MoreBeyondOrdinary.instance, MboGuiType.WARDROBE.id, player.worldObj, 0, 0, 0);
                }
            });
            return null;
        }
    }

    public static final class Events {
        @SubscribeEvent public void tracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event) {
            if (event.target instanceof EntityPlayer && event.entityPlayer instanceof EntityPlayerMP) {
                PacketManager.INSTANCE.sendTo(new PacketWardrobe((EntityPlayer) event.target), (EntityPlayerMP) event.entityPlayer);
            }
        }
        @SubscribeEvent public void joined(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.player instanceof EntityPlayerMP) broadcast((EntityPlayerMP) event.player);
        }
        @SubscribeEvent public void respawned(PlayerEvent.PlayerRespawnEvent event) {
            if (event.player instanceof EntityPlayerMP) broadcast((EntityPlayerMP) event.player);
        }
        @SubscribeEvent public void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (event.player instanceof EntityPlayerMP) broadcast((EntityPlayerMP) event.player);
        }
    }
}
