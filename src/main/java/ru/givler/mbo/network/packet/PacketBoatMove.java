package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import ru.givler.mbo.entity.boat.EntityMBOBoat;
import ru.givler.mbo.network.EditorPacketAccess;

public class PacketBoatMove implements IMessage {
  private int dimension, entityId;
  private double x, y, z;
  private float yaw, pitch;

  public PacketBoatMove() {}

  public PacketBoatMove(Entity entity) {
    dimension = entity.worldObj.provider.dimensionId;
    entityId = entity.getEntityId();
    x = entity.posX;
    y = entity.posY;
    z = entity.posZ;
    yaw = entity.rotationYaw;
    pitch = entity.rotationPitch;
  }

  public void fromBytes(ByteBuf buf) {
    dimension = buf.readInt();
    entityId = buf.readInt();
    x = buf.readDouble();
    y = buf.readDouble();
    z = buf.readDouble();
    yaw = buf.readFloat();
    pitch = buf.readFloat();
  }

  public void toBytes(ByteBuf buf) {
    buf.writeInt(dimension);
    buf.writeInt(entityId);
    buf.writeDouble(x);
    buf.writeDouble(y);
    buf.writeDouble(z);
    buf.writeFloat(yaw);
    buf.writeFloat(pitch);
  }

  public static class Handler implements IMessageHandler<PacketBoatMove, IMessage> {
    public IMessage onMessage(final PacketBoatMove message, MessageContext context) {
      final net.minecraft.entity.player.EntityPlayerMP player = context.getServerHandler().playerEntity;
      final net.minecraft.network.NetHandlerPlayServer handler=context.getServerHandler();
      EditorPacketAccess.schedule(context,new Runnable(){@Override public void run(){apply(message,player,handler);}});return null;
    }
    private void apply(PacketBoatMove message,net.minecraft.entity.player.EntityPlayerMP player,net.minecraft.network.NetHandlerPlayServer handler){
      if (message.dimension != player.dimension
          || !finite(message.x)
          || !finite(message.y)
          || !finite(message.z)
          || Float.isNaN(message.yaw)
          || Float.isInfinite(message.yaw)
          || Float.isNaN(message.pitch)
          || Float.isInfinite(message.pitch)) return;
      WorldServer world = DimensionManager.getWorld(message.dimension);
      if (world == null) return;
      Entity boat = world.getEntityByID(message.entityId);
      if (!(boat instanceof EntityMBOBoat) || boat.riddenByEntity != player) return;
      if (((EntityMBOBoat) boat).isGuardianLocked()) {
        handler.sendPacket(new S18PacketEntityTeleport(boat));
        return;
      }
      double dx = message.x - boat.posX, dy = message.y - boat.posY, dz = message.z - boat.posZ;
      double allowed =
          Math.max(
              1.25D,
              Math.sqrt(
                          boat.motionX * boat.motionX
                              + boat.motionY * boat.motionY
                              + boat.motionZ * boat.motionZ)
                      * 2D
                  + .5D);
      long tick = world.getTotalWorldTime();
      net.minecraft.nbt.NBTTagCompound budget = boat.getEntityData();
      double spent = budget.hasKey("MBO_MoveTick") && budget.getLong("MBO_MoveTick") == tick
          ? budget.getDouble("MBO_MoveDistance") : 0D;
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (spent + distance > allowed
          || dx * dx + dy * dy + dz * dz > allowed * allowed
          || !world
              .getCollidingBoundingBoxes(boat, boat.boundingBox.getOffsetBoundingBox(dx, dy, dz))
              .isEmpty()) {
        handler.sendPacket(new S18PacketEntityTeleport(boat));
        return;
      }
      // Check the swept path using the same axis clipping as ordinary entity movement.
      java.util.List obstacles = world.getCollidingBoundingBoxes(boat, boat.boundingBox.addCoord(dx, dy, dz));
      net.minecraft.util.AxisAlignedBB swept = boat.boundingBox.copy();
      double clippedY = dy, clippedX = dx, clippedZ = dz;
      for (Object obstacle : obstacles) clippedY = ((net.minecraft.util.AxisAlignedBB) obstacle).calculateYOffset(swept, clippedY);
      swept.offset(0, clippedY, 0);
      for (Object obstacle : obstacles) clippedX = ((net.minecraft.util.AxisAlignedBB) obstacle).calculateXOffset(swept, clippedX);
      swept.offset(clippedX, 0, 0);
      for (Object obstacle : obstacles) clippedZ = ((net.minecraft.util.AxisAlignedBB) obstacle).calculateZOffset(swept, clippedZ);
      if (Math.abs(clippedX - dx) > 1E-6 || Math.abs(clippedY - dy) > 1E-6 || Math.abs(clippedZ - dz) > 1E-6) {
        handler.sendPacket(new S18PacketEntityTeleport(boat));
        return;
      }
      budget.setLong("MBO_MoveTick", tick);
      budget.setDouble("MBO_MoveDistance", spent + distance);
      boat.setPositionAndRotation(message.x, message.y, message.z, message.yaw, message.pitch);
    }

    private static boolean finite(double value) {
      return !Double.isNaN(value) && !Double.isInfinite(value);
    }
  }
}
