package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import ru.givler.mbo.data.world.TrapdoorLatchData;
import ru.givler.mbo.client.state.ClientTrapdoorLatches;
import ru.givler.mbo.network.packet.PacketTrapdoorLatchSnapshot;

public final class TrapdoorLatchEvents {
  @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
  public void interact(PlayerInteractEvent event) {
    if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
        || !(event.world.getBlock(event.x, event.y, event.z) instanceof BlockTrapDoor)) return;
    ItemStack held = event.entityPlayer.getCurrentEquippedItem();
    boolean spanner = isMineFantasySpanner(held);
    if (event.world.isRemote) {
      if (spanner) event.entityPlayer.swingItem();
      else if (!event.entityPlayer.isSneaking()
          && ClientTrapdoorLatches.contains(
          event.world.provider.dimensionId, event.x, event.y, event.z)) event.setCanceled(true);
      return;
    }
    TrapdoorLatchData data = TrapdoorLatchData.get(event.world);
    if (spanner) {
      event.setCanceled(true);
      event.entityPlayer.swingItem();
      boolean latched = data.toggle(event.world, event.x, event.y, event.z);
      event.world.playSoundEffect(event.x + .5D, event.y + .5D, event.z + .5D,
          "random.click", .8F, latched ? .7F : 1.2F);
      event.entityPlayer.addChatMessage(new ChatComponentTranslation(
          latched ? "mbo.trapdoor.latched" : "mbo.trapdoor.unlatched"));
      return;
    }
    if (!event.entityPlayer.isSneaking() && data.contains(event.x, event.y, event.z)) {
      event.setCanceled(true);
      event.world.playSoundEffect(event.x + .5D, event.y + .5D, event.z + .5D,
          "random.click", .45F, .55F);
    }
  }

  @SubscribeEvent public void breakBlock(BlockEvent.BreakEvent event) {
    if (!event.world.isRemote && event.block instanceof BlockTrapDoor)
      TrapdoorLatchData.get(event.world).clear(event.world, event.x, event.y, event.z);
  }

  @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event) {
    if (event.player instanceof EntityPlayerMP) PacketTrapdoorLatchSnapshot.send((EntityPlayerMP) event.player);
  }

  @SubscribeEvent public void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
    if (event.player instanceof EntityPlayerMP) PacketTrapdoorLatchSnapshot.send((EntityPlayerMP) event.player);
  }

  private static boolean isMineFantasySpanner(ItemStack stack) {
    if (stack == null) return false;
    GameRegistry.UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(stack.getItem());
    return id != null && "minefantasy2".equalsIgnoreCase(id.modId)
        && "standard_spanner".equalsIgnoreCase(id.name);
  }
}
