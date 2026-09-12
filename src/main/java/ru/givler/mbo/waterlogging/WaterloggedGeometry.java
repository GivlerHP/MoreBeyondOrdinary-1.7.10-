package ru.givler.mbo.waterlogging;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
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
    return world.getBlock(x, y, z).getMaterial() == Material.water;
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
