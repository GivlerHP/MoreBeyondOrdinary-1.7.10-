package ru.givler.mbo.waterlogging;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWall;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.block.DoorBase;
import ru.givler.mbo.block.model.BlockModelCollision;

public final class WaterloggedBlockSupport {
  private WaterloggedBlockSupport() {}

  public static boolean canWaterlog(World world, int x, int y, int z) {
    if (world == null || y < 0 || y > 255) return false;
    Block block = world.getBlock(x, y, z);
    if (block instanceof BlockStairs) return true;
    if (block instanceof BlockSlab) return !block.isOpaqueCube();
    return block instanceof BlockModels
        || block instanceof BlockModelCollision
        || block instanceof BlockFence
        || block instanceof BlockWall
        || block instanceof BlockTrapDoor
        || block instanceof BlockFenceGate
        || block instanceof DoorBase
        || block instanceof BlockDoor
        || block instanceof BlockPane
        || block instanceof BlockLever
        || block instanceof BlockButton
        || block instanceof BlockBasePressurePlate;
  }
}
