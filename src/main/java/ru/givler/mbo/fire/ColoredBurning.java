package ru.givler.mbo.fire;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketColoredBurning;

/** Tracks which admin-fire color ignited an entity while its vanilla burn timer runs. */
public final class ColoredBurning {
  private static final String COLOR = "mboFireColor";
  private static final String UNTIL = "mboFireUntil";
  private static final String SYNC = "mboFireSync";

  public static void onFireContact(Entity entity, int color) {
    if (entity.worldObj.isRemote) {
      MoreBeyondOrdinary.proxy.recordColoredFire(entity, color, 160);
      return;
    }
    NBTTagCompound data = entity.getEntityData();
    long now = entity.worldObj.getTotalWorldTime();
    int oldColor = data.getLong(UNTIL) > now ? data.getInteger(COLOR) : -1;
    long lastSync = data.getLong(SYNC);
    if (color < 0) {
      data.setLong(UNTIL, 0);
    } else {
      data.setInteger(COLOR, color);
      data.setLong(UNTIL, now + 160);
    }
    if (oldColor != color || color >= 0 && now - lastSync >= 20) {
      data.setLong(SYNC, now);
      PacketManager.INSTANCE.sendToAllAround(new PacketColoredBurning(entity, color,
          color < 0 ? 0 : 160), new NetworkRegistry.TargetPoint(
              entity.dimension, entity.posX, entity.posY, entity.posZ, 96));
    }
  }

  @SubscribeEvent
  public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
    Entity entity = event.entityLiving;
    if (entity.worldObj.isRemote) return;
    NBTTagCompound data = entity.getEntityData();
    long until = data.getLong(UNTIL);
    if (until <= 0) return;
    long now = entity.worldObj.getTotalWorldTime();
    if (entity.isBurning() && until > now) {
      if (now - data.getLong(SYNC) >= 20) {
        data.setLong(SYNC, now);
        PacketManager.INSTANCE.sendToAllAround(new PacketColoredBurning(entity,
            data.getInteger(COLOR), (int) Math.min(160, until - now)),
            new NetworkRegistry.TargetPoint(
                entity.dimension, entity.posX, entity.posY, entity.posZ, 96));
      }
      return;
    }
    data.setLong(UNTIL, 0);
    PacketManager.INSTANCE.sendToAllAround(new PacketColoredBurning(entity, -1, 0),
        new NetworkRegistry.TargetPoint(
            entity.dimension, entity.posX, entity.posY, entity.posZ, 96));
  }

  @SubscribeEvent
  public void onStartTracking(PlayerEvent.StartTracking event) {
    if (!(event.entityPlayer instanceof EntityPlayerMP)) return;
    Entity target = event.target;
    NBTTagCompound data = target.getEntityData();
    long remaining = data.getLong(UNTIL) - target.worldObj.getTotalWorldTime();
    if (remaining > 0 && target.isBurning()) {
      PacketManager.INSTANCE.sendTo(new PacketColoredBurning(target,
          data.getInteger(COLOR), (int) Math.min(160, remaining)),
          (EntityPlayerMP) event.entityPlayer);
    }
  }
}
