package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ru.givler.mbo.core.DecorSleepHooks;

/** Keep suspended sleepers on their mattress after movement and network updates. */
public final class DecorSleepEvents {
 @SubscribeEvent public void onPlayerTick(TickEvent.PlayerTickEvent event) {
  if(event.phase==TickEvent.Phase.END)DecorSleepHooks.keepSleepingPosition(event.player);
 }
}
