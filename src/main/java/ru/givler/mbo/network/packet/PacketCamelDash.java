package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.givler.mbo.entity.fauna.EntityMBOCamel;
import ru.givler.mbo.network.EditorPacketAccess;

/** Pose/cooldown notification; MF2's horse protocol validates the actual movement. */
public final class PacketCamelDash implements IMessage {
  private int id;

  public PacketCamelDash() {}

  public PacketCamelDash(EntityMBOCamel camel) {
    id = camel.getEntityId();
  }

  @Override
  public void fromBytes(ByteBuf bytes) {
    id = bytes.readInt();
  }

  @Override
  public void toBytes(ByteBuf bytes) {
    bytes.writeInt(id);
  }

  public static final class Handler implements IMessageHandler<PacketCamelDash, IMessage> {
    @Override
    public IMessage onMessage(final PacketCamelDash packet, final MessageContext context) {
      final EntityPlayerMP player = context.getServerHandler().playerEntity;
      EditorPacketAccess.schedule(
          context,
          new Runnable() {
            @Override
            public void run() {
              Entity animal = player.worldObj.getEntityByID(packet.id);
              if (animal instanceof EntityMBOCamel && animal.riddenByEntity == player) {
                EntityMBOCamel camel = (EntityMBOCamel) animal;
                if (camel.isTame()
                    && camel.isHorseSaddled()
                    && !camel.sitting()
                    && !camel.transitioning()) camel.beginDash();
              }
            }
          });
      return null;
    }
  }
}
