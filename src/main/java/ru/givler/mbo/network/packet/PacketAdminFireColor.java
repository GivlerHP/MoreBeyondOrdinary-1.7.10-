package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.editor.BuilderAccess;
import ru.givler.mbo.network.EditorPacketAccess;
import ru.givler.mbo.registry.ItemRegistry;

public final class PacketAdminFireColor implements IMessage {
  private int color;

  public PacketAdminFireColor() {}
  public PacketAdminFireColor(int color) { this.color = color; }
  @Override public void fromBytes(ByteBuf buf) { color = buf.readUnsignedByte(); }
  @Override public void toBytes(ByteBuf buf) { buf.writeByte(color); }

  public static final class Handler implements IMessageHandler<PacketAdminFireColor, IMessage> {
    @Override
    public IMessage onMessage(final PacketAdminFireColor message, MessageContext context) {
      final EntityPlayerMP player = context.getServerHandler().playerEntity;
      EditorPacketAccess.schedule(context, new Runnable() {
        @Override public void run() {
          ItemStack held = player.getCurrentEquippedItem();
          if (message.color >= 0 && message.color <= 16
              && BuilderAccess.canUseTool(player, ItemRegistry.AdminLighter)
              && held != null && held.getItem() == ItemRegistry.AdminLighter)
            held.setItemDamage(message.color == 16 ? 0 : message.color + 1);
        }
      });
      return null;
    }
  }
}
