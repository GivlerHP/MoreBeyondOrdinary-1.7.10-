package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import ru.givler.mbo.client.gamemode.GamemodeSwitcherInputHandler;
import net.minecraft.client.Minecraft;

public final class PacketGamemodeMenuPermission implements IMessage {
    private boolean allowed;
    public PacketGamemodeMenuPermission() {}
    public PacketGamemodeMenuPermission(boolean allowed) { this.allowed = allowed; }
    @Override public void fromBytes(ByteBuf buffer) { allowed = buffer.readBoolean(); }
    @Override public void toBytes(ByteBuf buffer) { buffer.writeBoolean(allowed); }

    public static final class Handler implements IMessageHandler<PacketGamemodeMenuPermission, IMessage> {
        @Override public IMessage onMessage(PacketGamemodeMenuPermission message, MessageContext context) {
            final boolean allowed = message.allowed;
            Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override public void run() {
                    GamemodeSwitcherInputHandler.handlePermission(allowed);
                }
            });
            return null;
        }
    }
}
