package ru.givler.mbo.handler;

/** Server rules, independent of rendering and Minecraft state. */
public final class HangingRules {
    private HangingRules() {}
    public static int nextAir(int air) {
        // Once empty, keep vanilla's twenty-tick drowning cycle.
        return air > 0 ? Math.max(0, air - 6) : Math.max(-20, air - 1);
    }
    public static double passengerY(double attachmentY, float yOffset) {
        return attachmentY - 1.4 + yOffset;
    }
    public static boolean escapes(boolean suffocating, int attempts, boolean lucky) {
        if (suffocating && attempts < 2) return false;
        return lucky || suffocating && attempts >= 4;
    }
    public static boolean hookEscapes(int roll) { return roll >= 0 && roll < 10; }
}
