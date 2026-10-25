package ru.givler.mbo.integration.angelica.client.render;

/** MCPatcher's per-call result fields must not be shared by concurrent mesh jobs. */
public final class ThreadLocalBlockColors {
  private static final ThreadLocal<int[]> VALUES = new ThreadLocal<int[]>() {
    @Override protected int[] initialValue() { return new int[15]; }
  };
  private static final ThreadLocal<int[]> TYPES = new ThreadLocal<int[]>() {
    protected int[] initialValue() { return new int[6]; }
  };
  private static final ThreadLocal<float[][]> MULTIPLIERS = new ThreadLocal<float[][]>() {
    protected float[][] initialValue() { return new float[6][3]; }
  };
  public static int[] multiplierTypes() { return TYPES.get(); }
  public static float[][] nonAoMultipliers() { return MULTIPLIERS.get(); }
  private ThreadLocalBlockColors() {}
  public static int getInt(int slot) { return VALUES.get()[slot]; }
  public static void setInt(int value, int slot) { VALUES.get()[slot]=value; }
  public static boolean getBoolean(int slot) { return VALUES.get()[slot]!=0; }
  public static void setBoolean(boolean value, int slot) { VALUES.get()[slot]=value?1:0; }
  public static float getFloat(int slot) { return Float.intBitsToFloat(VALUES.get()[slot]); }
  public static void setFloat(float value, int slot) { VALUES.get()[slot]=Float.floatToRawIntBits(value); }
}
