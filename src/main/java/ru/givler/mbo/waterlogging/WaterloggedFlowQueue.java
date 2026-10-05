package ru.givler.mbo.waterlogging;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

/** Defers outlet checks until the block change which caused them has fully completed. */
public final class WaterloggedFlowQueue {
  private static final Map<World, Set<Long>> PENDING = new WeakHashMap<World, Set<Long>>();
  private static final ThreadLocal<Boolean> PROCESSING = new ThreadLocal<Boolean>();
  private static final int[][] NEARBY = {
    {0, 0, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
  };

  private WaterloggedFlowQueue() {}

  public static void onWaterloggedRemoved(World world, int x, int y, int z) {
    if (world == null || world.isRemote || Boolean.TRUE.equals(PROCESSING.get())) return;
    scheduleAround(world, x, y, z);
    wakeAdjacentWater(world, x, y, z);
  }

  public static void scheduleAround(World world, int x, int y, int z) {
    if (world == null || world.isRemote || Boolean.TRUE.equals(PROCESSING.get())) return;
    Set<Long> positions = null;
    for (int[] offset : NEARBY) {
      int checkX = x + offset[0];
      int checkY = y + offset[1];
      int checkZ = z + offset[2];
      if (checkY < -64
          || checkY >= world.getHeight()
          || !world.blockExists(checkX, checkY, checkZ)
          || !WaterloggedBlockSupport.canWaterlog(world, checkX, checkY, checkZ)) continue;
      if (positions == null) {
        positions = PENDING.get(world);
        if (positions == null) {
          positions = new HashSet<Long>();
          PENDING.put(world, positions);
        }
      }
      positions.add(pack(checkX, checkY, checkZ));
    }
  }

  public static void process(World world) {
    Set<Long> positions = PENDING.remove(world);
    if (positions == null || positions.isEmpty()) return;
    WaterloggedWorldData data = WaterloggedWorldData.get(world);
    Set<Long> changed = new HashSet<Long>();
    PROCESSING.set(Boolean.TRUE);
    try {
      for (long packed : positions) {
        int x = unpackX(packed);
        int y = unpackY(packed);
        int z = unpackZ(packed);
        if (!world.blockExists(x, y, z)
            || !WaterloggedBlockSupport.canWaterlog(world, x, y, z)) continue;
        byte before = data.ingressMask(x,y,z);
        if (data.contains(x, y, z)
            && (!WaterloggedFlow.canRetainWater(world, data, x, y, z)
                || !hasActiveIngress(world, data, x, y, z))) {
          data.set(world, x, y, z, false);
          wakeAdjacentWater(world, x, y, z);
        }
        if (!data.contains(x, y, z)) fillFromAdjacentSource(world, data, x, y, z);
        if (data.contains(x, y, z)) {
          WaterloggedFlow.flow(world, data, x, y, z);
          wakeAdjacentWater(world, x, y, z);
        }
        if (before != data.ingressMask(x,y,z)) changed.add(packed);
      }
    } finally {
      PROCESSING.remove();
    }
    for (long position : changed)
      scheduleAround(world,unpackX(position),unpackY(position),unpackZ(position));
  }

  private static boolean hasActiveIngress(
      World world, WaterloggedWorldData data, int x, int y, int z) {
    byte mask = data.ingressMask(x, y, z);
    if ((mask & WaterloggedWorldData.INTERNAL_SOURCE) != 0) return true;
    int[][] faces = {
      {-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}
    };
    for (int[] face : faces) {
      if ((mask & WaterloggedGeometry.faceBit(face[0], face[1], face[2])) == 0) continue;
      int waterX = x + face[0], waterY = y + face[1], waterZ = z + face[2];
      if (hasConnectedSupply(world, data, waterX, waterY, waterZ,
          -face[0], -face[1], -face[2])) return true;
    }
    return false;
  }

  private static void fillFromAdjacentSource(
      World world, WaterloggedWorldData data, int x, int y, int z) {
    int[][] sources = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {0, 1, 0}};
    for (int[] source : sources) {
      int sourceX = x + source[0];
      int sourceY = y + source[1];
      int sourceZ = z + source[2];
      if (!hasConnectedSupply(world, data, sourceX, sourceY, sourceZ,
          -source[0], -source[1], -source[2])) continue;
      int[] directionFromSource = {-source[0], -source[1], -source[2]};
      if (!WaterloggedFlow.canFillFromSource(world, x, y, z, directionFromSource)) continue;
      data.addIngress(world, x, y, z, source[0], source[1], source[2]);
    }
  }

