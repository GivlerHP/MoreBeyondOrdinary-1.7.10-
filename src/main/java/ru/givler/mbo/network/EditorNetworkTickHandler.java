package ru.givler.mbo.network;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public final class EditorNetworkTickHandler {
    @SubscribeEvent public void serverTick(TickEvent.ServerTickEvent event){if(event.phase==TickEvent.Phase.END)EditorPacketAccess.drainServer();}
    @SubscribeEvent public void clientTick(TickEvent.ClientTickEvent event){if(event.phase==TickEvent.Phase.END)EditorPacketAccess.drainClient();}
}
