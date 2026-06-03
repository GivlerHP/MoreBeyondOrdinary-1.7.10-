package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.network.PacketManager;

public class PacketSpawnParticle implements IMessage {

    private EnumParticleType type;
    private double x, y, z;
    private double motX, motY, motZ;

    public PacketSpawnParticle() {}

    public PacketSpawnParticle(EnumParticleType type,
                               double x, double y, double z,
                               double motX, double motY, double motZ) {
        this.type = type;
        this.x = x; this.y = y; this.z = z;
        this.motX = motX; this.motY = motY; this.motZ = motZ;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(type.ordinal());
        buf.writeDouble(x);    buf.writeDouble(y);    buf.writeDouble(z);
        buf.writeDouble(motX); buf.writeDouble(motY); buf.writeDouble(motZ);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        type = EnumParticleType.byOrdinal(buf.readInt());
        x    = buf.readDouble(); y    = buf.readDouble(); z    = buf.readDouble();
        motX = buf.readDouble(); motY = buf.readDouble(); motZ = buf.readDouble();
    }

    // Геттеры для Handler-а
    public EnumParticleType getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getMotX() { return motX; }
    public double getMotY() { return motY; }
    public double getMotZ() { return motZ; }

    public static void send(EnumParticleType type, World world, EntityPlayer player,
                            int count,
                            double spreadX, double spreadY, double spreadZ, double offsetY,
                            double motX, double motY, double motZ) {
        System.out.println("[DEBUG] send() вызван на isRemote=" + world.isRemote + " игрок=" + player.getCommandSenderName());
        NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(
                player.dimension,
                player.posX, player.posY, player.posZ, 64.0
        );

        double baseY = player.posY + offsetY;

        for (int i = 0; i < count; i++) {
            double ox = (world.rand.nextDouble() - 0.5) * spreadX;
            double oy = (world.rand.nextDouble() - 0.5) * spreadY;
            double oz = (world.rand.nextDouble() - 0.5) * spreadZ;

            PacketSpawnParticle pkt = new PacketSpawnParticle(
                    type,
                    player.posX + ox,
                    baseY + oy,
                    player.posZ + oz,
                    motX, motY, motZ
            );

            PacketManager.INSTANCE.sendToAllAround(pkt, point);
            PacketManager.INSTANCE.sendTo(pkt, (EntityPlayerMP) player);
        }
    }
}