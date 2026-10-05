package ru.givler.mbo.command;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.givler.mbo.core.TickRateHooks;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketTickRate;

public final class TickRateEvents {
  @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event) {
    if (event.player instanceof EntityPlayerMP)
      PacketManager.INSTANCE.sendTo(new PacketTickRate(TickRateHooks.getServerRate()), (EntityPlayerMP) event.player);
  }

  @SubscribeEvent public void connect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
    TickRateHooks.setClientRate(20);
  }

  @SubscribeEvent public void disconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
    TickRateHooks.setClientRate(20);
  }
}
