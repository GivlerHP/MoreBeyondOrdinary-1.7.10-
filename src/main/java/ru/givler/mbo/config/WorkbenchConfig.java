package ru.givler.mbo.config;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public final class WorkbenchConfig {
    public static boolean publicHistory = true;
    private WorkbenchConfig() {}
    public static void load(File directory) {
        Configuration config = new Configuration(new File(directory, "MoreBeyondOrdinary/workbench.cfg"));
        config.load();
        publicHistory = config.getBoolean("PublicHistory", "access", true,
                "Allow all players to use the history tab. Weapon editing always uses BuilderAccess.");
        if (config.hasChanged()) config.save();
    }
}
