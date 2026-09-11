package ru.givler.mbo.movingplatform;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

public final class PlatformBlock {
  public final int x, y, z, meta;
  public final Block block;

  public PlatformBlock(int x, int y, int z, Block block, int meta) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.block = block;
    this.meta = meta;
  }

  public NBTTagCompound write() {
    NBTTagCompound tag = new NBTTagCompound();
    tag.setInteger("X", x);
    tag.setInteger("Y", y);
    tag.setInteger("Z", z);
    tag.setString("Block", String.valueOf(GameData.getBlockRegistry().getNameForObject(block)));
    tag.setByte("Meta", (byte) meta);
    return tag;
  }

  public static PlatformBlock read(NBTTagCompound tag) {
    Block block = GameData.getBlockRegistry().getObject(tag.getString("Block"));
    return block == null
        ? null
        : new PlatformBlock(
            tag.getInteger("X"),
            tag.getInteger("Y"),
            tag.getInteger("Z"),
            block,
            tag.getByte("Meta") & 15);
  }
}
