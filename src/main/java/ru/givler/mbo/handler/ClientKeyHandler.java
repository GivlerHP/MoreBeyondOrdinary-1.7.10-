package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import ru.givler.mbo.proxy.ClientProxy;
import ru.givler.mbo.proxy.CommonProxy;

public class ClientKeyHandler {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getMinecraft().currentScreen != null) return;

        if (ClientProxy.activateAmuletKey.isPressed()) {
                CommonProxy.NETWORK.sendToServer(new PacketActivateAmulet());
        }
    }
}