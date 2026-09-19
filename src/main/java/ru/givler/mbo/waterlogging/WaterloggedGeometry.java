package ru.givler.mbo.waterlogging;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.block.model.BlockModelCollision;

public final class WaterloggedGeometry {
  private WaterloggedGeometry() {}

  public static boolean[] freeCells(World world, int x, int y, int z) {
    boolean[] free = new boolean[8];
    Block block = world.getBlock(x, y, z);
    List<AxisAlignedBB> collision = new ArrayList<AxisAlignedBB>();
    block.addCollisionBoxesToList(
        world,
        x,
        y,
        z,
        AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1),
        collision,
        (Entity) null);
    for (int cellX = 0; cellX < 2; cellX++)
      for (int cellY = 0; cellY < 2; cellY++)
        for (int cellZ = 0; cellZ < 2; cellZ++) {
          double centerX = x + cellX * 0.5D + 0.25D;
          double centerY = y + cellY * 0.5D + 0.25D;
          double centerZ = z + cellZ * 0.5D + 0.25D;
          boolean solid = false;
          for (AxisAlignedBB box : collision)
            if (centerX > box.minX
                && centerX < box.maxX
                && centerY > box.minY
                && centerY < box.maxY
                && centerZ > box.minZ
                && centerZ < box.maxZ) {
              solid = true;
              break;
            }
          free[index(cellX, cellY, cellZ)] = !solid;
        }
    return free;
  }

  public static boolean[] waterCellsForRender(World world, int x, int y, int z) {
    boolean[] free = freeCells(world, x, y, z);
    Block block = world.getBlock(x, y, z);
    if (block instanceof BlockTrapDoor) {
      int metadata = world.getBlockMetadata(x, y, z);
      if ((metadata & 4) == 0) {
        int blockedY = (metadata & 8) == 0 ? 0 : 1;
        for (int cellX = 0; cellX < 2; cellX++)
          for (int cellZ = 0; cellZ < 2; cellZ++)
            free[index(cellX, blockedY, cellZ)] = false;
      }
      return free;
    }
    if (block instanceof BlockFence
        || block instanceof BlockFenceGate
        || block instanceof BlockWall
        || block instanceof BlockModelCollision
        || block instanceof BlockLever
        || block instanceof BlockButton
        || block instanceof BlockBasePressurePlate) {
      for (int i = 0; i < free.length; i++) free[i] = true;
      return free;
    }
    if (!(block instanceof BlockPane)) return free;
    BlockPane pane = (BlockPane) block;
    boolean north = pane.canPaneConnectTo(world, x, y, z - 1, ForgeDirection.NORTH);
    boolean south = pane.canPaneConnectTo(world, x, y, z + 1, ForgeDirection.SOUTH);
    boolean west = pane.canPaneConnectTo(world, x - 1, y, z, ForgeDirection.WEST);
    boolean east = pane.canPaneConnectTo(world, x + 1, y, z, ForgeDirection.EAST);
    boolean northSouthWall = north && south;
    boolean westEastWall = west && east;
    if (!northSouthWall && !westEastWall) {
      for (int i = 0; i < free.length; i++) free[i] = true;
      return free;
    }

    boolean[] wet = new boolean[4];
    seedWater(world, x, y, z - 1, wet, 0, 2);
    seedWater(world, x, y, z + 1, wet, 1, 3);
    seedWater(world, x - 1, y, z, wet, 0, 1);
    seedWater(world, x + 1, y, z, wet, 2, 3);
    if (!wet[0] && !wet[1] && !wet[2] && !wet[3]) return free;
    boolean changed;
    do {
      changed = false;
      changed |= connect(wet, 0, 2, !northSouthWall);
      changed |= connect(wet, 1, 3, !northSouthWall);
      changed |= connect(wet, 0, 1, !westEastWall);
      changed |= connect(wet, 2, 3, !westEastWall);
    } while (changed);
    for (int cellX = 0; cellX < 2; cellX++)
      for (int cellY = 0; cellY < 2; cellY++)
        for (int cellZ = 0; cellZ < 2; cellZ++)
          free[index(cellX, cellY, cellZ)] &= wet[cellX * 2 + cellZ];
    return free;
  }

  private static void seedWater(
      World world, int x, int y, int z, boolean[] wet, int first, int second) {
    if (hasWater(world, x, y, z)) {
      wet[first] = true;
      wet[second] = true;
    }
  }

  private static boolean hasWater(World world, int x, int y, int z) {
    return world.getBlock(x, y, z).getMaterial() == Material.water
        && world.getBlockMetadata(x, y, z) == 0;
  }

  public static boolean isFaceOpen(
      World world, int x, int y, int z, int directionX, int directionY, int directionZ) {
    Block block = world.getBlock(x, y, z);
    List<AxisAlignedBB> collision = new ArrayList<AxisAlignedBB>();
    block.addCollisionBoxesToList(
        world,
        x,
        y,
        z,
        AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1),
        collision,
        (Entity) null);
    double epsilon = 1.0D / 1024.0D;
    for (int first = 0; first < 2; first++)
      for (int second = 0; second < 2; second++) {
        double a = first * 0.5D + 0.25D;
        double b = second * 0.5D + 0.25D;
        double sampleX;
        double sampleY;
        double sampleZ;
        if (directionX != 0) {
          sampleX = directionX < 0 ? x + epsilon : x + 1 - epsilon;
          sampleY = y + a;
          sampleZ = z + b;
        } else if (directionY != 0) {
          sampleX = x + a;
          sampleY = directionY < 0 ? y + epsilon : y + 1 - epsilon;
          sampleZ = z + b;
        } else {
          sampleX = x + a;
          sampleY = y + b;
          sampleZ = directionZ < 0 ? z + epsilon : z + 1 - epsilon;
        }
        boolean blocked = false;
        for (AxisAlignedBB box : collision)
          if (sampleX >= box.minX
              && sampleX <= box.maxX
              && sampleY >= box.minY
              && sampleY <= box.maxY
              && sampleZ >= box.minZ
              && sampleZ <= box.maxZ) {
            blocked = true;
            break;
          }
        if (!blocked) return true;
      }
    return false;
  }

  public static boolean isLateralToDoor(
      World world, int x, int y, int z, int directionX, int directionZ) {
    Block block = world.getBlock(x, y, z);
    if (!(block instanceof BlockDoor)) return true;
    List<AxisAlignedBB> collision = new ArrayList<AxisAlignedBB>();
    block.addCollisionBoxesToList(
        world,
        x,
        y,
        z,
        AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1),
        collision,
        (Entity) null);
    if (collision.isEmpty()) return true;
    AxisAlignedBB box = collision.get(0);
    double widthX = box.maxX - box.minX;
    double widthZ = box.maxZ - box.minZ;
    return widthX < widthZ ? directionZ != 0 : directionX != 0;
  }

  public static byte faceBit(int x, int y, int z) {
    if (x < 0) return 1;
    if (x > 0) return 2;
    if (y < 0) return 4;
    if (y > 0) return 8;
    if (z < 0) return 16;
    return 32;
  }

  public static boolean canReachFace(
      World world, int x, int y, int z, byte ingressMask, int outX, int outY, int outZ) {
    if (ingressMask == 0) return false;
    Block block = world.getBlock(x, y, z);
    List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
    block.addCollisionBoxesToList(
        world,
        x,
        y,
        z,
        AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1),
        boxes,
        (Entity) null);
    boolean[] free = freeCells(world, x, y, z);
    boolean[] reached = new boolean[8];
    int[] queue = new int[8];
    int head = 0, tail = 0;
    int[][] faces = {{-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}};
    for (int[] face : faces) {
      if ((ingressMask & faceBit(face[0], face[1], face[2])) == 0) continue;
      for (int node = 0; node < 8; node++)
        if (free[node] && nodeTouchesFace(node, face[0], face[1], face[2])
            && clearToFace(boxes, x, y, z, node, face[0], face[1], face[2])
            && !reached[node]) {
          reached[node] = true;
          queue[tail++] = node;
        }
    }
    int[][] steps = {{-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}};
    while (head < tail) {
      int node = queue[head++];
      if (nodeTouchesFace(node, outX, outY, outZ)
          && clearToFace(boxes, x, y, z, node, outX, outY, outZ)) return true;
      int cellX = node >> 2;
      int cellY = node >> 1 & 1;
      int cellZ = node & 1;
      for (int[] step : steps) {
        int nextX = cellX + step[0], nextY = cellY + step[1], nextZ = cellZ + step[2];
        if (nextX < 0 || nextX > 1 || nextY < 0 || nextY > 1 || nextZ < 0 || nextZ > 1)
          continue;
        int next = index(nextX, nextY, nextZ);
        if (!free[next]) continue;
        if (!reached[next]
            && clearSegment(boxes, x, y, z, cellX, cellY, cellZ, nextX, nextY, nextZ)) {
          reached[next] = true;
          queue[tail++] = next;
        }
      }
    }
    return false;
  }

  private static boolean nodeTouchesFace(int node, int x, int y, int z) {
    int cellX = node >> 2, cellY = node >> 1 & 1, cellZ = node & 1;
    return x < 0 ? cellX == 0 : x > 0 ? cellX == 1
        : y < 0 ? cellY == 0 : y > 0 ? cellY == 1 : z < 0 ? cellZ == 0 : cellZ == 1;
  }

  private static boolean clearToFace(
      List<AxisAlignedBB> boxes, int x, int y, int z, int node, int dx, int dy, int dz) {
    double cx = x + (node >> 2) * 0.5D + 0.25D;
    double cy = y + (node >> 1 & 1) * 0.5D + 0.25D;
    double cz = z + (node & 1) * 0.5D + 0.25D;
    return clearLine(boxes, cx, cy, cz,
        dx < 0 ? x : dx > 0 ? x + 1 : cx,
        dy < 0 ? y : dy > 0 ? y + 1 : cy,
        dz < 0 ? z : dz > 0 ? z + 1 : cz);
  }

  private static boolean clearSegment(
      List<AxisAlignedBB> boxes, int x, int y, int z,
      int ax, int ay, int az, int bx, int by, int bz) {
    return clearLine(boxes,
        x + ax * 0.5D + 0.25D, y + ay * 0.5D + 0.25D, z + az * 0.5D + 0.25D,
        x + bx * 0.5D + 0.25D, y + by * 0.5D + 0.25D, z + bz * 0.5D + 0.25D);
  }

  private static boolean clearLine(
      List<AxisAlignedBB> boxes, double ax, double ay, double az, double bx, double by, double bz) {
    for (AxisAlignedBB box : boxes) {
      net.minecraft.util.Vec3 start = net.minecraft.util.Vec3.createVectorHelper(ax, ay, az);
      net.minecraft.util.Vec3 end = net.minecraft.util.Vec3.createVectorHelper(bx, by, bz);
      if (box.calculateIntercept(start, end) != null) return false;
    }
    return true;
  }

  private static boolean connect(boolean[] wet, int first, int second, boolean open) {
    if (!open || wet[first] == wet[second]) return false;
    wet[first] = wet[second] = true;
    return true;
  }

  public static boolean intersects(World world, int x, int y, int z, AxisAlignedBB target) {
    boolean[] free = waterCellsForRender(world, x, y, z);
    for (int cellX = 0; cellX < 2; cellX++)
      for (int cellY = 0; cellY < 2; cellY++)
        for (int cellZ = 0; cellZ < 2; cellZ++)
          if (free[index(cellX, cellY, cellZ)]
              && target.intersectsWith(
                  AxisAlignedBB.getBoundingBox(
                      x + cellX * 0.5D,
                      y + cellY * 0.5D,
                      z + cellZ * 0.5D,
                      x + (cellX + 1) * 0.5D,
                      y + (cellY + 1) * 0.5D,
                      z + (cellZ + 1) * 0.5D))) return true;
    return false;
  }

  public static int index(int x, int y, int z) {
    return x << 2 | y << 1 | z;
  }
}
