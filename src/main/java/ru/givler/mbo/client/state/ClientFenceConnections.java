package ru.givler.mbo.client.state;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.givler.mbo.data.world.FenceConnectionData;

public final class ClientFenceConnections {
  private static int dimension = Integer.MIN_VALUE;
  private static final Map<Long, Byte> MASKS = new HashMap<Long, Byte>();

  private ClientFenceConnections() {}

  public static byte get(int x, int y, int z) {
    Byte value = MASKS.get(FenceConnectionData.pack(x, y, z));
    return value == null ? 0 : value.byteValue();
  }

  public static void set(int dim, int x, int y, int z, byte mask) {
    if (dimension != dim) { dimension = dim; MASKS.clear(); }
    long key = FenceConnectionData.pack(x, y, z);
    if (mask == 0) MASKS.remove(key); else MASKS.put(key, Byte.valueOf(mask));
  }

  public static void replace(int dim, List<FenceConnectionData.Entry> entries) {
    dimension = dim;
    MASKS.clear();
    for (FenceConnectionData.Entry entry : entries)
      if (entry.mask != 0) MASKS.put(FenceConnectionData.pack(entry.x, entry.y, entry.z), entry.mask);
  }
}
