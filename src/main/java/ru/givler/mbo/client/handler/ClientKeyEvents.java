package ru.givler.mbo.client.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketActivateAmulet;
import ru.givler.mbo.proxy.ClientProxy;

public class ClientKeyEvents {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getMinecraft().currentScreen != null) return;

        if (Minecraft.getMinecraft().thePlayer == null) return;
        if (ClientProxy.activateBeltKey.isPressed()) {
            PacketManager.INSTANCE.sendToServer(new ru.givler.mbo.network.packet.PacketActivateBelt());
        }
        if (ClientProxy.activateAmuletKey.isPressed()) {
            PacketManager.INSTANCE.sendToServer(new PacketActivateAmulet());
        }
    }
}
