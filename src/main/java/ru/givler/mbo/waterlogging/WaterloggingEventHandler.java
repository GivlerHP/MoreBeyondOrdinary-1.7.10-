package ru.givler.mbo.waterlogging;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import ru.givler.mbo.network.packet.PacketWaterloggedSnapshot;

public final class WaterloggingEventHandler {
  private final List<PendingWater> brokenWaterloggedBlocks = new ArrayList<PendingWater>();
  private final List<PendingWater> pendingWaterlogging = new ArrayList<PendingWater>();

  /**
   * Doors and fence gates consume right clicks before ItemBucket can create a FillBucketEvent.
   * Handle buckets first so using one on an opening does not toggle it instead.
   */
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void interactWithBucket(PlayerInteractEvent event) {
    if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
    ItemStack held = event.entityPlayer.getCurrentEquippedItem();
    if (held == null || (held.getItem() != Items.water_bucket && held.getItem() != Items.bucket))
      return;
    if (!WaterloggedBlockSupport.canWaterlog(event.world, event.x, event.y, event.z)) return;

    event.setCanceled(true);
    if (event.world.isRemote) return;

    WaterloggedWorldData data = WaterloggedWorldData.get(event.world);
    if (held.getItem() == Items.water_bucket) {
      boolean changed = setIncludingDoor(event.world, data, event.x, event.y, event.z, true);
      if (!changed) {
        WaterloggedFlow.flow(event.world, data, event.x, event.y, event.z);
        return;
      }
      replaceBucket(event, new ItemStack(Items.bucket));
    } else if (setIncludingDoor(event.world, data, event.x, event.y, event.z, false)) {
      replaceBucket(event, new ItemStack(Items.water_bucket));
    }
  }

  @SubscribeEvent
  public void fillBucket(FillBucketEvent event) {
    if (event.current == null || event.target == null) return;
    int x = event.target.blockX, y = event.target.blockY, z = event.target.blockZ;
    if (event.current.getItem() == Items.water_bucket) {
      if (!WaterloggedBlockSupport.canWaterlog(event.world, x, y, z)) {
        int[] offset = sideOffset(event.target.sideHit);
        x += offset[0];
        y += offset[1];
        z += offset[2];
      }
      if (!WaterloggedBlockSupport.canWaterlog(event.world, x, y, z)) return;
      if (event.world.isRemote) {
        event.setCanceled(true);
        return;
      }
      WaterloggedWorldData data = WaterloggedWorldData.get(event.world);
      if (!data.set(event.world, x, y, z, true)) {
        WaterloggedFlow.flow(event.world, data, x, y, z);
        event.setCanceled(true);
        return;
      }
      event.result = new ItemStack(Items.bucket);
      event.setResult(Result.ALLOW);
      return;
    }
    if (event.world.isRemote) return;
    if (event.current.getItem() != Items.bucket) return;
    if (!WaterloggedWorldData.get(event.world).set(event.world, x, y, z, false)) return;
    event.result = new ItemStack(Items.water_bucket);
    event.setResult(Result.ALLOW);
  }

  @SubscribeEvent
  public void breakBlock(BlockEvent.BreakEvent event) {
    if (event.world.isRemote) return;
    WaterloggedWorldData data = WaterloggedWorldData.get(event.world);
    if (!data.contains(event.x, event.y, event.z)) return;
    data.set(event.world, event.x, event.y, event.z, false);
    brokenWaterloggedBlocks.add(new PendingWater(event.world, event.x, event.y, event.z));
  }

  @SubscribeEvent
  public void worldTick(TickEvent.WorldTickEvent event) {
    if (event.phase != TickEvent.Phase.END || event.world.isRemote) return;
    Iterator<PendingWater> fillIterator = pendingWaterlogging.iterator();
    while (fillIterator.hasNext()) {
      PendingWater pending = fillIterator.next();
      if (pending.world != event.world) continue;
      fillFromAdjacentSource(
          event.world,
          WaterloggedWorldData.get(event.world),
          pending.x,
          pending.y,
          pending.z);
      fillIterator.remove();
    }
    Iterator<PendingWater> iterator = brokenWaterloggedBlocks.iterator();
    while (iterator.hasNext()) {
      PendingWater pending = iterator.next();
      if (pending.world != event.world) continue;
      if (event.world.isAirBlock(pending.x, pending.y, pending.z)
          || event.world.getBlock(pending.x, pending.y, pending.z).getMaterial().isReplaceable())
        if (event.world.setBlock(
            pending.x, pending.y, pending.z, Blocks.flowing_water, 0, 3))
          event.world.scheduleBlockUpdate(
              pending.x,
              pending.y,
              pending.z,
              Blocks.flowing_water,
              Blocks.flowing_water.tickRate(event.world));
      iterator.remove();
    }
  }

