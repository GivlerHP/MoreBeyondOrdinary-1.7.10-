package ru.givler.mbo.lockable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LockData {
    private LockDifficulty difficulty = LockDifficulty.NONE;
    private boolean locked;
    private int[] pins = new int[0];
    private long relockAtEpochSec;
    private int relockDelaySec = 30;
    private int playerRadius = 10;
    private final Map<UUID, Integer> attempts = new HashMap<>();
    private final Map<UUID, Long> attemptActivity = new HashMap<>();

    public LockDifficulty getDifficulty() { return difficulty; }
    public boolean isLocked() { return difficulty != LockDifficulty.NONE && locked; }
    public int getRelockDelaySec() { return relockDelaySec; }
    public int getPlayerRadius() { return playerRadius; }
    public long getRelockAtEpochSec() { return relockAtEpochSec; }

    public void setDifficulty(LockDifficulty value, Random random) {
        difficulty = value == null ? LockDifficulty.NONE : value;
        if (difficulty == LockDifficulty.NONE) {
            locked = false;
            pins = new int[0];
        } else {
            locked = true;
            regeneratePins(random);
        }
    }

    public void setRelockDelaySec(int value) { relockDelaySec = Math.max(0, value); }
    public void setPlayerRadius(int value) { playerRadius = Math.max(0, Math.min(128, value)); }

    public void unlock() {
        locked = false;
        relockAtEpochSec = now() + relockDelaySec;
    }

    public void lock(Random random) {
        if (difficulty == LockDifficulty.NONE) return;
        locked = true;
        relockAtEpochSec = 0L;
        regeneratePins(random);
    }

    public int[] copyPins() {
        return pins.clone();
    }

    public boolean matches(int[] attempt) {
        if (attempt == null || attempt.length != pins.length) return false;
        for (int i = 0; i < pins.length; i++) if (attempt[i] != pins[i]) return false;
        return true;
    }

    public boolean isPinCorrect(int pinNumber, int order) {
        return order >= 0 && order < pins.length && pins[order] == pinNumber;
    }

    public boolean checkPin(UUID player, int pinNumber, int order) {
        int expected = attempts.containsKey(player) ? attempts.get(player) : 0;
        attemptActivity.put(player, now());
        if (order != expected || !isPinCorrect(pinNumber, order)) return false;
        attempts.put(player, expected + 1);
        return true;
    }

    public boolean hasCompleted(UUID player) { return attempts.containsKey(player) && attempts.get(player) >= pins.length; }
    public void clearAttempt(UUID player) { attempts.remove(player); attemptActivity.remove(player); }
    public boolean hasActiveAttempt() {
        long cutoff = now() - 120L;
        java.util.Iterator<Map.Entry<UUID, Long>> iterator = attemptActivity.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (entry.getValue() < cutoff) { attempts.remove(entry.getKey()); iterator.remove(); }
        }
        return !attemptActivity.isEmpty();
    }

    public boolean shouldRelock(TileEntity tile) {
        return difficulty != LockDifficulty.NONE && !locked && !hasActiveAttempt() && relockAtEpochSec > 0L
                && now() >= relockAtEpochSec && !hasPlayerNearby(tile);
    }

    @SuppressWarnings("unchecked")
    public boolean hasPlayerNearby(TileEntity tile) {
        if (tile == null || tile.getWorldObj() == null || playerRadius <= 0) return false;
        double r = playerRadius;
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(tile.xCoord + 0.5D - r, tile.yCoord + 0.5D - r,
                tile.zCoord + 0.5D - r, tile.xCoord + 0.5D + r, tile.yCoord + 0.5D + r,
                tile.zCoord + 0.5D + r);
        List<EntityPlayer> players = tile.getWorldObj().getEntitiesWithinAABB(EntityPlayer.class, box);
        if (players == null) return false;
        for (EntityPlayer player : players) {
            if (player != null && !player.capabilities.isCreativeMode) return true;
        }
        return false;
    }

    private void regeneratePins(Random random) {
        attempts.clear();
        attemptActivity.clear();
        Random source = random == null ? new Random() : random;
        pins = new int[difficulty.pinCount];
        for (int i = 0; i < pins.length; i++) pins[i] = i;
        for (int i = pins.length - 1; i > 0; i--) {
            int other = source.nextInt(i + 1);
            int value = pins[i];
            pins[i] = pins[other];
            pins[other] = value;
        }
    }

    private boolean hasValidPins() {
        if (pins.length != difficulty.pinCount) return false;
        boolean[] used = new boolean[pins.length];
        for (int pin : pins) {
            if (pin < 0 || pin >= pins.length || used[pin]) return false;
            used[pin] = true;
        }
        return true;
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setByte("mboLockDifficulty", (byte) difficulty.ordinal());
        tag.setBoolean("mboLocked", locked);
        tag.setIntArray("mboLockPins", pins);
        tag.setLong("mboRelockAt", relockAtEpochSec);
        tag.setInteger("mboRelockDelay", relockDelaySec);
        tag.setInteger("mboLockPlayerRadius", playerRadius);
    }

    public void readFromNBT(NBTTagCompound tag) {
        difficulty = LockDifficulty.byOrdinal(tag.getByte("mboLockDifficulty"));
        locked = tag.getBoolean("mboLocked");
        pins = tag.getIntArray("mboLockPins");
        if (!hasValidPins()) regeneratePins(null);
        relockAtEpochSec = tag.getLong("mboRelockAt");
        relockDelaySec = tag.hasKey("mboRelockDelay") ? Math.max(0, tag.getInteger("mboRelockDelay")) : 30;
        playerRadius = tag.hasKey("mboLockPlayerRadius") ? Math.max(0, tag.getInteger("mboLockPlayerRadius")) : 10;
    }

    private static long now() { return System.currentTimeMillis() / 1000L; }
}
