package ru.givler.mbo.network.packet.magic;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;

/** Client-side jagged lightning line between two world positions. */
public final class PacketLightningArc implements IMessage {
  private double startX, startY, startZ, endX, endY, endZ;

  public PacketLightningArc() {}

  public PacketLightningArc(double startX, double startY, double startZ,
      double endX, double endY, double endZ) {
    this.startX = startX;
    this.startY = startY;
    this.startZ = startZ;
    this.endX = endX;
    this.endY = endY;
    this.endZ = endZ;
  }

  @Override public void toBytes(ByteBuf b) {
    b.writeDouble(startX); b.writeDouble(startY); b.writeDouble(startZ);
    b.writeDouble(endX); b.writeDouble(endY); b.writeDouble(endZ);
  }

  @Override public void fromBytes(ByteBuf b) {
    startX = b.readDouble(); startY = b.readDouble(); startZ = b.readDouble();
    endX = b.readDouble(); endY = b.readDouble(); endZ = b.readDouble();
  }

  public static final class Handler implements IMessageHandler<PacketLightningArc, IMessage> {
    @Override public IMessage onMessage(final PacketLightningArc message, MessageContext context) {
      ClientHandler.handle(message);
      return null;
    }
  }

  @SideOnly(Side.CLIENT)
  private static final class ClientHandler {
    static void handle(final PacketLightningArc message) {
      Minecraft.getMinecraft().func_152344_a(new Runnable() {
        @Override public void run() {
          World world = Minecraft.getMinecraft().theWorld;
          if (world == null) return;
          int segments = Math.max(8, (int) (distance(message) * 5));
          for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            double taper = Math.sin(Math.PI * t) * 0.12D;
            double x = lerp(message.startX, message.endX, t) + (world.rand.nextDouble() - 0.5D) * taper;
            double y = lerp(message.startY, message.endY, t) + (world.rand.nextDouble() - 0.5D) * taper;
            double z = lerp(message.startZ, message.endZ, t) + (world.rand.nextDouble() - 0.5D) * taper;
            MoreBeyondOrdinary.proxy.spawnSparkle(world, x, y, z, 0, 0, 0,
                4 + world.rand.nextInt(3), 0.35F, 0.65F, 1.0F);
          }
        }
      });
    }

    private static double distance(PacketLightningArc p) {
      double x = p.endX - p.startX, y = p.endY - p.startY, z = p.endZ - p.startZ;
      return Math.sqrt(x * x + y * y + z * z);
    }

    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }
  }
}