  @SubscribeEvent
  public void placeBlock(BlockEvent.PlaceEvent event) {
    if (event.world.isRemote) return;
    WaterloggedWorldData data = WaterloggedWorldData.get(event.world);
    boolean supported =
        WaterloggedBlockSupport.canWaterlog(event.world, event.x, event.y, event.z);
    if (!supported)
      data.set(event.world, event.x, event.y, event.z, false);
    else {
      fillFromAdjacentSource(event.world, data, event.x, event.y, event.z);
      Block block = event.world.getBlock(event.x, event.y, event.z);
      if (block instanceof BlockDoor) {
        int baseY = (event.world.getBlockMetadata(event.x, event.y, event.z) & 8) == 0
            ? event.y
            : event.y - 1;
        pendingWaterlogging.add(new PendingWater(event.world, event.x, baseY, event.z));
        pendingWaterlogging.add(new PendingWater(event.world, event.x, baseY + 1, event.z));
      }
    }
    if ((event.placedBlock == Blocks.water || event.placedBlock == Blocks.flowing_water)
        && event.world.getBlockMetadata(event.x, event.y, event.z) == 0) {
      int[][] directions = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {0, -1, 0}};
      for (int[] direction : directions) {
        int x = event.x + direction[0];
        int y = event.y + direction[1];
        int z = event.z + direction[2];
        if (WaterloggedFlow.canFillFromSource(event.world, x, y, z, direction))
          data.set(event.world, x, y, z, true);
      }
    }
  }

  private static void fillFromAdjacentSource(
      World world, WaterloggedWorldData data, int x, int y, int z) {
    int[][] sources = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {0, 1, 0}};
    for (int[] source : sources) {
      int sourceX = x + source[0];
      int sourceY = y + source[1];
      int sourceZ = z + source[2];
      if (world.getBlock(sourceX, sourceY, sourceZ).getMaterial() != Material.water
          || world.getBlockMetadata(sourceX, sourceY, sourceZ) != 0) continue;
      int[] directionFromSource = {-source[0], -source[1], -source[2]};
      if (WaterloggedFlow.canFillFromSource(world, x, y, z, directionFromSource)) {
        data.set(world, x, y, z, true);
        return;
      }
    }
  }

  @SubscribeEvent
  public void livingUpdate(LivingUpdateEvent event) {
    EntityLivingBase entity = event.entityLiving;
    if (entity.worldObj.isRemote || !touchesWaterloggedSpace(entity)) return;
    entity.extinguish();
  }

  @SubscribeEvent
  public void login(PlayerEvent.PlayerLoggedInEvent event) {
    if (event.player instanceof EntityPlayerMP)
      PacketWaterloggedSnapshot.send((EntityPlayerMP) event.player);
  }

  @SubscribeEvent
  public void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
    if (event.player instanceof EntityPlayerMP)
      PacketWaterloggedSnapshot.send((EntityPlayerMP) event.player);
  }

  private static int[] sideOffset(int side) {
    switch (side) {
      case 0:
        return new int[] {0, -1, 0};
      case 1:
        return new int[] {0, 1, 0};
      case 2:
        return new int[] {0, 0, -1};
      case 3:
        return new int[] {0, 0, 1};
      case 4:
        return new int[] {-1, 0, 0};
      default:
        return new int[] {1, 0, 0};
    }
  }

  private static void replaceBucket(PlayerInteractEvent event, ItemStack replacement) {
    if (event.entityPlayer.capabilities.isCreativeMode) return;
    event.entityPlayer.inventory.setInventorySlotContents(
        event.entityPlayer.inventory.currentItem, replacement);
    event.entityPlayer.inventoryContainer.detectAndSendChanges();
  }

  private static boolean setIncludingDoor(
      World world, WaterloggedWorldData data, int x, int y, int z, boolean waterlogged) {
    Block block = world.getBlock(x, y, z);
    if (!(block instanceof BlockDoor)) return data.set(world, x, y, z, waterlogged);
    int baseY = (world.getBlockMetadata(x, y, z) & 8) == 0 ? y : y - 1;
    boolean changed = data.set(world, x, baseY, z, waterlogged);
    if (world.getBlock(x, baseY + 1, z) == block)
      changed |= data.set(world, x, baseY + 1, z, waterlogged);
    return changed;
  }

  private static boolean touchesWaterloggedSpace(EntityLivingBase entity) {
    AxisAlignedBB box = entity.boundingBox;
    WaterloggedWorldData data = WaterloggedWorldData.get(entity.worldObj);
    int minX = (int) Math.floor(box.minX), maxX = (int) Math.floor(box.maxX);
    int minY = (int) Math.floor(box.minY), maxY = (int) Math.floor(box.maxY);
    int minZ = (int) Math.floor(box.minZ), maxZ = (int) Math.floor(box.maxZ);
    for (int x = minX; x <= maxX; x++)
      for (int y = minY; y <= maxY; y++)
        for (int z = minZ; z <= maxZ; z++)
          if (data.contains(x, y, z)
              && WaterloggedBlockSupport.canWaterlog(entity.worldObj, x, y, z)
              && WaterloggedGeometry.intersects(entity.worldObj, x, y, z, box)) return true;
    return false;
  }

  private static final class PendingWater {
    final World world;
    final int x, y, z;

    PendingWater(World world, int x, int y, int z) {
      this.world = world;
      this.x = x;
      this.y = y;
      this.z = z;
    }
  }
}
