package ru.givler.mbo.swimming;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Random;

public final class SwimmingEvents {
  @SubscribeEvent
  public void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if (event.phase == TickEvent.Phase.START) SwimmingHooks.updateSize(event.player);
    if (event.phase == TickEvent.Phase.END && event.player.worldObj.isRemote
        && SwimmingHooks.isSwimming(event.player)) spawnBubbles(event.player);
  }

  private static void spawnBubbles(EntityPlayer player) {
    double speed = Math.sqrt(player.motionX * player.motionX + player.motionY * player.motionY
        + player.motionZ * player.motionZ);
    if (speed < 0.025D) return;
    Random random = player.worldObj.rand;
    for (int i = 0; i < 2; i++) {
      double x = player.posX - player.motionX * 3.0D + (random.nextDouble() - 0.5D) * 0.35D;
      double y = player.boundingBox.minY + 0.15D + random.nextDouble() * 0.3D;
      double z = player.posZ - player.motionZ * 3.0D + (random.nextDouble() - 0.5D) * 0.35D;
      player.worldObj.spawnParticle("bubble", x, y, z,
          -player.motionX * 0.1D, 0.01D, -player.motionZ * 0.1D);
    }
  }
}
