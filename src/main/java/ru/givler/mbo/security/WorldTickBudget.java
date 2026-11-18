package ru.givler.mbo.security;

import java.util.Map;
import java.util.WeakHashMap;

public final class WorldTickBudget {
    private final int limit;
    private final Map<Object, TickUsage> usageByWorld = new WeakHashMap<>();

    public WorldTickBudget(int limit) {
        this.limit = limit;
    }

    public synchronized int reserve(Object world, long tick, int requested) {
        TickUsage usage = usageByWorld.get(world);
        if (usage == null || usage.tick != tick) {
            usage = new TickUsage(tick);
            usageByWorld.put(world, usage);
        }
        int allowed = Math.min(Math.max(0, requested), limit - usage.count);
        usage.count += allowed;
        return allowed;
    }

    private static final class TickUsage {
        final long tick;
        int count;

        TickUsage(long tick) {
            this.tick = tick;
        }
    }
}
