package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockWall;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import ru.givler.mbo.data.world.FenceConnectionData;
import ru.givler.mbo.network.packet.PacketFenceConnectionSnapshot;

public final class FenceConnectionEvents {
  @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
  public void interact(PlayerInteractEvent event) {
    if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
    Block block = event.world.getBlock(event.x, event.y, event.z);
    if (!isFenceOrWall(block) || !isMineFantasySaw(event.entityPlayer.getCurrentEquippedItem())) return;
    MovingObjectPosition hit = event.entityPlayer.rayTrace(6D, 1F);
    float hitX = hit == null || hit.hitVec == null ? .5F : (float) (hit.hitVec.xCoord - event.x);
    float hitZ = hit == null || hit.hitVec == null ? .5F : (float) (hit.hitVec.zCoord - event.z);
    int side = horizontalSide(event.face, hitX, hitZ);
    if (side < 2) return;
    if (event.world.isRemote) {
      event.entityPlayer.swingItem();
      return;
    }
    event.setCanceled(true);
    event.entityPlayer.swingItem();

    int dx = side == 4 ? -1 : side == 5 ? 1 : 0;
    int dz = side == 2 ? -1 : side == 3 ? 1 : 0;
    FenceConnectionData data = FenceConnectionData.get(event.world);
    byte before = data.mask(event.x, event.y, event.z);
    boolean disable = (before & FenceConnectionData.bit(side)) == 0;
    data.set(event.world, event.x, event.y, event.z, side, disable);
    int nx = event.x + dx, nz = event.z + dz;
    if (isFenceOrWall(event.world.getBlock(nx, event.y, nz)))
      data.set(event.world, nx, event.y, nz, FenceConnectionData.opposite(side), disable);
    event.world.markBlockRangeForRenderUpdate(event.x - 1, event.y, event.z - 1, event.x + 1, event.y + 1, event.z + 1);
    event.world.playSoundEffect(event.x + .5D, event.y + .5D, event.z + .5D, "minefantasy2:block.sawcarpenter", 1F, disable ? .9F : 1.1F);
    ItemStack saw = event.entityPlayer.getCurrentEquippedItem();
    if (!event.entityPlayer.capabilities.isCreativeMode) saw.damageItem(1, event.entityPlayer);
    event.entityPlayer.addChatMessage(
        new ChatComponentTranslation(
            disable ? "mbo.fence.connection.disabled" : "mbo.fence.connection.enabled"));
  }

  @SubscribeEvent public void breakBlock(BlockEvent.BreakEvent event) {
    if (event.world.isRemote || !isFenceOrWall(event.block)) return;
    FenceConnectionData data = FenceConnectionData.get(event.world);
    data.clear(event.world, event.x, event.y, event.z);
    for (int side = 2; side <= 5; side++) {
      int nx = event.x + (side == 4 ? -1 : side == 5 ? 1 : 0);
      int nz = event.z + (side == 2 ? -1 : side == 3 ? 1 : 0);
      data.set(event.world, nx, event.y, nz, FenceConnectionData.opposite(side), false);
    }
  }

  @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event) {
    if (event.player instanceof EntityPlayerMP) PacketFenceConnectionSnapshot.send((EntityPlayerMP) event.player);
  }

  @SubscribeEvent public void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
    if (event.player instanceof EntityPlayerMP) PacketFenceConnectionSnapshot.send((EntityPlayerMP) event.player);
  }

  private static boolean isFenceOrWall(Block block) {
    return block instanceof BlockFence || block instanceof BlockWall;
  }

  private static boolean isMineFantasySaw(ItemStack stack) {
    if (stack == null) return false;
    GameRegistry.UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(stack.getItem());
    return id != null
        && "minefantasy2".equalsIgnoreCase(id.modId)
        && "standard_saw".equalsIgnoreCase(id.name);
  }

  private static int horizontalSide(int face, float hitX, float hitZ) {
    if (face >= 2 && face <= 5) return face;
    float north = hitZ, south = 1F - hitZ, west = hitX, east = 1F - hitX;
    float nearest = Math.min(Math.min(north, south), Math.min(west, east));
    return nearest == north ? 2 : nearest == south ? 3 : nearest == west ? 4 : 5;
  }

}
