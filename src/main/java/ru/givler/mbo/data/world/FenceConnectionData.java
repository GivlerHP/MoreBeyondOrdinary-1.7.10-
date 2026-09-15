package ru.givler.mbo.data.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import ru.givler.mbo.network.packet.PacketFenceConnectionDelta;

public final class FenceConnectionData extends WorldSavedData {
  private static final String NAME = "mbo_fence_connections";
  private final Map<Long, Byte> masks = new HashMap<Long, Byte>();

  public FenceConnectionData() { super(NAME); }
  public FenceConnectionData(String name) { super(name); }

  public static FenceConnectionData get(World world) {
    FenceConnectionData data = (FenceConnectionData) world.perWorldStorage.loadData(FenceConnectionData.class, NAME);
    if (data == null) { data = new FenceConnectionData(); world.perWorldStorage.setData(NAME, data); }
    return data;
  }

  public byte mask(int x, int y, int z) {
    Byte value = masks.get(pack(x, y, z));
    return value == null ? 0 : value.byteValue();
  }

  public void set(World world, int x, int y, int z, int side, boolean disabled) {
    long key = pack(x, y, z);
    byte old = mask(x, y, z);
    byte updated = disabled ? (byte) (old | bit(side)) : (byte) (old & ~bit(side));
    if (old == updated) return;
    if (updated == 0) masks.remove(key); else masks.put(key, Byte.valueOf(updated));
    markDirty();
    world.markBlockForUpdate(x, y, z);
    PacketFenceConnectionDelta.broadcast(world, x, y, z, updated);
  }

  public void clear(World world, int x, int y, int z) {
    if (masks.remove(pack(x, y, z)) == null) return;
    markDirty();
    PacketFenceConnectionDelta.broadcast(world, x, y, z, (byte) 0);
  }

  public List<Entry> all() {
    List<Entry> result = new ArrayList<Entry>(masks.size());
    for (Map.Entry<Long, Byte> value : masks.entrySet()) result.add(unpack(value.getKey(), value.getValue()));
    return result;
  }

  @Override public void readFromNBT(NBTTagCompound tag) {
    masks.clear();
    NBTTagList list = tag.getTagList("Connections", 10);
    for (int i = 0; i < list.tagCount(); i++) {
      NBTTagCompound entry = list.getCompoundTagAt(i); byte mask = entry.getByte("Mask");
      if (mask != 0) masks.put(pack(entry.getInteger("X"), entry.getInteger("Y"), entry.getInteger("Z")), Byte.valueOf(mask));
    }
  }

  @Override public void writeToNBT(NBTTagCompound tag) {
    NBTTagList list = new NBTTagList();
    for (Entry value : all()) {
      NBTTagCompound entry = new NBTTagCompound();
      entry.setInteger("X", value.x); entry.setInteger("Y", value.y); entry.setInteger("Z", value.z);
      entry.setByte("Mask", value.mask); list.appendTag(entry);
    }
    tag.setTag("Connections", list);
  }

  public static int bit(int side) { return side == 2 ? 1 : side == 3 ? 2 : side == 4 ? 4 : side == 5 ? 8 : 0; }
  public static int opposite(int side) { return side == 2 ? 3 : side == 3 ? 2 : side == 4 ? 5 : 4; }
  public static long pack(int x, int y, int z) {
    return ((long) (x & 0x3ffffff) << 38) | ((long) (z & 0x3ffffff) << 12) | (long) (y & 0xfff);
  }
  private static Entry unpack(long key, byte mask) {
    return new Entry((int) (key >> 38), (int) (key & 0xfff), (int) (key << 26 >> 38), mask);
  }
  public static final class Entry {
    public final int x, y, z; public final byte mask;
    public Entry(int x, int y, int z, byte mask) { this.x=x; this.y=y; this.z=z; this.mask=mask; }
  }
}
