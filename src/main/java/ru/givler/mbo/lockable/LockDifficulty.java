package ru.givler.mbo.lockable;

public enum LockDifficulty {
    NONE(0, 27), EASY(4, 27), NORMAL(5, 27), HARD(6, 27);

    public final int pinCount;
    public final int inventorySlots;

    LockDifficulty(int pinCount, int inventorySlots) {
        this.pinCount = pinCount;
        this.inventorySlots = inventorySlots;
    }

    public static LockDifficulty byOrdinal(int ordinal) {
        LockDifficulty[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NONE;
    }
}
