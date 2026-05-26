package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketActivateAmulet;
import ru.givler.mbo.proxy.ClientProxy;

public class ClientKeyHandler {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getMinecraft().currentScreen != null) return;

        if (ClientProxy.activateAmuletKey.isPressed()) {
            PacketManager.INSTANCE.sendToServer(new PacketActivateAmulet());
        }
    }
}