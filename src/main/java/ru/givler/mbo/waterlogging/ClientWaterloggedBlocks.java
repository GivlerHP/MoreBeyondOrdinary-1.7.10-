package ru.givler.mbo.waterlogging;

import java.util.HashSet;
import java.util.Set;

public final class ClientWaterloggedBlocks {
  private static volatile int dimension = Integer.MIN_VALUE;
  private static final Set<Position> POSITIONS = java.util.concurrent.ConcurrentHashMap.newKeySet();
  private static final java.util.Map<Long, Set<Position>> CHUNKS = new java.util.concurrent.ConcurrentHashMap<Long, Set<Position>>();
  // Lookup keys are never inserted into either index and contain no world references.
  private static final ThreadLocal<Lookup> LOOKUP = new ThreadLocal<Lookup>() {
    @Override protected Lookup initialValue() {return new Lookup();}
  };

  private static long chunkKey(int x,int z) { return ((long)(x >> 4) << 32) | ((z >> 4) & 0xffffffffL); }

  private static void index(Position position, boolean add) {
    long key = chunkKey(position.x,position.z);
    Set<Position> positions = CHUNKS.get(key);
    if (add) {
      if (positions == null) { positions = java.util.concurrent.ConcurrentHashMap.newKeySet(); CHUNKS.put(key,positions); }
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
    return dimension == currentDimension && containsCurrent(x,y,z);
  }

  public static boolean containsCurrent(int x, int y, int z) {
    Lookup key=LOOKUP.get();key.x=x;key.y=y;key.z=z;
    return POSITIONS.contains(key);
  }

  /** A 16-block section plus Angelica's two-block halo; copied once, read only by that mesh job. */
  public static Set<Position> snapshotForRender(Object owner,int minX,int minY,int minZ) {
    if(!(owner instanceof net.minecraft.world.World))return java.util.Collections.emptySet();
    net.minecraft.world.World world=(net.minecraft.world.World)owner;
    if(!world.isRemote || world.provider==null)return java.util.Collections.emptySet();
    int expected=world.provider.dimensionId;
    if(dimension!=expected)return java.util.Collections.emptySet();
    Set<Position> result=new HashSet<Position>();
    for(int cx=minX>>4;cx<=(minX+19)>>4;cx++)for(int cz=minZ>>4;cz<=(minZ+19)>>4;cz++) {
      Set<Position> positions=CHUNKS.get(((long)cx<<32)|(cz&0xffffffffL));
      if(positions!=null)for(Position p:positions)
        if(p.x>=minX && p.x<minX+20 && p.y>=minY && p.y<minY+20 && p.z>=minZ && p.z<minZ+20)result.add(p);
    }
    if(dimension!=expected || result.isEmpty())return java.util.Collections.emptySet();
    return java.util.Collections.unmodifiableSet(result);
  }

  public static boolean snapshotContains(Set<Position> snapshot,int x,int y,int z) {
    if(snapshot==null || snapshot.isEmpty())return false;
    Lookup key=LOOKUP.get();key.x=x;key.y=y;key.z=z;return snapshot.contains(key);
  }
  public static boolean snapshotHasWater(Set<Position> snapshot) {return snapshot!=null && !snapshot.isEmpty();}

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

  private interface Coordinates {
    int x();int y();int z();
  }
  private static boolean same(Coordinates value,Object other) {
    if(!(other instanceof Coordinates))return false;
    Coordinates position=(Coordinates)other;
    return value.x()==position.x() && value.y()==position.y() && value.z()==position.z();
  }
  private static int hash(int x,int y,int z) {return (x*31+y)*31+z;}
  private static final class Lookup implements Coordinates {
    int x,y,z;
    public int x(){return x;} public int y(){return y;} public int z(){return z;}
    @Override public boolean equals(Object other){return same(this,other);}
    @Override public int hashCode(){return hash(x,y,z);}
  }
  public static final class Position implements Coordinates {
    public final int x, y, z;

    public Position(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }
    public int x(){return x;} public int y(){return y;} public int z(){return z;}

    @Override
    public boolean equals(Object other) {
      return same(this,other);
    }

    @Override
    public int hashCode() {
      return hash(x,y,z);
    }
  }
}
