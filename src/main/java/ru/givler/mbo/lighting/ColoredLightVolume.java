package ru.givler.mbo.lighting;

/** Bounded RGB flood fill. No Minecraft or OpenGL dependencies; all work is budgeted. */
public final class ColoredLightVolume {
  public final int size, x, y, z;
  private final byte[] opacity;
  private final int[] emission, light, queue;
  private final boolean[] queued;
  private int head, tail, count;

  public ColoredLightVolume(int size, int x, int y, int z) {
    this.size = size; this.x = x; this.y = y; this.z = z;
    int cells = size * size * size;
    opacity = new byte[cells]; emission = new int[cells]; light = new int[cells];
    queue = new int[cells]; queued = new boolean[cells];
  }

  public int cells() { return light.length; }
  public int worldX(int i) { return x + i % size; }
  public int worldY(int i) { return y + i / (size * size); }
  public int worldZ(int i) { return z + i / size % size; }

  public int index(int wx, int wy, int wz) {
    int lx = wx - x, ly = wy - y, lz = wz - z;
    if (lx < 0 || ly < 0 || lz < 0 || lx >= size || ly >= size || lz >= size) return -1;
    return (ly * size + lz) * size + lx;
  }

  public boolean matchesCell(int i, int obstruction, int rgb) {
    return i >= 0 && (opacity[i] & 255) == Math.max(1, Math.min(15, obstruction)) && emission[i] == (rgb & 0xFFFFFF);
  }

  public void copyCell(int to, ColoredLightVolume source, int from) {
    setCell(to, source.opacity[from] & 255, source.emission[from]);
  }

  public ColoredLightVolume copyInputs() {
    ColoredLightVolume copy = new ColoredLightVolume(size, x, y, z);
    for (int i = 0; i < cells(); i++) copy.copyCell(i, this, i);
    return copy;
  }

  public void setCell(int i, int obstruction, int rgb) {
    opacity[i] = (byte) Math.max(1, Math.min(15, obstruction));
    emission[i] = rgb & 0xFFFFFF;
    light[i] = emission[i];
    if (light[i] != 0) enqueue(i);
  }

  public boolean sameInputs(ColoredLightVolume other) {
    return other != null && x == other.x && y == other.y && z == other.z
        && size == other.size && java.util.Arrays.equals(opacity, other.opacity)
        && java.util.Arrays.equals(emission, other.emission);
  }

  private void enqueue(int i) {
    if (queued[i]) return;
    queued[i] = true; queue[tail] = i; tail = (tail + 1) % queue.length; count++;
  }

  /** Returns true when propagation has completed. Never processes more than budget cells. */
  public boolean propagate(int budget) {
    while (count > 0 && budget-- > 0) {
      int i = queue[head]; head = (head + 1) % queue.length; count--; queued[i] = false;
      int lx = i % size, lz = i / size % size, ly = i / (size * size);
      if (lx > 0) spread(i, i - 1);
      if (lx + 1 < size) spread(i, i + 1);
      if (lz > 0) spread(i, i - size);
      if (lz + 1 < size) spread(i, i + size);
      if (ly > 0) spread(i, i - size * size);
      if (ly + 1 < size) spread(i, i + size * size);
    }
    return count == 0;
  }

  private void spread(int from, int to) {
    int loss = (opacity[to] & 255) * 17;
    int value = light[from], old = light[to];
    int r = Math.max(old >>> 16, Math.max(0, (value >>> 16) - loss));
    int g = Math.max(old >> 8 & 255, Math.max(0, (value >> 8 & 255) - loss));
    int b = Math.max(old & 255, Math.max(0, (value & 255) - loss));
    int next = r << 16 | g << 8 | b;
    if (next != old) { light[to] = next; enqueue(to); }
  }

  public int sample(int wx, int wy, int wz) {
    int lx = wx - x, ly = wy - y, lz = wz - z;
    if (lx < 0 || ly < 0 || lz < 0 || lx >= size || ly >= size || lz >= size) return 0;
    return light[(ly * size + lz) * size + lx];
  }

  public static int merge(int a, int b) {
    return Math.max(a >>> 16, b >>> 16) << 16
        | Math.max(a >> 8 & 255, b >> 8 & 255) << 8 | Math.max(a & 255, b & 255);
  }
}
