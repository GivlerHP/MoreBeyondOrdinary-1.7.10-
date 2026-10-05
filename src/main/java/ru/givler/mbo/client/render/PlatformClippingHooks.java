package ru.givler.mbo.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.ChunkCache;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;

/** Hides the same upper pieces after an elevator materializes at a station. */
public final class PlatformClippingHooks {
  private static World trackedWorld;
  private static final List<EntityMovingPlatform> platforms = new ArrayList<EntityMovingPlatform>();
  private static Map<UUID, Region> regions = new HashMap<UUID, Region>();

  private PlatformClippingHooks() { }

  public static boolean shouldHideBlock(Block block, IBlockAccess access, int x, int y, int z) {
    if (!(access instanceof World) && !(access instanceof ChunkCache)) return false;
    if (access instanceof World && access != trackedWorld) return false;
    for (EntityMovingPlatform platform : platforms)
      if (!platform.isDead && platform.hidesMaterializedBlock(block, x, y, z)) return true;
    return false;
  }

  public static void refresh(World world) {
    if (world != trackedWorld) {
      trackedWorld = world;
      platforms.clear();
      regions.clear();
    }
    if (world == null) return;
    platforms.clear();
    Map<UUID, Region> next = new HashMap<UUID, Region>();
    for (Object entity : world.loadedEntityList) {
      if (!(entity instanceof EntityMovingPlatform)) continue;
      EntityMovingPlatform platform = (EntityMovingPlatform) entity;
      if (platform.isDead || !platform.isClipAboveSelection() || platform.isRebuildPending()
          || platform.isCollisionActive()) continue;
      platforms.add(platform);
      next.put(platform.getPlatformId(), new Region(platform));
    }
    // Recompile old and new stationary regions on settings changes, arrival, or removal.
    for (Map.Entry<UUID, Region> entry : regions.entrySet())
      if (!entry.getValue().same(next.get(entry.getKey()))) entry.getValue().invalidate(world);
    for (Map.Entry<UUID, Region> entry : next.entrySet())
      if (!entry.getValue().same(regions.get(entry.getKey()))) entry.getValue().invalidate(world);
    regions = next;
  }

  private static final class Region {
    final int x, y, z, sx, sy, sz;
    final double ceiling;
    Region(EntityMovingPlatform platform) {
      x = (int) Math.floor(platform.posX);
      y = (int) Math.floor(platform.posY);
      z = (int) Math.floor(platform.posZ);
      sx = platform.getSizeX(); sy = platform.getSizeY(); sz = platform.getSizeZ();
      ceiling = platform.getRenderCeilingY();
    }
    boolean same(Region other) {
      return other != null && x == other.x && y == other.y && z == other.z
          && sx == other.sx && sy == other.sy && sz == other.sz && ceiling == other.ceiling;
    }
    void invalidate(World world) {
      world.markBlockRangeForRenderUpdate(x - 1, y - 1, z - 1, x + sx, y + sy, z + sz);
    }
  }
}
