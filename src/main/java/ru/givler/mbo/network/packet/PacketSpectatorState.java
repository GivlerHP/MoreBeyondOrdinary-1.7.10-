package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import ru.givler.mbo.spectator.SpectatorManager;

import java.util.UUID;

public final class PacketSpectatorState implements IMessage {
    private long most;
    private long least;
    private boolean active;

    public PacketSpectatorState() {}
    public PacketSpectatorState(UUID id, boolean active) {
        most = id.getMostSignificantBits();
        least = id.getLeastSignificantBits();
        this.active = active;
    }

    @Override public void fromBytes(ByteBuf buffer) {
        most = buffer.readLong(); least = buffer.readLong(); active = buffer.readBoolean();
    }
    @Override public void toBytes(ByteBuf buffer) {
        buffer.writeLong(most); buffer.writeLong(least); buffer.writeBoolean(active);
    }

    public static final class Handler implements IMessageHandler<PacketSpectatorState, IMessage> {
        @Override public IMessage onMessage(PacketSpectatorState message, MessageContext context) {
            SpectatorManager.setClientState(new UUID(message.most, message.least), message.active);
            return null;
        }
    }
}
