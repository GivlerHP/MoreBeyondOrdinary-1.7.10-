package ru.givler.mbo.command;

import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
import net.minecraft.entity.player.PlayerCapabilities;

/** The vanilla speed setters are client-only; the backing fields exist on both sides. */
final class PlayerSpeedAccess {
    private static final Field WALK = ReflectionHelper.findField(PlayerCapabilities.class,
            "walkSpeed", "field_75097_g");
    private static final Field FLY = ReflectionHelper.findField(PlayerCapabilities.class,
            "flySpeed", "field_75096_f");

    static void set(PlayerCapabilities capabilities, float walk, float fly) {
        try {
            WALK.setFloat(capabilities, walk);
            FLY.setFloat(capabilities, fly);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot update player movement speeds", exception);
        }
    }
}
