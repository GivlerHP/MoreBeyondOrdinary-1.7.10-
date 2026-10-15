package ru.givler.mbo.lighting;

import java.util.HashMap;
import java.util.Map;

/** Immutable world-coordinate section colors. Moving the scan window never clears overlap. */
public final class ColoredLightCache {
  public interface Dirty { void mark(int x, int y, int z); }
  private volatile Map<Key, int[]> sections = new HashMap<>();
  private volatile long version;
  public long version() { return version; }
  private final ThreadLocal<Key> regionProbe = new ThreadLocal<Key>() {
    @Override protected Key initialValue() {return new Key(0,0,0);}
  };
  /** Immutable section arrays change identity only when their published RGB changes. */
  public int regionSignature(int minX,int minY,int minZ,int maxX,int maxY,int maxZ) {
    Map<Key,int[]> snapshot=sections;Key key=regionProbe.get();int signature=1;
    for(int y=minY>>4;y<=(maxY>>4);y++)for(int z=minZ>>4;z<=(maxZ>>4);z++)for(int x=minX>>4;x<=(maxX>>4);x++) {
      key.x=x;key.y=y;key.z=z;
      signature=31*signature+System.identityHashCode(snapshot.get(key));
    }
    return signature;
  }
  private final java.util.concurrent.ConcurrentLinkedQueue<java.lang.ref.WeakReference<Lookup>> lookups=new java.util.concurrent.ConcurrentLinkedQueue<>();
  private final ThreadLocal<Lookup> lookup = new ThreadLocal<Lookup>() {
    @Override protected Lookup initialValue() { Lookup value=new Lookup();lookups.add(new java.lang.ref.WeakReference<>(value));return value; }
  };

  public int sample(int x, int y, int z) {
    Lookup value=lookup.get();int sx=x>>4,sy=y>>4,sz=z>>4;
    for(;;) {
      long snapshot=version;
      int[] data=value.data;
      if(value.version==snapshot && value.key.x==sx && value.key.y==sy && value.key.z==sz)
        return data==null?0:data[((y&15)*16+(z&15))*16+(x&15)];
      value.key.x=sx;value.key.y=sy;value.key.z=sz;
      data=sections.get(value.key);value.data=data;value.version=snapshot;
      if(version!=snapshot){value.data=null;value.version=-1;continue;}
      return data==null?0:data[((y&15)*16+(z&15))*16+(x&15)];
    }
  }

  public void clear(Dirty dirty) {
    invalidateAll(dirty);
    sections = new HashMap<>();
    version++;
    for(java.util.Iterator<java.lang.ref.WeakReference<Lookup>> iterator=lookups.iterator();iterator.hasNext();) {
      Lookup value=iterator.next().get();if(value==null)iterator.remove();else {value.data=null;value.version=-1;}
    }
  }

  public Editor edit() { return new Editor(); }

  /** Atomic publication of a converged update, without changing any vertex format. */
  public final class Editor {
    private final Map<Key, int[]> next = new HashMap<>(sections);
    public void put(int sx, int sy, int sz, int[] data, Dirty dirty) {
      Key key = new Key(sx, sy, sz); int[] old = next.get(key);
      boolean different = false, nonzero = false;
      for (int i = 0; i < 4096; i++) {
        nonzero |= data[i] != 0;
        if ((old == null ? 0 : old[i]) == data[i]) continue;
        different = true;
        int lx = i & 15, ly = i >> 8, lz = i >> 4 & 15;
        // Shared vertices sample diagonal sections too.
        for (int dy = ly == 0 ? -1 : 0; dy <= (ly == 15 ? 1 : 0); dy++)
          for (int dz = lz == 0 ? -1 : 0; dz <= (lz == 15 ? 1 : 0); dz++)
            for (int dx = lx == 0 ? -1 : 0; dx <= (lx == 15 ? 1 : 0); dx++)
              dirty.mark((sx + dx) * 16 + 8, (sy + dy) * 16 + 8, (sz + dz) * 16 + 8);
      }
      if (!different) return;
      if (nonzero) next.put(key, data.clone()); else next.remove(key);
    }
    public void remove(int sx, int sy, int sz, Dirty dirty) {
      if (next.remove(new Key(sx, sy, sz)) != null)
        for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) for (int dx = -1; dx <= 1; dx++)
          dirty.mark((sx + dx) * 16 + 8, (sy + dy) * 16 + 8, (sz + dz) * 16 + 8);
    }
    public void commit() { sections = next; version++; }
  }

  public void invalidateAll(Dirty dirty) {
    for (Key key : sections.keySet()) markSection(key, dirty);
  }

  private static void markSection(Key key, Dirty dirty) {
    int x = key.x * 16 + 8, y = key.y * 16 + 8, z = key.z * 16 + 8;
    // Face interpolation can read the edge/corner of a diagonally adjacent section.
    for(int dy=-1;dy<=1;dy++)for(int dz=-1;dz<=1;dz++)for(int dx=-1;dx<=1;dx++)
      dirty.mark(x+dx*16,y+dy*16,z+dz*16);
  }

  /** Only publish the interior: every possible contributing source has been scanned in the halo. */
  public void publish(ColoredLightVolume volume, int halo, Dirty dirty) {
    Map<Key, int[]> next = new HashMap<>(sections), edited = new HashMap<>();
    Key probe = new Key(0, 0, 0);
    for (int y = volume.y + halo; y < volume.y + volume.size - halo; y++) {
      if (y < 0 || y >= 256) continue;
      for (int z = volume.z + halo; z < volume.z + volume.size - halo; z++) for (int x = volume.x + halo; x < volume.x + volume.size - halo; x++) {
        probe.x = x >> 4; probe.y = y >> 4; probe.z = z >> 4;
        int i = ((y & 15) * 16 + (z & 15)) * 16 + (x & 15), value = volume.sample(x, y, z);
        int[] data = next.get(probe);
        if ((data == null ? 0 : data[i]) == value) continue;
        if (!edited.containsKey(probe)) {
          Key key = new Key(probe.x, probe.y, probe.z);
          data = data == null ? new int[4096] : data.clone(); next.put(key, data); edited.put(key, data);
        }
        data[i] = value;
        dirty.mark(x, y, z); dirty.mark(x - 1, y, z); dirty.mark(x + 1, y, z);
        dirty.mark(x, y - 1, z); dirty.mark(x, y + 1, z); dirty.mark(x, y, z - 1); dirty.mark(x, y, z + 1);
      }
    }
    // Retain a bounded neighborhood behind the player; evicted geometry is invalidated too.
    java.util.Iterator<Key> keys = next.keySet().iterator();
    while (keys.hasNext()) {
      Key key = keys.next();
      int cx = key.x * 16 + 8, cy = key.y * 16 + 8, cz = key.z * 16 + 8;
      if (Math.abs(cx - (volume.x + volume.size / 2)) > 64 || Math.abs(cy - (volume.y + volume.size / 2)) > 64 || Math.abs(cz - (volume.z + volume.size / 2)) > 64) {
        keys.remove(); markSection(key, dirty);
      }
    }
    sections = next;
    version++;
  }

  private static final class Key {
    int x, y, z;
    Key(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
    @Override public int hashCode() { return (x * 31 + y) * 31 + z; }
    @Override public boolean equals(Object other) {
      if (!(other instanceof Key)) return false;
      Key key = (Key) other; return x == key.x && y == key.y && z == key.z;
    }
  }
  private static final class Lookup {final Key key=new Key(0,0,0);volatile int[] data;volatile long version=-1;}
}
