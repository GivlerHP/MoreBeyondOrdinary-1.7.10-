package ru.givler.mbo.waterlogging;

import java.util.HashSet;
import java.util.Set;

public final class ClientWaterloggedBlocks {
  private static int dimension = Integer.MIN_VALUE;
  private static final Set<Position> POSITIONS = new HashSet<Position>();
  private static final java.util.Map<Long, Set<Position>> CHUNKS = new java.util.HashMap<Long, Set<Position>>();

  private static long chunkKey(int x,int z) { return ((long)(x >> 4) << 32) | ((z >> 4) & 0xffffffffL); }

  private static void index(Position position, boolean add) {
    long key = chunkKey(position.x,position.z);
    Set<Position> positions = CHUNKS.get(key);
    if (add) {
      if (positions == null) { positions = new HashSet<Position>(); CHUNKS.put(key,positions); }
      positions.add(position);
    } else if (positions != null) {
      positions.remove(position);
      if (positions.isEmpty()) CHUNKS.remove(key);
    }
  }

  public static Iterable<Position> nearby(int currentDimension, double x,double z,int radius) {
    java.util.List<Position> result = new java.util.ArrayList<Position>();
    if (dimension != currentDimension) return result;
    int minX = ((int)Math.floor(x)-radius) >> 4, maxX = ((int)Math.floor(x)+radius) >> 4;
    int minZ = ((int)Math.floor(z)-radius) >> 4, maxZ = ((int)Math.floor(z)+radius) >> 4;
    for (int cx = minX; cx <= maxX; ++cx) for (int cz = minZ; cz <= maxZ; ++cz) {
      Set<Position> positions = CHUNKS.get(((long)cx << 32) | (cz & 0xffffffffL));
      if (positions != null) result.addAll(positions);
    }
    return result;
  }

  public static void replaceChunk(int newDimension,int chunkX,int chunkZ,Iterable<WaterloggedWorldData.Position> positions) {
    if (dimension != newDimension) { clear(); dimension=newDimension; }
    long key=((long)chunkX << 32) | (chunkZ & 0xffffffffL);
    Set<Position> old=CHUNKS.remove(key);
    if (old!=null) POSITIONS.removeAll(old);
    for (WaterloggedWorldData.Position p:positions) set(newDimension,p.x,p.y,p.z,true);
  }

  private ClientWaterloggedBlocks() {}

  public static void replace(int newDimension, Iterable<WaterloggedWorldData.Position> positions) {
    dimension = newDimension;
    POSITIONS.clear();
    CHUNKS.clear();
    for (WaterloggedWorldData.Position position : positions)
      set(newDimension,position.x,position.y,position.z,true);
  }

  public static void set(int newDimension, int x, int y, int z, boolean waterlogged) {
    if (dimension != newDimension) {
      dimension = newDimension;
      POSITIONS.clear();
      CHUNKS.clear();
    }
    Position position = new Position(x, y, z);
    if (waterlogged) POSITIONS.add(position);
    else POSITIONS.remove(position);
    index(position,waterlogged);
  }

  public static boolean contains(int currentDimension, int x, int y, int z) {
    return dimension == currentDimension && POSITIONS.contains(new Position(x, y, z));
  }

  public static boolean containsCurrent(int x, int y, int z) {
    return POSITIONS.contains(new Position(x, y, z));
  }

  public static Set<Position> all(int currentDimension) {
    return dimension == currentDimension
        ? new HashSet<Position>(POSITIONS)
        : new HashSet<Position>();
  }

  public static void clear() {
    dimension = Integer.MIN_VALUE;
    POSITIONS.clear();
    CHUNKS.clear();
  }

  public static final class Position {
    public final int x, y, z;

    public Position(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof Position)) return false;
      Position position = (Position) other;
      return x == position.x && y == position.y && z == position.z;
    }

    @Override
    public int hashCode() {
      int result = x;
      result = 31 * result + y;
      return 31 * result + z;
    }
  }
}
