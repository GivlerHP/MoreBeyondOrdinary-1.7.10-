package ru.givler.mbo.editor;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import ru.givler.mbo.item.ItemAreaEditor;

/** Prevents every area editor from breaking or activating the selected block. */
public final class AreaEditorInteractionHandler {
    @SubscribeEvent(priority=EventPriority.HIGHEST,receiveCanceled=true)
    public void onInteract(PlayerInteractEvent event){
        if(event==null||event.entityPlayer==null)return;ItemStack held=event.entityPlayer.getCurrentEquippedItem();
        if(held==null||!(held.getItem() instanceof ItemAreaEditor))return;
        if(event.action!=PlayerInteractEvent.Action.LEFT_CLICK_BLOCK&&event.action!=PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)return;
        event.setCanceled(true);event.useBlock=Event.Result.DENY;event.useItem=Event.Result.DENY;
    }
}
