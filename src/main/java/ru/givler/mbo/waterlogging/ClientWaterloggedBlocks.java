package ru.givler.mbo.waterlogging;

import java.util.HashSet;
import java.util.Set;

public final class ClientWaterloggedBlocks {
  private static int dimension = Integer.MIN_VALUE;
  private static final Set<Position> POSITIONS = new HashSet<Position>();

  private ClientWaterloggedBlocks() {}

  public static void replace(int newDimension, Iterable<WaterloggedWorldData.Position> positions) {
    dimension = newDimension;
    POSITIONS.clear();
    for (WaterloggedWorldData.Position position : positions)
      POSITIONS.add(new Position(position.x, position.y, position.z));
  }

  public static void set(int newDimension, int x, int y, int z, boolean waterlogged) {
    if (dimension != newDimension) {
      dimension = newDimension;
      POSITIONS.clear();
    }
    Position position = new Position(x, y, z);
    if (waterlogged) POSITIONS.add(position);
    else POSITIONS.remove(position);
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
