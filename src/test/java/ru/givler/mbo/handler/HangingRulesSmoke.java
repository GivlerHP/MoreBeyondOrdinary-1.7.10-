package ru.givler.mbo.handler;

/** Gameplay rules can be verified without launching Minecraft. */
public final class HangingRulesSmoke {
    public static void main(String[] args) {
        check(!HangingRules.escapes(true, 1, true), "gallows must reject the first attempt");
        check(HangingRules.escapes(true, 2, true), "second attempt can succeed");
        check(!HangingRules.escapes(true, 2, false), "failed attempt must retain the player");
        check(HangingRules.escapes(false, 1, true), "hook can release on first attempt");
        check(!HangingRules.escapes(false, 1, false), "hook failure causes damage instead");
        check(HangingRules.escapes(true, 4, false), "escape eventually guaranteed");
        check(!HangingRules.escapes(false, 4, false), "hook must not guarantee the fourth attempt");
        int successes = 0;
        for (int roll = 0; roll < 100; roll++) if (HangingRules.hookEscapes(roll)) successes++;
        check(successes == 10, "hook escape chance must be ten percent");
        int air = 300;
        for (int i = 0; i < 49; i++) air = HangingRules.nextAir(air);
        check(air == 6, "air remains before 50 ticks");
        check(HangingRules.nextAir(air) == 0, "air runs out after 2.5 seconds");
        int drowning = 0;
        for (int i = 0; i < 20; i++) drowning = HangingRules.nextAir(drowning);
        check(drowning == -20, "vanilla drowning threshold reached after twenty ticks");
        check(HangingRules.nextAir(-20) == -20, "never skip vanilla drowning threshold");
        double serverY = HangingRules.passengerY(3, 0);
        double clientY = HangingRules.passengerY(3, 1.62F);
        check(Math.abs(serverY - (clientY - 1.62F)) < .00001,
            "server and client must agree on physical feet position");
        check(serverY + 1.62 > 3, "eyes must be above the neck attachment");
        System.out.println("Hanging escape and air checks passed");
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
