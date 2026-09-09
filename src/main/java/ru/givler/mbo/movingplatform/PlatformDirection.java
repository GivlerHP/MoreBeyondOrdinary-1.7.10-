package ru.givler.mbo.movingplatform;

public enum PlatformDirection {
    UP(0, 1, 0), DOWN(0, -1, 0), NORTH(0, 0, -1), SOUTH(0, 0, 1), WEST(-1, 0, 0), EAST(1, 0, 0);
    public final int x, y, z;
    PlatformDirection(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
    public static PlatformDirection byOrdinal(int value) {
        PlatformDirection[] all = values();
        return value < 0 || value >= all.length ? UP : all[value];
    }
}
