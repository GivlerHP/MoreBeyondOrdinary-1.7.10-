package ru.givler.mbo.data.world;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import ru.givler.mbo.network.packet.PacketTrapdoorLatchDelta;

public final class TrapdoorLatchData extends WorldSavedData {
  private static final String NAME = "mbo_latched_trapdoors";
  private final Set<Long> latched = new HashSet<Long>();

  public TrapdoorLatchData() { super(NAME); }
  public TrapdoorLatchData(String name) { super(name); }

  public static TrapdoorLatchData get(World world) {
    TrapdoorLatchData data =
        (TrapdoorLatchData) world.perWorldStorage.loadData(TrapdoorLatchData.class, NAME);
    if (data == null) { data = new TrapdoorLatchData(); world.perWorldStorage.setData(NAME, data); }
    return data;
  }

  public boolean contains(int x, int y, int z) { return latched.contains(pack(x, y, z)); }

  public boolean toggle(World world, int x, int y, int z) {
    long key = pack(x, y, z);
    boolean locked;
    if (latched.remove(key)) locked = false;
    else { latched.add(key); locked = true; }
    markDirty();
    PacketTrapdoorLatchDelta.broadcast(world, x, y, z, locked);
    return locked;
  }

  public void clear(World world, int x, int y, int z) {
    if (latched.remove(pack(x, y, z))) {
      markDirty();
      PacketTrapdoorLatchDelta.broadcast(world, x, y, z, false);
    }
  }

  public List<Long> all() { return new ArrayList<Long>(latched); }

  @Override public void readFromNBT(NBTTagCompound tag) {
    latched.clear();
    int[] values = tag.getIntArray("Positions");
    for (int i = 0; i + 1 < values.length; i += 2)
      latched.add(((long) values[i] << 32) | (values[i + 1] & 0xffffffffL));
  }

  @Override public void writeToNBT(NBTTagCompound tag) {
    int[] values = new int[latched.size() * 2]; int i = 0;
    for (Long boxed : latched) {
      long value = boxed.longValue();
      values[i++] = (int) (value >>> 32);
      values[i++] = (int) value;
    }
    tag.setIntArray("Positions", values);
  }

  public static long pack(int x, int y, int z) {
    return ((long) (x & 0x3ffffff) << 38) | ((long) (z & 0x3ffffff) << 12) | (long) (y & 0xfff);
  }
}
