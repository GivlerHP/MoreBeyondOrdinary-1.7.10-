package ru.givler.mbo.integration.thaumcraft.handler;

import net.minecraftforge.common.config.Configuration;
import thaumcraft.api.wands.FocusUpgradeType;

public class ConfigHandler {

    public static Configuration config;

    public static int COOLDOWN_UPGRADE_ID = 51;
    public static int DIVINE_PROTECTION_UPGRADE_ID = 52;
    public static int SPIRIT_UPGRADE_ID = 53;
    public static int VITALITY_UPGRADE_ID = 54;
    public static int INSPIRATION_UPGRADE_ID = 55;
    public static int SANITY_UPGRADE_ID = 56;
    public static int CORROSIVE_UPGRADE_ID = 57;
    public static int PERSISTENT_UPGRADE_ID = 58;
    public static int DIFFUSION_UPGRADE_ID = 59;

    public static void initConfig() {
        config.load();

        COOLDOWN_UPGRADE_ID = config.getInt("COOLDOWN_UPGRADE_ID", "wands_and_foci", COOLDOWN_UPGRADE_ID,
                FocusUpgradeType.types.length + 1, Short.MAX_VALUE, "The ID for the Cooldown focus upgrade.");

        DIVINE_PROTECTION_UPGRADE_ID = config.getInt("DIVINE_PROTECTION_UPGRADE_ID", "wands_and_foci", DIVINE_PROTECTION_UPGRADE_ID,
                FocusUpgradeType.types.length + 1, Short.MAX_VALUE, "The ID for the Divine Protection focus upgrade.");

        SPIRIT_UPGRADE_ID = config.getInt("SPIRIT_UPGRADE_ID", "wands_and_foci", SPIRIT_UPGRADE_ID,
                FocusUpgradeType.types.length + 1, Short.MAX_VALUE, "The ID for the Spirit focus upgrade.");

        VITALITY_UPGRADE_ID = config.getInt("VITALITY_UPGRADE_ID", "wands_and_foci", VITALITY_UPGRADE_ID,
                FocusUpgradeType.types.length + 1, Short.MAX_VALUE, "The ID for the Vitality focus upgrade.");

        INSPIRATION_UPGRADE_ID = config.getInt("INSPIRATION_UPGRADE_ID", "wands_and_foci", INSPIRATION_UPGRADE_ID,
                FocusUpgradeType.types.length + 1, Short.MAX_VALUE, "The ID for the Inspiration focus upgrade.");

        SANITY_UPGRADE_ID = config.getInt("SANITY_UPGRADE_ID", "wands_and_foci", SANITY_UPGRADE_ID,
                FocusUpgradeType.types.length + 1, Short.MAX_VALUE, "The ID for the Sanity focus upgrade.");

        CORROSIVE_UPGRADE_ID = config.getInt("CORROSIVE_UPGRADE_ID", "wands_and_foci", CORROSIVE_UPGRADE_ID,
                FocusUpgradeType.types.length + 1, Short.MAX_VALUE, "The ID for the Corrosive focus upgrade.");

        PERSISTENT_UPGRADE_ID = config.getInt("PERSISTENT_UPGRADE_ID", "wands_and_foci", PERSISTENT_UPGRADE_ID,
                FocusUpgradeType.types.length + 1, Short.MAX_VALUE, "The ID for the Persistent focus upgrade.");

        DIFFUSION_UPGRADE_ID = config.getInt("DIFFUSION_UPGRADE_ID", "wands_and_foci", DIFFUSION_UPGRADE_ID,
                FocusUpgradeType.types.length + 1, Short.MAX_VALUE, "The ID for the Diffusion focus upgrade.");

        config.save();
    }
}