  /** Trace upstream to real water; a loop of waterlogged blocks is not a source. */
  private static boolean hasConnectedSupply(World world, WaterloggedWorldData data,
      int x, int y, int z, int outX, int outY, int outZ) {
    java.util.ArrayDeque<int[]> pending = new java.util.ArrayDeque<int[]>();
    java.util.HashSet<String> visited = new java.util.HashSet<String>();
    pending.add(new int[] {x, y, z, outX, outY, outZ});
    int[][] faces = {{-1,0,0}, {1,0,0}, {0,-1,0}, {0,1,0}, {0,0,-1}, {0,0,1}};
    while (!pending.isEmpty()) {
      int[] node = pending.removeFirst();
      if (!visited.add(node[0]+":"+node[1]+":"+node[2]+":"+node[3]+":"+node[4]+":"+node[5])) continue;
      if (world.getBlock(node[0], node[1], node[2]).getMaterial() == Material.water) {
        if (node[4] < 0 || world.getBlockMetadata(node[0], node[1], node[2]) == 0) return true;
        continue;
      }
      if (!data.contains(node[0], node[1], node[2])
          || !WaterloggedFlow.canSupplyWaterTo(world, data, node[0], node[1], node[2],
              node[3], node[4], node[5])) continue;
      byte mask = data.ingressMask(node[0], node[1], node[2]);
      if ((mask & WaterloggedWorldData.INTERNAL_SOURCE) != 0) return true;
      for (int side = 0; side < faces.length; ++side) {
        int[] face = faces[side];
        if ((mask & WaterloggedGeometry.faceBit(face[0], face[1], face[2])) == 0
            || face[0] == node[3] && face[1] == node[4] && face[2] == node[5]) continue;
        if (!WaterloggedGeometry.canReachFace(world, node[0], node[1], node[2],
            WaterloggedGeometry.faceBit(face[0], face[1], face[2]),
            node[3], node[4], node[5])) continue;
        pending.add(new int[] {node[0]+face[0], node[1]+face[1], node[2]+face[2],
            -face[0], -face[1], -face[2]});
      }
    }
    return false;
  }

  private static void wakeAdjacentWater(World world, int x, int y, int z) {
    for (int[] offset : NEARBY) {
      if (offset[0] == 0 && offset[1] == 0 && offset[2] == 0) continue;
      int waterX = x + offset[0];
      int waterY = y + offset[1];
      int waterZ = z + offset[2];
      if (waterY < -64 || waterY >= world.getHeight()) continue;
      Block water = world.getBlock(waterX, waterY, waterZ);
      if (water.getMaterial() != Material.water) continue;
      water.onNeighborBlockChange(world, waterX, waterY, waterZ, world.getBlock(x, y, z));
      water = world.getBlock(waterX, waterY, waterZ);
      if (water.getMaterial() != Material.water) continue;
      world.scheduleBlockUpdate(
          waterX, waterY, waterZ, water, water.tickRate(world));
    }
  }

  private static long pack(int x, int y, int z) {
    return ((long) x & 0x3FFFFFFL) << 38 | ((long) z & 0x3FFFFFFL) << 12 | y & 0xFFFL;
  }

  private static int unpackX(long packed) {
    return (int) (packed >> 38);
  }

  private static int unpackY(long packed) {
    int y = (int) (packed & 0xFFFL);
    return y >= 2048 ? y - 4096 : y;
  }

  private static int unpackZ(long packed) {
    return (int) (packed << 26 >> 38);
  }
}
