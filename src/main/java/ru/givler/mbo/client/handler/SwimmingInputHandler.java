package ru.givler.mbo.client.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** Allows the sprint key to start swimming while the player is submerged. */
public final class SwimmingInputHandler {
  @SubscribeEvent
  public void onPlace(PlayerInteractEvent event) {
    if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || !event.world.isRemote
        || !ru.givler.mbo.swimming.SwimmingHooks.isSwimming(event.entityPlayer)) return;
    ItemStack held = event.entityPlayer.getHeldItem();
    if (held != null && held.getItem() instanceof ItemBlock) event.entityPlayer.swingItem();
  }

  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent event) {
    if (event.phase != TickEvent.Phase.START) return;
    Minecraft minecraft = Minecraft.getMinecraft();
    EntityPlayerSP player = minecraft.thePlayer;
    if (player == null || minecraft.currentScreen != null || player.isSprinting()) return;
    if (minecraft.gameSettings.keyBindSprint.getIsKeyPressed()
        && minecraft.gameSettings.keyBindForward.getIsKeyPressed()
        && player.getFoodStats().getFoodLevel() > 6
        && !player.isPotionActive(Potion.blindness)
        && !player.capabilities.isFlying && !player.isRiding()
        && player.isInsideOfMaterial(net.minecraft.block.material.Material.water)) {
      player.setSprinting(true);
    }
  }
}
