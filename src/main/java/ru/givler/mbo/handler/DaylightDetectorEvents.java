package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.BlockDaylightDetector;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import ru.givler.mbo.registry.BlockRegistry;

public final class DaylightDetectorEvents {
  @SubscribeEvent
  public void onInteract(PlayerInteractEvent event) {
    if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
        || event.world.getBlock(event.x, event.y, event.z) != Blocks.daylight_detector) return;
    if (event.world.isRemote) return;

    event.setCanceled(true);
    int power = event.world.getBlockMetadata(event.x, event.y, event.z);
    event.world.setBlock(
        event.x, event.y, event.z, BlockRegistry.InvertedDaylightDetector, power, 3);
    ((BlockDaylightDetector) BlockRegistry.InvertedDaylightDetector)
        .func_149957_e(event.world, event.x, event.y, event.z);
  }
}
