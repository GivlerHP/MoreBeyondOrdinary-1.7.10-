package ru.givler.mbo.core;

/** A continuous simulation clock; changing rate never jumps accumulated server time. */
public final class TickRateClock {
  private long anchorNanos;
  private double anchorMillis;
  private volatile int rate = 20;

  public TickRateClock(long nowNanos) {
    anchorNanos = nowNanos;
    anchorMillis = nowNanos / 1000000D;
  }

  public synchronized long timeMillis(long nowNanos) {
    return (long) currentMillis(nowNanos);
  }

  public synchronized void setRate(int newRate, long nowNanos) {
    if (newRate < 1 || newRate > 1000) throw new IllegalArgumentException("Tick rate must be 1..1000");
    anchorMillis = currentMillis(nowNanos);
    anchorNanos = nowNanos;
    rate = newRate;
  }

  public int getRate() { return rate; }

  public long sleepMillis(long simulationMillis) {
    return Math.max(1L, (long) Math.ceil(simulationMillis * 20D / rate));
  }

  private double currentMillis(long nowNanos) {
    return anchorMillis + (nowNanos - anchorNanos) / 1000000D * rate / 20D;
  }
}
