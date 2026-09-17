package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** Explicitly mirrors dynamically allocated MBO potion effects to tracking clients. */
public final class PacketPotionEffectSync implements IMessage {
  private int entityId;
  private int potionId;
  private int duration;
  private int amplifier;
  private boolean ambient;

  public PacketPotionEffectSync() {}

  public PacketPotionEffectSync(EntityLivingBase entity, PotionEffect effect) {
    entityId = entity.getEntityId();
    potionId = effect.getPotionID();
    duration = effect.getDuration();
    amplifier = effect.getAmplifier();
    ambient = effect.getIsAmbient();
  }

  @Override public void toBytes(ByteBuf buffer) {
    buffer.writeInt(entityId);
    buffer.writeInt(potionId);
    buffer.writeInt(duration);
    buffer.writeByte(amplifier);
    buffer.writeBoolean(ambient);
  }

  @Override public void fromBytes(ByteBuf buffer) {
    entityId = buffer.readInt();
    potionId = buffer.readInt();
    duration = buffer.readInt();
    amplifier = buffer.readUnsignedByte();
    ambient = buffer.readBoolean();
  }

  public static final class Handler implements IMessageHandler<PacketPotionEffectSync, IMessage> {
    @Override public IMessage onMessage(final PacketPotionEffectSync message, MessageContext context) {
      ClientHandler.handle(message);
      return null;
    }
  }

  @SideOnly(Side.CLIENT)
  private static final class ClientHandler {
    static void handle(final PacketPotionEffectSync message) {
      Minecraft.getMinecraft().func_152344_a(new Runnable() {
        @Override public void run() {
          World world = Minecraft.getMinecraft().theWorld;
          if (world == null) return;
          Entity entity = world.getEntityByID(message.entityId);
          if (entity instanceof EntityLivingBase) {
            ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(
                message.potionId, message.duration, message.amplifier, message.ambient));
          }
        }
      });
    }
  }
}
