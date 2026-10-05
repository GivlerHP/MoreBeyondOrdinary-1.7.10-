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
    NBTTagList dry = new NBTTagList();
    NBTTagCompound dryPosition = new NBTTagCompound();
    dryPosition.setInteger("X", -8); dryPosition.setInteger("Y", -1); dryPosition.setInteger("Z", 12);
    dry.appendTag(dryPosition); root.setTag("Drained", dry);

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
    if (restored.addIngress(null,-8,-1,12,1,0,0))
      throw new AssertionError("Bucket-drained pane must remain dry after save/reload");
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
    verifyClientChunks();
    verifyChunkPackets();
    System.out.println("Waterlogged negative Y storage and migration passed");
  }

  private static void verifyClientChunks() {
    ClientWaterloggedBlocks.clear();
    ClientWaterloggedBlocks.set(7,-1,-32,-1,true);
    ClientWaterloggedBlocks.set(7,1600,0,1600,true);
    int count=0;
    for (ClientWaterloggedBlocks.Position p:ClientWaterloggedBlocks.nearby(7,-1,-1,96)) {
      if (p.x==1600) throw new AssertionError("Distant chunk included in local water rendering");
      ++count;
    }
    if (count!=1) throw new AssertionError("Negative chunk coordinates lost water");
    ClientWaterloggedBlocks.replaceChunk(7,-1,-1,java.util.Collections.<WaterloggedWorldData.Position>emptyList());
    if (ClientWaterloggedBlocks.contains(7,-1,-32,-1) || !ClientWaterloggedBlocks.contains(7,1600,0,1600))
      throw new AssertionError("Unwatching a chunk must only clear that chunk");
    ClientWaterloggedBlocks.replaceChunk(7,-1,-1,java.util.Arrays.asList(new WaterloggedWorldData.Position(-2,-1,-2)));
    if (!ClientWaterloggedBlocks.contains(7,-2,-1,-2)) throw new AssertionError("Watching a chunk must restore water");
    ClientWaterloggedBlocks.set(8,0,0,0,true);
    count=0;
    for (ClientWaterloggedBlocks.Position p:ClientWaterloggedBlocks.nearby(8,-1,-1,96)) ++count;
    if (count!=1) throw new AssertionError("Water chunk index survived a dimension change");
    ClientWaterloggedBlocks.clear();
  }

  private static void verifyChunkPackets() {
    for (boolean chunk:new boolean[] {false,true}) {
      io.netty.buffer.ByteBuf input=io.netty.buffer.Unpooled.buffer(), output=io.netty.buffer.Unpooled.buffer();
      try {
        input.writeInt(7).writeBoolean(chunk);
        if (chunk) input.writeInt(-1).writeInt(-2);
        input.writeInt(1).writeInt(-3).writeShort(-32).writeInt(-17);
        int length=input.readableBytes();
        ru.givler.mbo.network.packet.PacketWaterloggedSnapshot packet=new ru.givler.mbo.network.packet.PacketWaterloggedSnapshot();
        packet.fromBytes(input.duplicate()); packet.toBytes(output);
        if (output.readableBytes()!=length || !io.netty.buffer.ByteBufUtil.equals(input,output))
          throw new AssertionError("Chunk-scoped water snapshot lost positions or mode");
      } finally { input.release(); output.release(); }
    }
  }
}
