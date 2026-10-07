package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import ru.givler.mbo.client.render.fauna.ElderGuardianApparition;

/** The potion uses vanilla synchronization; this packet starts the client apparition. */
public final class PacketElderGuardianCurse implements IMessage {
  @Override
  public void fromBytes(ByteBuf buffer) {}

  @Override
  public void toBytes(ByteBuf buffer) {}

  public static final class Handler implements IMessageHandler<PacketElderGuardianCurse, IMessage> {
    @Override
    public IMessage onMessage(PacketElderGuardianCurse message, MessageContext context) {
      ClientHandler.show();
      return null;
    }
  }

  @SideOnly(Side.CLIENT)
  private static final class ClientHandler {
    static void show() {
      Minecraft.getMinecraft()
          .func_152344_a(
              new Runnable() {
                @Override
                public void run() {
                  Minecraft mc = Minecraft.getMinecraft();
                  if (mc.theWorld == null || mc.thePlayer == null) return;
                  mc.effectRenderer.addEffect(
                      new ElderGuardianApparition(mc.theWorld, mc.thePlayer));
                  mc.thePlayer.playSound("mbo:entity.elder_guardian.curse", 1, 1);
                }
              });
    }
  }
}
