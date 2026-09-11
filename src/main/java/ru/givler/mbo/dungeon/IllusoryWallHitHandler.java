package ru.givler.mbo.dungeon;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** Turns the first ordinary left click into an activation instead of block damage. */
public final class IllusoryWallHitHandler {
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onInteract(PlayerInteractEvent event) {
    if (event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) return;
    boolean matched;
    if (event.world.isRemote) {
      DungeonAreaRecord area = ClientDungeonAreas.at(event.x, event.y, event.z);
      matched = area != null && area.getType() == 1 && !area.isActivated();
    } else {
      DungeonAreaSavedData data = DungeonAreaSavedData.get(event.world);
      DungeonAreaRecord area = data.at(event.x, event.y, event.z);
      matched = area != null && area.getType() == 1 && !area.isActivated();
      if (matched && area.activate(event.world)) data.changed(event.world);
    }
    if (matched) event.setCanceled(true);
  }
}
