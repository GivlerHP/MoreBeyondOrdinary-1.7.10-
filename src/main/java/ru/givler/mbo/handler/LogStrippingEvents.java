package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import ru.givler.mbo.registry.BlockRegistry;

/** Axe right-click stripping, after protection listeners have handled the interaction. */
public final class LogStrippingEvents {
    public static Block strippedBlock(Block block, int metadata) {
        if (block == Blocks.log) {
            switch (metadata & 3) {
                case 0: return BlockRegistry.DebarkedOak;
                case 1: return BlockRegistry.DebarkedSpruce;
                case 2: return BlockRegistry.DebarkedBirch;
                case 3: return BlockRegistry.DebarkedJungle;
            }
        }
        if (block == Blocks.log2) {
            if ((metadata & 3) == 0) return BlockRegistry.DebarkedAcacia;
            if ((metadata & 3) == 1) return BlockRegistry.DebarkedBigOak;
        }
        return null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void interact(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        ItemStack held = event.entityPlayer.getHeldItem();
        if (held == null || !(held.getItem() instanceof ItemAxe
                || held.getItem().getToolClasses(held).contains("axe"))) return;
        int metadata = event.world.getBlockMetadata(event.x, event.y, event.z);
        Block stripped = strippedBlock(event.world.getBlock(event.x, event.y, event.z), metadata);
        if (stripped == null) return;
        if (event.world.isRemote) {
            // Server swing packets exclude the acting player; animate their own hand locally.
            if (event.useBlock != Event.Result.DENY && event.useItem != Event.Result.DENY) {
                event.entityPlayer.swingItem();
            }
            return;
        }
        if (!BlockInteractionAccess.allowed(event)) return;
        if (!event.world.setBlock(event.x, event.y, event.z, stripped, metadata & 12, 3)) return;
        if (!event.entityPlayer.capabilities.isCreativeMode) held.damageItem(1, event.entityPlayer);
        event.entityPlayer.swingItem();
        event.world.playSoundEffect(event.x + .5, event.y + .5, event.z + .5, "dig.wood", 1, .8F);
        event.setCanceled(true);
    }
}
