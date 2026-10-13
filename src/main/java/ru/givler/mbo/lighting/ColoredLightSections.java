package ru.givler.mbo.lighting;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Event-driven RGB relaxation on persistent world sections; missing sections block light. */
public final class ColoredLightSections {
  private static final int[] DARK = new int[4096];
  private final Map<Key, Section> sections = new HashMap<>();
  private final Key probe = new Key(0, 0, 0);
  private final ArrayDeque<Section> work = new ArrayDeque<>();
  private final ArrayDeque<QueueBuffers> spareQueues = new ArrayDeque<>();
  private final Set<Section> changed = new LinkedHashSet<>();
  private final Set<Key> removed = new LinkedHashSet<>();

  private Section section(int x, int y, int z) {
    probe.x = x; probe.y = y; probe.z = z; return sections.get(probe);
  }
  public boolean contains(int x, int y, int z) { return section(x, y, z) != null; }

  public boolean install(int sx, int sy, int sz, byte[] opacity, int[] emission) {
    if (opacity.length != 4096 || emission.length != 4096) throw new IllegalArgumentException("Section must contain 4096 cells");
    Section old = section(sx, sy, sz);
    if (old != null) {
      boolean changed = false;
      for (int i = 0; i < 4096; i++) changed |= updateCell(old, i, opacity[i] & 255, emission[i]);
      return changed;
    }
    Section s = new Section(sx, sy, sz);
    sections.put(new Key(sx, sy, sz), s);
    for (int i = 0; i < 4096; i++) {
      s.opacity[i] = (byte) Math.max(1, Math.min(15, opacity[i] & 255));
      s.setEmission(i, emission[i] & 0xFFFFFF);
      if (s.emission(i) != 0 || border(i)) enqueue(s, i);
    }
    return true;
  }

  private static boolean border(int i) {
    int x = i & 15, z = i >> 4 & 15, y = i >> 8;
    return x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15;
  }

  public boolean matches(int x, int y, int z, int opacity, int emission) {
    Section s = section(x >> 4, y >> 4, z >> 4);
    int i = ((y & 15) * 16 + (z & 15)) * 16 + (x & 15);
    return s != null && (s.opacity[i] & 255) == Math.max(1, Math.min(15, opacity)) && s.emission(i) == (emission & 0xFFFFFF);
  }

  public void update(int x, int y, int z, int opacity, int emission) {
    Section s = section(x >> 4, y >> 4, z >> 4);
    if (s != null) updateCell(s, ((y & 15) * 16 + (z & 15)) * 16 + (x & 15), opacity, emission);
  }

  private boolean updateCell(Section s, int i, int opacity, int emission) {
    int obstruction = Math.max(1, Math.min(15, opacity)); emission &= 0xFFFFFF;
    if ((s.opacity[i] & 255) == obstruction && s.emission(i) == emission) return false;
    s.opacity[i] = (byte) obstruction; s.setEmission(i, emission); enqueue(s, i);
    return true;
  }

  public int sample(int x, int y, int z) {
    Section s = section(x >> 4, y >> 4, z >> 4);
    return s == null ? 0 : s.light(((y & 15) * 16 + (z & 15)) * 16 + (x & 15));
  }

  private void enqueue(Section s, int i) {
    if (s == null || !s.live) return;
    if(s.queue==null) {
      s.buffers=spareQueues.pollFirst();if(s.buffers==null)s.buffers=new QueueBuffers();
      s.queue=s.buffers.cells;s.queued=s.buffers.bits;s.head=s.tail=0;
    }
    long bit=1L<<(i&63);int word=i>>6;
    if((s.queued[word]&bit)!=0)return;
    s.queued[word] |= bit; s.queue[s.tail] = (short)i; s.tail = (s.tail + 1) & 4095; s.count++;
    schedule(s);
  }
  private void schedule(Section s) {
    if (s.count > 0 && !s.scheduled && s.live) { s.scheduled = true; work.addLast(s); }
  }

  private Section neighbor(Section s, int i, int direction) {
    switch (direction) {
      case 0: return (i >> 8) == 0 ? section(s.x, s.y - 1, s.z) : s;
      case 1: return (i >> 8) == 15 ? section(s.x, s.y + 1, s.z) : s;
      case 2: return (i >> 4 & 15) == 0 ? section(s.x, s.y, s.z - 1) : s;
      case 3: return (i >> 4 & 15) == 15 ? section(s.x, s.y, s.z + 1) : s;
      case 4: return (i & 15) == 0 ? section(s.x - 1, s.y, s.z) : s;
      default: return (i & 15) == 15 ? section(s.x + 1, s.y, s.z) : s;
    }
  }
  private static int adjacent(int i, int direction) {
    switch (direction) {
      case 0: return (i - 256) & 4095;
      case 1: return (i + 256) & 4095;
      case 2: return i & 0xF0F | (i - 16) & 0xF0;
      case 3: return i & 0xF0F | (i + 16) & 0xF0;
      case 4: return i & 0xFF0 | (i - 1) & 15;
      default: return i & 0xFF0 | (i + 1) & 15;
    }
  }

