package ru.givler.mbo.movingplatform;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import ru.givler.mbo.item.ItemPlatformEditor;
import ru.givler.mbo.registry.ItemRegistry;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketPlatformInteract;

public final class PlatformEditorInteractionHandler {
    @SubscribeEvent(priority=EventPriority.HIGHEST,receiveCanceled=true) public void onInteract(PlayerInteractEvent event) {
        if(event==null||event.entityPlayer==null)return;
        ItemStack held=event.entityPlayer.getCurrentEquippedItem();
        if(held==null||held.getItem()!=ItemRegistry.PlatformEditor)return;
        if(event.action!=PlayerInteractEvent.Action.LEFT_CLICK_BLOCK&&event.action!=PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)return;

        event.setCanceled(true);
        event.useBlock=Event.Result.DENY;
        event.useItem=Event.Result.DENY;
        // The digging packet creates a second PlayerInteractEvent on the logical server.
        // It must be cancelled there too, otherwise creative mode removes the block even
        // though the client-side click was already consumed by the editor.
        if(!event.world.isRemote)return;
        ItemPlatformEditor editor=(ItemPlatformEditor)held.getItem();
        if(event.action==PlayerInteractEvent.Action.LEFT_CLICK_BLOCK){
            editor.selectFirst(held,event.x,event.y,event.z,event.entityPlayer);
            PacketManager.INSTANCE.sendToServer(new PacketPlatformInteract(PacketPlatformInteract.FIRST,event.x,event.y,event.z));
        }else{
            boolean open=event.entityPlayer.isSneaking();
            if(!open)editor.selectSecondOrOpen(held,event.entityPlayer,event.world,event.x,event.y,event.z,false);
            PacketManager.INSTANCE.sendToServer(new PacketPlatformInteract(open?PacketPlatformInteract.OPEN:PacketPlatformInteract.SECOND,event.x,event.y,event.z));
        }
    }
}
