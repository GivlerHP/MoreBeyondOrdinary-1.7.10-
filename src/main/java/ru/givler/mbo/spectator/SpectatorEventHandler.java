package ru.givler.mbo.spectator;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

public final class SpectatorEventHandler {
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && SpectatorManager.isSpectator(event.player)) {
            SpectatorManager.apply(event.player);
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        SpectatorManager.syncTo(player);
        if (SpectatorManager.isSpectator(player)) SpectatorManager.apply(player);
    }

    @SubscribeEvent
    public void onAttack(LivingAttackEvent event) {
        if (event.entityLiving instanceof net.minecraft.entity.player.EntityPlayer
                && SpectatorManager.isSpectator((net.minecraft.entity.player.EntityPlayer) event.entityLiving)) {
            event.setCanceled(true);
            return;
        }
        if (event.source.getEntity() instanceof net.minecraft.entity.player.EntityPlayer
                && SpectatorManager.isSpectator((net.minecraft.entity.player.EntityPlayer) event.source.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent public void onInteract(PlayerInteractEvent event) {
        if (SpectatorManager.isSpectator(event.entityPlayer)) event.setCanceled(true);
    }

    @SubscribeEvent public void onEntityInteract(EntityInteractEvent event) {
        if (SpectatorManager.isSpectator(event.entityPlayer)) event.setCanceled(true);
    }

    @SubscribeEvent public void onBreak(BlockEvent.BreakEvent event) {
        if (SpectatorManager.isSpectator(event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent public void onPlace(BlockEvent.PlaceEvent event) {
        if (SpectatorManager.isSpectator(event.player)) event.setCanceled(true);
    }

    @SubscribeEvent public void onToss(ItemTossEvent event) {
        if (SpectatorManager.isSpectator(event.player)) event.setCanceled(true);
    }

    @SubscribeEvent public void onPickup(EntityItemPickupEvent event) {
        if (SpectatorManager.isSpectator(event.entityPlayer)) event.setCanceled(true);
    }
}
