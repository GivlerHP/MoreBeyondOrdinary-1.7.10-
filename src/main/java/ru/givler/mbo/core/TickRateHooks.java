package ru.givler.mbo.core;

public final class TickRateHooks {
  private static final TickRateClock SERVER = new TickRateClock(System.nanoTime());
  private static volatile int clientRate = 20;

  private TickRateHooks() { }

  public static long serverTimeMillis() { return SERVER.timeMillis(System.nanoTime()); }

  public static void sleepServer(long simulationMillis) throws InterruptedException {
    Thread.sleep(SERVER.sleepMillis(simulationMillis));
  }

  public static int getServerRate() { return SERVER.getRate(); }

  public static void setServerRate(int rate) { SERVER.setRate(rate, System.nanoTime()); }

  public static void setClientRate(int rate) {
    clientRate = rate >= 1 && rate <= 1000 ? rate : 20;
  }

  public static float clientTicksPerSecond(float vanillaRate) {
    return vanillaRate * clientRate / 20F;
  }

  public static int clientTickLimit() {
    return Math.max(10, (clientRate + 1) / 2);
  }
}