  /** Both increases and decreases converge: every non-source edge loses at least 17. */
  public boolean process(int budget) {
    int initialBudget=budget;
    while (budget-- > 0 && !work.isEmpty()) {
      Section s = work.removeFirst(); s.scheduled = false;
      if (!s.live || s.count == 0) continue;
      int i = s.queue[s.head]&0xffff; s.head = (s.head + 1) & 4095; s.count--; s.queued[i>>6] &= ~(1L<<(i&63));
      int emission=s.emission(i);
      int r = emission >>> 16, g = emission >> 8 & 255, b = emission & 255;
      int loss = (s.opacity[i] & 255) * 17;
      for (int d = 0; d < 6; d++) {
        Section n = neighbor(s, i, d);
        if (n == null) continue;
        int value = n.light(adjacent(i, d));
        r = Math.max(r, (value >>> 16) - loss); g = Math.max(g, (value >> 8 & 255) - loss); b = Math.max(b, (value & 255) - loss);
      }
      int next = r << 16 | g << 8 | b;
      if (next != s.light(i)) {
        s.setLight(i,next); changed.add(s);
        for (int d = 0; d < 6; d++) enqueue(neighbor(s, i, d), adjacent(i, d));
      }
      schedule(s);
      if(s.count==0) {
        // Every membership bit has been cleared by popping; reuse scratch storage, not world data.
        if(spareQueues.size()<8)spareQueues.addLast(s.buffers);
        s.buffers=null;s.queue=null;s.queued=null;
      }
    }
    processedCells+=Math.max(0,initialBudget-budget-1);
    return work.isEmpty();
  }
  private long processedCells;
  public long processedCells(){return processedCells;}

  public void publish(ColoredLightCache cache, ColoredLightCache.Dirty dirty) {
    if (!work.isEmpty() || (changed.isEmpty() && removed.isEmpty())) return;
    ColoredLightCache.Editor editor = cache.edit();
    for (Key key : removed) editor.remove(key.x, key.y, key.z, dirty);
    for (Section s : changed) if (s.live) editor.put(s.x, s.y, s.z, s.light==null?DARK:s.light, dirty);
    editor.commit(); removed.clear(); changed.clear();
  }

  public void remove(int sx, int sy, int sz) {
    probe.x = sx; probe.y = sy; probe.z = sz;
    Section s = sections.remove(probe);
    if (s == null) return;
    s.live = false; changed.remove(s); removed.add(new Key(sx, sy, sz));
    s.buffers=null;s.queue=null;s.queued=null;s.count=0;
    for (int i = 0; i < 4096; i++) if (border(i)) for (int d = 0; d < 6; d++) {
      Section n = neighbor(s, i, d); if (n != s) enqueue(n, adjacent(i, d));
    }
  }

  public void retain(int sx, int sy, int sz, int radius) {
    java.util.List<Key> discard = new java.util.ArrayList<>();
    for (Key key : sections.keySet()) if (Math.abs(key.x - sx) > radius || Math.abs(key.y - sy) > radius || Math.abs(key.z - sz) > radius) discard.add(key);
    for (Key key : discard) remove(key.x, key.y, key.z);
  }
  public int sectionCount() { return sections.size(); }
  public int opacity(int x, int y, int z) {
    Section s = section(x >> 4, y >> 4, z >> 4);
    return s == null ? 15 : s.opacity[((y & 15) * 16 + (z & 15)) * 16 + (x & 15)] & 255;
  }

  private static final class Key {
    int x, y, z;
    Key(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
    @Override public int hashCode() { return (x * 31 + y) * 31 + z; }
    @Override public boolean equals(Object other) { if (!(other instanceof Key)) return false; Key k = (Key) other; return x == k.x && y == k.y && z == k.z; }
  }
  private static final class Section {
    final int x, y, z;
    final byte[] opacity = new byte[4096];
    int[] emission, light;
    short[] queue;
    long[] queued;
    QueueBuffers buffers;
    int emissionCells, lightCells;
    int emission(int i) {return emission==null?0:emission[i];}
    int light(int i) {return light==null?0:light[i];}
    void setEmission(int i,int value) {
      int old=emission(i);if(old==value)return;
      if(emission==null)emission=new int[4096];
      if(old!=0)emissionCells--;if(value!=0)emissionCells++;
      emission[i]=value;if(emissionCells==0)emission=null;
    }
    void setLight(int i,int value) {
      int old=light(i);if(old==value)return;
      if(light==null)light=new int[4096];
      if(old!=0)lightCells--;if(value!=0)lightCells++;
      light[i]=value;if(lightCells==0)light=null;
    }
    int head, tail, count;
    boolean live = true, scheduled;
    Section(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
  }
  private static final class QueueBuffers {
    final short[] cells=new short[4096];
    final long[] bits=new long[64];
  }
}
