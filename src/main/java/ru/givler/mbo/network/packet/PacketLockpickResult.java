package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import ru.givler.mbo.client.gui.GuiLockpicking;

public class PacketLockpickResult implements IMessage {
    private boolean correct, reset, complete, close; private int pin;
    public PacketLockpickResult() {}
    public PacketLockpickResult(boolean correct, int pin, boolean reset, boolean complete, boolean close) { this.correct=correct; this.pin=pin; this.reset=reset; this.complete=complete; this.close=close; }
    @Override public void fromBytes(ByteBuf b) { correct=b.readBoolean(); pin=b.readInt(); reset=b.readBoolean(); complete=b.readBoolean(); close=b.readBoolean(); }
    @Override public void toBytes(ByteBuf b) { b.writeBoolean(correct); b.writeInt(pin); b.writeBoolean(reset); b.writeBoolean(complete); b.writeBoolean(close); }
    public static class Handler implements IMessageHandler<PacketLockpickResult, IMessage> {
        @Override public IMessage onMessage(PacketLockpickResult m, MessageContext ctx) {
            if (Minecraft.getMinecraft().currentScreen instanceof GuiLockpicking)
                ((GuiLockpicking) Minecraft.getMinecraft().currentScreen).handleResult(m.correct, m.pin, m.reset, m.complete, m.close);
            return null;
        }
    }
}
