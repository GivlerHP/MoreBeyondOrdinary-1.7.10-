package ru.givler.mbo.dungeon;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.BlockEvent;

/** Prevents real blocks from being placed into occupied cells of a phantom snapshot. */
public final class DungeonAreaProtectionHandler {
    @SubscribeEvent public void onPlace(BlockEvent.PlaceEvent event){
        if(event.world==null||event.world.isRemote)return;
        if(DungeonAreaSavedData.get(event.world).at(event.x,event.y,event.z)!=null)event.setCanceled(true);
    }
}
