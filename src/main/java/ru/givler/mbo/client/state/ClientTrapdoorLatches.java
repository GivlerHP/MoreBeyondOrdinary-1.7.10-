package ru.givler.mbo.client.state;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ru.givler.mbo.data.world.TrapdoorLatchData;

public final class ClientTrapdoorLatches {
  private static int dimension = Integer.MIN_VALUE;
  private static final Set<Long> POSITIONS = new HashSet<Long>();
  private ClientTrapdoorLatches() {}

  public static boolean contains(int dim, int x, int y, int z) {
    return dimension == dim && POSITIONS.contains(TrapdoorLatchData.pack(x, y, z));
  }
  public static void set(int dim, int x, int y, int z, boolean latched) {
    if (dimension != dim) { dimension = dim; POSITIONS.clear(); }
    long key = TrapdoorLatchData.pack(x, y, z);
    if (latched) POSITIONS.add(key); else POSITIONS.remove(key);
  }
  public static void replace(int dim, List<Long> positions) {
    dimension = dim; POSITIONS.clear(); POSITIONS.addAll(positions);
  }
}
