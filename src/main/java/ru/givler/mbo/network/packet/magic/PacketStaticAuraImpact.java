package ru.givler.mbo.network.packet.magic;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;

/** Client visual emitted when static aura retaliates against an attacker. */
public final class PacketStaticAuraImpact implements IMessage {
  private int entityId;

  public PacketStaticAuraImpact() {}
  public PacketStaticAuraImpact(Entity entity) { entityId = entity.getEntityId(); }

  @Override public void toBytes(ByteBuf buffer) { buffer.writeInt(entityId); }
  @Override public void fromBytes(ByteBuf buffer) { entityId = buffer.readInt(); }

  public static final class Handler implements IMessageHandler<PacketStaticAuraImpact, IMessage> {
    @Override public IMessage onMessage(final PacketStaticAuraImpact message, MessageContext context) {
      ClientHandler.handle(message);
      return null;
    }
  }

  @SideOnly(Side.CLIENT)
  private static final class ClientHandler {
    static void handle(final PacketStaticAuraImpact message) {
      Minecraft.getMinecraft().func_152344_a(new Runnable() {
        @Override public void run() {
          World world = Minecraft.getMinecraft().theWorld;
          if (world == null) return;
          Entity target = world.getEntityByID(message.entityId);
          if (target == null) return;
          for (int i = 0; i < 8; i++) {
            double x = target.posX + world.rand.nextFloat() - 0.5D;
            double y = target.boundingBox.minY + target.height / 2.0D
                + world.rand.nextFloat() * 2.0D - 1.0D;
            double z = target.posZ + world.rand.nextFloat() - 0.5D;
            MoreBeyondOrdinary.proxy.spawnParticle(EnumParticleType.SPARK, world,
                x, y, z, 0, 0, 0, new ParticleSettings(3, 1, 1, 1, 1.4F, false));
            MoreBeyondOrdinary.proxy.spawnParticle(
                EnumParticleType.VANILLA_SMOKE, world, x, y, z, 0, 0, 0);
          }
        }
      });
    }
  }
}
