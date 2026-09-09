package ru.givler.mbo.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import ru.givler.mbo.movingplatform.MovingPlatformTickHandler;

public final class MovingPlatformClientTickHandler {
    @SubscribeEvent public void onClientTick(TickEvent.ClientTickEvent event){
        if(event.phase==TickEvent.Phase.END&&Minecraft.getMinecraft().theWorld!=null&&!Minecraft.getMinecraft().isGamePaused())
            MovingPlatformTickHandler.processClientPlayer(Minecraft.getMinecraft().theWorld,Minecraft.getMinecraft().thePlayer);
    }
}
