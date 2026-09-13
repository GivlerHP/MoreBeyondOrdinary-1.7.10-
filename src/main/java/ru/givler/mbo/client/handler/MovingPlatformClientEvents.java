package ru.givler.mbo.client.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import ru.givler.mbo.movingplatform.MovingPlatformTickHandler;

public final class MovingPlatformClientEvents {
  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent event) {
    Minecraft minecraft = Minecraft.getMinecraft();
    if (event.phase == TickEvent.Phase.END
        && minecraft.theWorld != null
        && !minecraft.isGamePaused())
      MovingPlatformTickHandler.processClientPlayer(minecraft.theWorld, minecraft.thePlayer);
  }
}
