package ru.givler.mbo.lockable;

public enum RefillMode {
    IMMEDIATE, BATCH, GRADUAL;

    public static RefillMode byOrdinal(int ordinal) {
        RefillMode[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : BATCH;
    }
}
