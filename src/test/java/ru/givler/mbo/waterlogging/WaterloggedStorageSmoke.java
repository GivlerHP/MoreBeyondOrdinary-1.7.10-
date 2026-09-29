package ru.givler.mbo.waterlogging;

import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public final class WaterloggedStorageSmoke {
  public static void main(String[] args) {
    NBTTagCompound root = new NBTTagCompound();
    NBTTagList chunks = new NBTTagList();
    NBTTagCompound chunk = new NBTTagCompound();
    chunk.setInteger("X", 2);
    chunk.setInteger("Z", -3);
    int low = (4 << 13) | (7 << 9) | 63;  // Y=-1
    int high = (4 << 13) | (7 << 9) | 319; // Y=255
    chunk.setByteArray("PositionsV2", new byte[] {
        (byte) (low >>> 16), (byte) (low >>> 8), (byte) low,
        (byte) (high >>> 16), (byte) (high >>> 8), (byte) high});
    chunk.setByteArray("Ingress", new byte[] {1, 2});
    chunks.appendTag(chunk);
    root.setTag("Chunks", chunks);

    WaterloggedWorldData data = new WaterloggedWorldData();
    data.readFromNBT(root);
    int x = 36, z = -41;
    if (!data.contains(x, -1, z) || !data.contains(x, 255, z)
        || data.contains(x, -64, z) || data.contains(x, 0, z)
        || data.ingressMask(x, -1, z) != 1 || data.ingressMask(x, 255, z) != 2)
      throw new AssertionError("Negative and positive Y aliases in waterlogged storage");
    NBTTagCompound saved = new NBTTagCompound();
    data.writeToNBT(saved);
    WaterloggedWorldData restored = new WaterloggedWorldData();
    restored.readFromNBT(saved);
    if (!restored.contains(x, -1, z) || !restored.contains(x, 255, z))
      throw new AssertionError("Waterlogged storage round trip failed");

    NBTTagCompound oldRoot = new NBTTagCompound();
    NBTTagList oldChunks = new NBTTagList();
    NBTTagCompound oldChunk = new NBTTagCompound();
    oldChunk.setInteger("X", 2);
    oldChunk.setInteger("Z", -3);
    int oldPosition = (4 << 12) | (7 << 8) | 255;
    oldChunk.setByteArray("Positions", new byte[] {(byte) (oldPosition >>> 8), (byte) oldPosition});
    oldChunks.appendTag(oldChunk);
    oldRoot.setTag("Chunks", oldChunks);
    restored.readFromNBT(oldRoot);
    List<WaterloggedWorldData.Position> positions = restored.all();
    if (positions.size() != 1 || positions.get(0).y != 255
        || !restored.contains(x, 255, z) || restored.contains(x, -1, z))
      throw new AssertionError("Legacy waterlogged storage migration failed");
    System.out.println("Waterlogged negative Y storage and migration passed");
  }
}
