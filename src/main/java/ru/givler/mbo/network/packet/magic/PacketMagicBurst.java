package ru.givler.mbo.network.packet.magic;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;

/** Client-bound description of a coloured sparkle burst around an entity. */
public final class PacketMagicBurst implements IMessage {
  private int entityId;
  private int count;
  private float red;
  private float green;
  private float blue;

  public PacketMagicBurst() {}

  public PacketMagicBurst(EntityLivingBase entity, int count, float red, float green, float blue) {
    this.entityId = entity.getEntityId();
    this.count = count;
    this.red = red;
    this.green = green;
    this.blue = blue;
  }

  @Override
  public void toBytes(ByteBuf buffer) {
    buffer.writeInt(entityId);
    buffer.writeByte(count);
    buffer.writeFloat(red);
    buffer.writeFloat(green);
    buffer.writeFloat(blue);
  }

  @Override
  public void fromBytes(ByteBuf buffer) {
    entityId = buffer.readInt();
    count = buffer.readUnsignedByte();
    red = buffer.readFloat();
    green = buffer.readFloat();
    blue = buffer.readFloat();
  }

  public static final class Handler implements IMessageHandler<PacketMagicBurst, IMessage> {
    @Override
    public IMessage onMessage(final PacketMagicBurst message, MessageContext context) {
      ClientHandler.handle(message);
      return null;
    }
  }

  @SideOnly(Side.CLIENT)
  private static final class ClientHandler {
    private static void handle(final PacketMagicBurst message) {
      Minecraft.getMinecraft().func_152344_a(new Runnable() {
        @Override
        public void run() {
          World world = Minecraft.getMinecraft().theWorld;
          if (world == null) return;
          Entity entity = world.getEntityByID(message.entityId);
          if (!(entity instanceof EntityLivingBase)) return;
          EntityLivingBase living = (EntityLivingBase) entity;
          for (int i = 0; i < message.count; i++) {
            double x = living.posX + (world.rand.nextDouble() - 0.5D) * living.width * 2.0D;
            double y = living.posY + world.rand.nextDouble() * living.height;
            double z = living.posZ + (world.rand.nextDouble() - 0.5D) * living.width * 2.0D;
            MoreBeyondOrdinary.proxy.spawnSparkle(
                world, x, y, z, 0.0D, 0.1D, 0.0D,
                48 + world.rand.nextInt(12), message.red, message.green, message.blue);
          }
        }
      });
    }
  }
}
