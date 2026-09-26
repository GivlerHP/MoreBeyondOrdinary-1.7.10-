package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.network.EditorPacketAccess;

/** Synchronizes the color of an entity's existing fire overlay. */
public final class PacketColoredBurning implements IMessage {
  private int entityId;
  private int color;
  private int remaining;

  public PacketColoredBurning() {}

  public PacketColoredBurning(Entity entity, int color, int remaining) {
    this.entityId = entity.getEntityId();
    this.color = color;
    this.remaining = remaining;
  }

  @Override public void fromBytes(ByteBuf buffer) {
    entityId = buffer.readInt();
    color = buffer.readByte();
    remaining = buffer.readUnsignedShort();
  }

  @Override public void toBytes(ByteBuf buffer) {
    buffer.writeInt(entityId);
    buffer.writeByte(color);
    buffer.writeShort(remaining);
  }

  public static final class Handler implements IMessageHandler<PacketColoredBurning, IMessage> {
    @Override public IMessage onMessage(final PacketColoredBurning packet, MessageContext context) {
      EditorPacketAccess.scheduleClient(new Runnable() {
        @Override public void run() {
          net.minecraft.world.World world = MoreBeyondOrdinary.proxy.getClientWorld();
          if (world == null) return;
          Entity entity = world.getEntityByID(packet.entityId);
          if (entity != null) MoreBeyondOrdinary.proxy.recordColoredFire(
              entity, packet.color, packet.remaining);
        }
      });
      return null;
    }
  }
}
