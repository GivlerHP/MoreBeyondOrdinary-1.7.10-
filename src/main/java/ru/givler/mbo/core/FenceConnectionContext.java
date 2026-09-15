package ru.givler.mbo.core;

/** Coordinates of the fence or wall currently calculating rendering or collision bounds. */
public final class FenceConnectionContext {
  private static final ThreadLocal<int[]> ORIGIN = new ThreadLocal<int[]>();

  private FenceConnectionContext() {}

  public static void set(int x, int y, int z) {
    int[] value = ORIGIN.get();
    if (value == null) ORIGIN.set(value = new int[3]);
    value[0] = x;
    value[1] = y;
    value[2] = z;
  }

  public static int[] get() {
    return ORIGIN.get();
  }
}
