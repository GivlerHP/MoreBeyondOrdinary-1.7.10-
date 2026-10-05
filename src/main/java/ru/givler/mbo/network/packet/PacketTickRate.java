package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import ru.givler.mbo.core.TickRateHooks;

public final class PacketTickRate implements IMessage {
  private int rate;
  public PacketTickRate() { }
  public PacketTickRate(int rate) { this.rate = rate; }
  @Override public void fromBytes(ByteBuf buf) { rate = buf.readInt(); }
  @Override public void toBytes(ByteBuf buf) { buf.writeInt(rate); }

  public static final class Handler implements IMessageHandler<PacketTickRate, IMessage> {
    @Override public IMessage onMessage(PacketTickRate message, MessageContext context) {
      // A single volatile value is safe to publish from Netty and is read by the timer.
      TickRateHooks.setClientRate(message.rate);
      return null;
    }
  }
}
