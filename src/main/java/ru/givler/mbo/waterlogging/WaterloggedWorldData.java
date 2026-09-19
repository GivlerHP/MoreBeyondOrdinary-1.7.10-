package ru.givler.mbo.waterlogging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import ru.givler.mbo.network.packet.PacketWaterloggedDelta;

public final class WaterloggedWorldData extends WorldSavedData {
  private static final String NAME = "mbo_waterlogged_blocks";
  public static final byte ALL_FACES = 0x7F;
  public static final byte INTERNAL_SOURCE = 0x40;
  public static final byte OPEN_TRAPDOOR_INGRESS = (byte) 0x80;
  private final Map<Long, WaterloggedChunk> chunks = new HashMap<Long, WaterloggedChunk>();

  public WaterloggedWorldData() {
    super(NAME);
  }

  public WaterloggedWorldData(String name) {
    super(name);
  }

  public static WaterloggedWorldData get(World world) {
    WaterloggedWorldData data =
        (WaterloggedWorldData) world.perWorldStorage.loadData(WaterloggedWorldData.class, NAME);
    if (data == null) {
      data = new WaterloggedWorldData();
      world.perWorldStorage.setData(NAME, data);
    }
    return data;
  }

  public boolean contains(int x, int y, int z) {
    WaterloggedChunk positions = chunks.get(chunkKey(x >> 4, z >> 4));
    return positions != null && positions.contains(pack(x, y, z));
  }

  public byte ingressMask(int x, int y, int z) {
    WaterloggedChunk positions = chunks.get(chunkKey(x >> 4, z >> 4));
    return positions == null ? 0 : positions.mask(pack(x, y, z));
  }

  public boolean addIngress(World world, int x, int y, int z, int faceX, int faceY, int faceZ) {
    if (!WaterloggedBlockSupport.canWaterlog(world, x, y, z)) return false;
    byte mask = WaterloggedGeometry.faceBit(faceX, faceY, faceZ);
    if (isOpenBarrier(world, x, y, z))
      mask |= OPEN_TRAPDOOR_INGRESS;
    return setMask(world, x, y, z, mask);
  }

  public boolean set(World world, int x, int y, int z, boolean waterlogged) {
    if (waterlogged && !WaterloggedBlockSupport.canWaterlog(world, x, y, z)) return false;
    long chunkKey = chunkKey(x >> 4, z >> 4);
    WaterloggedChunk positions = chunks.get(chunkKey);
    boolean changed;
    if (waterlogged) {
      if (positions == null) {
        positions = new WaterloggedChunk();
        chunks.put(chunkKey, positions);
      }
      byte mask = ALL_FACES;
      if (isOpenBarrier(world, x, y, z))
        mask |= OPEN_TRAPDOOR_INGRESS;
      changed = positions.add(pack(x, y, z), mask);
    } else {
      changed = positions != null && positions.remove(pack(x, y, z));
      if (positions != null && positions.isEmpty()) chunks.remove(chunkKey);
    }
    if (changed) {
      markDirty();
      world.markBlockForUpdate(x, y, z);
      PacketWaterloggedDelta.broadcast(world, x, y, z, waterlogged);
      if (waterlogged) WaterloggedFlow.flow(world, this, x, y, z);
      else WaterloggedFlowQueue.onWaterloggedRemoved(world, x, y, z);
    }
    return changed;
  }

  private static boolean isOpenBarrier(World world, int x, int y, int z) {
    Block block = world.getBlock(x, y, z);
    if (block instanceof BlockTrapDoor) return (world.getBlockMetadata(x, y, z) & 4) != 0;
    if (!(block instanceof BlockDoor)) return false;
    int metadata = world.getBlockMetadata(x, y, z);
    int baseY = (metadata & 8) == 0 ? y : y - 1;
    return (world.getBlockMetadata(x, baseY, z) & 4) != 0;
  }

  private boolean setMask(World world, int x, int y, int z, byte addedMask) {
    long chunkKey = chunkKey(x >> 4, z >> 4);
    WaterloggedChunk positions = chunks.get(chunkKey);
    if (positions == null) {
      positions = new WaterloggedChunk();
      chunks.put(chunkKey, positions);
    }
    boolean wasPresent = positions.contains(pack(x, y, z));
    boolean changed = positions.add(pack(x, y, z), addedMask);
    if (changed) {
      markDirty();
      world.markBlockForUpdate(x, y, z);
      if (!wasPresent) PacketWaterloggedDelta.broadcast(world, x, y, z, true);
      WaterloggedFlow.flow(world, this, x, y, z);
    }
    return changed;
  }

  public List<Position> all() {
    List<Position> result = new ArrayList<Position>();
    for (Map.Entry<Long, WaterloggedChunk> entry : chunks.entrySet()) {
      int chunkX = (int) (entry.getKey() >> 32);
      int chunkZ = (int) (long) entry.getKey();
      for (short packed : entry.getValue().values()) result.add(unpack(chunkX, chunkZ, packed));
    }
    return Collections.unmodifiableList(result);
  }

