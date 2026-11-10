package ru.givler.mbo.config;
import java.io.File;
import net.minecraftforge.common.config.Configuration;
/** HUD settings retain the existing file and keys for installed clients. */
public final class HudConfig {
    public static boolean bossBarAtBottom = true;
    private HudConfig() {}
    public static void load(File directory) {
        Configuration cfg = new Configuration(new File(directory, "MoreBeyondOrdinary/tooltip.cfg"));
        try {
            cfg.load();
            bossBarAtBottom = cfg.get("hud", "bossBarAtBottom", true,
                    "Move the boss health bar above the hotbar, health and air HUD.").getBoolean(true);
        } catch (Exception error) {
            System.err.println("[MBO] Failed to load HUD settings: " + error);
        } finally { if (cfg.hasChanged()) cfg.save(); }
    }
}
