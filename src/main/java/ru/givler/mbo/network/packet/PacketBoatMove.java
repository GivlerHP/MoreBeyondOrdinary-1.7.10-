package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class PacketBoatMove implements IMessage {
    private int dimension, entityId;
    private double x, y, z;
    private float yaw, pitch;

    public PacketBoatMove() { }
    public PacketBoatMove(Entity entity) {
        dimension=entity.worldObj.provider.dimensionId; entityId=entity.getEntityId();
        x=entity.posX; y=entity.posY; z=entity.posZ; yaw=entity.rotationYaw; pitch=entity.rotationPitch;
    }
    public void fromBytes(ByteBuf buf) {
        dimension=buf.readInt(); entityId=buf.readInt();
        x=buf.readDouble(); y=buf.readDouble(); z=buf.readDouble();
        yaw=buf.readFloat(); pitch=buf.readFloat();
    }
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimension); buf.writeInt(entityId);
        buf.writeDouble(x); buf.writeDouble(y); buf.writeDouble(z);
        buf.writeFloat(yaw); buf.writeFloat(pitch);
    }

    public static class Handler implements IMessageHandler<PacketBoatMove, IMessage> {
        public IMessage onMessage(PacketBoatMove message, MessageContext context) {
            WorldServer world= DimensionManager.getWorld(message.dimension);
            if (world==null) return null;
            Entity boat=world.getEntityByID(message.entityId);
            if (boat==null || boat.riddenByEntity!=context.getServerHandler().playerEntity) return null;
            double dx=message.x-boat.posX,dy=message.y-boat.posY,dz=message.z-boat.posZ;
            if (dx*dx+dy*dy+dz*dz>100D) {
                context.getServerHandler().sendPacket(new S18PacketEntityTeleport(boat));
                return null;
            }
            boat.setPositionAndRotation(message.x,message.y,message.z,message.yaw,message.pitch);
            return null;
        }
    }
}
