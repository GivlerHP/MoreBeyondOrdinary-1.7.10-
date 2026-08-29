package ru.givler.mbo.config;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class LockSecurityConfig {
    public static final int CREATIVE_AND_KEY = 0;
    public static final int OP_AND_KEY = 1;
    public static final int UUID_LIST_AND_KEY = 2;
    public static int protectionLevel = CREATIVE_AND_KEY;
    private static final Set<String> allowedUuids = new HashSet<>();

    private LockSecurityConfig() {}

    public static void load(File configDir) {
        Configuration cfg = new Configuration(new File(configDir, "MoreBeyondOrdinary/locks.cfg"));
        cfg.load();
        protectionLevel = Math.max(0, Math.min(2, cfg.getInt("ProtectionLevel", "security", 0, 0, 2,
                "0 = creative + admin key, 1 = creative + admin key + OP, 2 = creative + admin key + UUID whitelist")));
        String[] values = cfg.getStringList("AllowedUUIDs", "security", new String[0],
                "UUIDs allowed to configure locks when ProtectionLevel is 2. Hyphens are optional.");
        allowedUuids.clear();
        for (String value : values) if (value != null && !value.trim().isEmpty()) allowedUuids.add(normalize(value));
        if (cfg.hasChanged()) cfg.save();
    }

    public static boolean isAuthorized(EntityPlayer player) {
        if (player == null) return false;
        if (protectionLevel == CREATIVE_AND_KEY) return true;
        if (protectionLevel == OP_AND_KEY) {
            MinecraftServer server = MinecraftServer.getServer();
            return server != null && server.getConfigurationManager().func_152596_g(player.getGameProfile());
        }
        return allowedUuids.contains(normalize(player.getUniqueID().toString()));
    }

    private static String normalize(String value) { return value.replace("-", "").trim().toLowerCase(Locale.ROOT); }
}
