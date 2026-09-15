package ru.givler.mbo.movingplatform;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public final class PlatformBlock {
  public final int x, y, z;
  public int meta;
  public final Block block;
  public boolean collidable = true;
  public double minX, minY, minZ, maxX = 1D, maxY = 1D, maxZ = 1D;

  public PlatformBlock(int x, int y, int z, Block block, int meta) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.block = block;
    this.meta = meta;
  }

  public void captureCollision(World world, int worldX, int worldY, int worldZ) {
    block.setBlockBoundsBasedOnState(world, worldX, worldY, worldZ);
    AxisAlignedBB box = block.getCollisionBoundingBoxFromPool(world, worldX, worldY, worldZ);
    collidable = box != null;
    if (box == null) return;
    minX = box.minX - worldX; minY = box.minY - worldY; minZ = box.minZ - worldZ;
    maxX = box.maxX - worldX; maxY = box.maxY - worldY; maxZ = box.maxZ - worldZ;
  }

  public NBTTagCompound write() {
    NBTTagCompound tag = new NBTTagCompound();
    tag.setInteger("X", x);
    tag.setInteger("Y", y);
    tag.setInteger("Z", z);
    tag.setString("Block", String.valueOf(GameData.getBlockRegistry().getNameForObject(block)));
    tag.setByte("Meta", (byte) meta);
    tag.setBoolean("Collidable", collidable);
    tag.setDouble("MinX", minX); tag.setDouble("MinY", minY); tag.setDouble("MinZ", minZ);
    tag.setDouble("MaxX", maxX); tag.setDouble("MaxY", maxY); tag.setDouble("MaxZ", maxZ);
    return tag;
  }

  public static PlatformBlock read(NBTTagCompound tag) {
    Block block = GameData.getBlockRegistry().getObject(tag.getString("Block"));
    if (block == null) return null;
    PlatformBlock result = new PlatformBlock(
            tag.getInteger("X"),
            tag.getInteger("Y"),
            tag.getInteger("Z"),
            block,
            tag.getByte("Meta") & 15);
    if (tag.hasKey("Collidable")) {
      result.collidable = tag.getBoolean("Collidable");
      result.minX = tag.getDouble("MinX"); result.minY = tag.getDouble("MinY"); result.minZ = tag.getDouble("MinZ");
      result.maxX = tag.getDouble("MaxX"); result.maxY = tag.getDouble("MaxY"); result.maxZ = tag.getDouble("MaxZ");
    }
    return result;
  }
}
