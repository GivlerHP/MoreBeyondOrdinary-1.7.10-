package ru.givler.mbo.client.settings;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public final class MirrorSettings {
    public enum Mode { DETAILED, FAST, OFF }

    private static Mode mode = Mode.DETAILED;
    private static Configuration config;

    private MirrorSettings() {}

    public static void load(File directory) {
        config = new Configuration(new File(directory,"MoreBeyondOrdinary/mirrors.cfg"));
        config.load();
        mode = Mode.valueOf(config.getString("Mirrors","quality",Mode.DETAILED.name(),
            "Select the live rendering quality for mirrors.",new String[]{"DETAILED","FAST","OFF"}));
        if (config.hasChanged()) config.save();
    }

    public static Mode mode() { return mode; }

    public static void apply(Mode value) {
        mode = value;
        if (config != null) {
            config.get("quality","Mirrors",Mode.DETAILED.name()).set(value.name());
            config.save();
        }
    }
}