  @Override
  public void readFromNBT(NBTTagCompound tag) {
    chunks.clear();
    NBTTagList list = tag.getTagList("Chunks", 10);
    for (int i = 0; i < list.tagCount(); i++) {
      NBTTagCompound chunk = list.getCompoundTagAt(i);
      byte[] bytes = chunk.getByteArray("Positions");
      byte[] masks = chunk.getByteArray("Ingress");
      if ((bytes.length & 1) != 0) continue;
      WaterloggedChunk positions = new WaterloggedChunk();
      for (int p = 0; p < bytes.length; p += 2) {
        byte mask = p / 2 < masks.length ? masks[p / 2] : ALL_FACES;
        if (mask == 0x3F) mask = ALL_FACES;
        positions.add((short) ((bytes[p] & 255) << 8 | bytes[p + 1] & 255), mask);
      }
      if (!positions.isEmpty())
        positionsFor(chunk.getInteger("X"), chunk.getInteger("Z"), positions);
    }
  }

  @Override
  public void writeToNBT(NBTTagCompound tag) {
    NBTTagList list = new NBTTagList();
    for (Map.Entry<Long, WaterloggedChunk> entry : chunks.entrySet()) {
      NBTTagCompound chunk = new NBTTagCompound();
      chunk.setInteger("X", (int) (entry.getKey() >> 32));
      chunk.setInteger("Z", (int) (long) entry.getKey());
      byte[] bytes = new byte[entry.getValue().size() * 2];
      int index = 0;
      for (short position : entry.getValue().values()) {
        bytes[index++] = (byte) (position >>> 8);
        bytes[index++] = (byte) position;
      }
      chunk.setByteArray("Positions", bytes);
      chunk.setByteArray("Ingress", entry.getValue().masks());
      list.appendTag(chunk);
    }
    tag.setTag("Chunks", list);
  }

  private void positionsFor(int chunkX, int chunkZ, WaterloggedChunk positions) {
    chunks.put(chunkKey(chunkX, chunkZ), positions);
  }

  private static short pack(int x, int y, int z) {
    return (short) ((x & 15) << 12 | (z & 15) << 8 | y & 255);
  }

  private static Position unpack(int chunkX, int chunkZ, short packedValue) {
    int packed = packedValue & 65535;
    return new Position(
        (chunkX << 4) | packed >>> 12, packed & 255, (chunkZ << 4) | packed >>> 8 & 15);
  }

  private static long chunkKey(int x, int z) {
    return (long) x << 32 ^ z & 0xffffffffL;
  }

  private static final class WaterloggedChunk {
    private short[] positions = new short[0];
    private byte[] ingress = new byte[0];

    boolean contains(short position) {
      return Arrays.binarySearch(positions, position) >= 0;
    }

    byte mask(short position) {
      int index = Arrays.binarySearch(positions, position);
      return index < 0 ? 0 : ingress[index];
    }

    boolean add(short position, byte mask) {
      int index = Arrays.binarySearch(positions, position);
      if (index >= 0) {
        byte combined = (byte) (ingress[index] | mask);
        if (combined == ingress[index]) return false;
        ingress[index] = combined;
        return true;
      }
      index = -index - 1;
      short[] expanded = new short[positions.length + 1];
      byte[] expandedIngress = new byte[ingress.length + 1];
      System.arraycopy(positions, 0, expanded, 0, index);
      System.arraycopy(ingress, 0, expandedIngress, 0, index);
      expanded[index] = position;
      expandedIngress[index] = mask;
      System.arraycopy(positions, index, expanded, index + 1, positions.length - index);
      System.arraycopy(ingress, index, expandedIngress, index + 1, ingress.length - index);
      positions = expanded;
      ingress = expandedIngress;
      return true;
    }

    boolean remove(short position) {
      int index = Arrays.binarySearch(positions, position);
      if (index < 0) return false;
      short[] reduced = new short[positions.length - 1];
      byte[] reducedIngress = new byte[ingress.length - 1];
      System.arraycopy(positions, 0, reduced, 0, index);
      System.arraycopy(ingress, 0, reducedIngress, 0, index);
      System.arraycopy(positions, index + 1, reduced, index, positions.length - index - 1);
      System.arraycopy(ingress, index + 1, reducedIngress, index, ingress.length - index - 1);
      positions = reduced;
      ingress = reducedIngress;
      return true;
    }

    boolean isEmpty() {
      return positions.length == 0;
    }

    short[] values() {
      return positions;
    }

    byte[] masks() {
      return ingress.clone();
    }

    int size() {
      return positions.length;
    }
  }

  public static final class Position {
    public final int x;
    public final int y;
    public final int z;

    public Position(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }
  }
}
