package ru.givler.mbo.handler;

import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AmuletCooldownTracker {
    private static final String TAG = "AmuletCD";
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    public static boolean isOnCooldown(EntityPlayer player) {
        return getRemainingCooldown(player) > 0;
    }

    public static long getRemainingCooldown(EntityPlayer player) {
        long now = System.currentTimeMillis();
        long end = cooldowns.getOrDefault(player.getUniqueID(), 0L);
        return Math.max(0, end - now);
    }

    public static void setCooldown(EntityPlayer player, int ticks) {
        long now = System.currentTimeMillis();
        cooldowns.put(player.getUniqueID(), now + ticks * 50L); // 1 тик = 50 мс
    }
}