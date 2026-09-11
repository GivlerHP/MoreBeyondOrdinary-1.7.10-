package ru.givler.mbo.integration.thaumcraft.item.focus;

import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.integration.thaumcraft.config.ThaumcraftConfig;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;

public class TMFocusUpgrades {

    public static FocusUpgradeType cooldown;
    public static FocusUpgradeType divineProtection;
    public static FocusUpgradeType spirit;
    public static FocusUpgradeType vitality;
    public static FocusUpgradeType inspiration;
    public static FocusUpgradeType sanity;
    public static FocusUpgradeType corrosive;
    public static FocusUpgradeType persistent;
    public static FocusUpgradeType diffusion;
    public static FocusUpgradeType tripleEye;
    public static FocusUpgradeType corrosive_darklight;

    public static void initFocusUpgrades() {

        cooldown = new FocusUpgradeType(
                ThaumcraftConfig.COOLDOWN_UPGRADE_ID,
                new ResourceLocation("thaumcraft", "textures/foci/extend.png"),
                "focus.upgrade.cleansing.cooldown.name",
                "focus.upgrade.cleansing.cooldown.text",
                new AspectList().add(Aspect.AIR, 1)
        );

        divineProtection = new FocusUpgradeType(
                ThaumcraftConfig.DIVINE_PROTECTION_UPGRADE_ID,
                new ResourceLocation("mbo", "textures/foci/divine_protection.png"),
                "focus.upgrade.cleansing.divine.name",
                "focus.upgrade.cleansing.divine.text",
                new AspectList().add(Aspect.ORDER, 1)
        );

        spirit = new FocusUpgradeType(
                ThaumcraftConfig.SPIRIT_UPGRADE_ID,
                new ResourceLocation("mbo", "textures/foci/spirit.png"),
                "focus.upgrade.mhealing.spirit.name",
                "focus.upgrade.mhealing.spirit.text",
                new AspectList().add(Aspect.MIND, 1)
        );

        vitality = new FocusUpgradeType(
                ThaumcraftConfig.VITALITY_UPGRADE_ID,
                new ResourceLocation("mbo", "textures/foci/vitality.png"),
                "focus.upgrade.mhealing.vitality.name",
                "focus.upgrade.mhealing.vitality.text",
                new AspectList().add(Aspect.LIFE, 1)
        );

        inspiration = new FocusUpgradeType(
                ThaumcraftConfig.INSPIRATION_UPGRADE_ID,
                new ResourceLocation("mbo", "textures/foci/inspiration.png"),
                "focus.upgrade.mhealing.inspiration.name",
                "focus.upgrade.mhealing.inspiration.text",
                new AspectList().add(Aspect.AURA, 1)
        );

        sanity = new FocusUpgradeType(
                ThaumcraftConfig.SANITY_UPGRADE_ID,
                new ResourceLocation("mbo", "textures/foci/IconSanity.png"),
                "focus.upgrade.sanity.name",
                "focus.upgrade.sanity.text",
                new AspectList().add(Aspect.MIND, 1).add(Aspect.HEAL, 1)
        );

        corrosive = new FocusUpgradeType(
                ThaumcraftConfig.CORROSIVE_UPGRADE_ID,
                new ResourceLocation("mbo", "textures/foci/IconCorrosive.png"),
                "focus.upgrade.corrosive.name",
                "focus.upgrade.corrosive.text",
                new AspectList().add(Aspect.TAINT, 1).add(Aspect.POISON, 1)
        );

        persistent = new FocusUpgradeType(
                ThaumcraftConfig.PERSISTENT_UPGRADE_ID,
                new ResourceLocation("mbo", "textures/foci/IconPersistent.png"),
                "focus.upgrade.persistent.name",
                "focus.upgrade.persistent.text",
                new AspectList().add(Aspect.ARMOR, 1).add(Aspect.MOTION, 1).add(Aspect.ENERGY, 1)
        );

        diffusion = new FocusUpgradeType(
                ThaumcraftConfig.DIFFUSION_UPGRADE_ID,
                new ResourceLocation("mbo", "textures/foci/IconDiffusion.png"),
                "focus.upgrade.diffusion.name",
                "focus.upgrade.diffusion.text",
                new AspectList().add(Aspect.DARKNESS, 1).add(Aspect.ELDRITCH, 2).add(Aspect.AURA, 4)
        );

        tripleEye = new FocusUpgradeType(
                ThaumcraftConfig.TRIPLE_EYE_UPGRADE_ID,
                new ResourceLocation("mbo", "textures/foci/IconTripleEye.png"),
                "focus.upgrade.tripleeye.name",
                "focus.upgrade.tripleeye.text",
                new AspectList().add(Aspect.DARKNESS, 2).add(Aspect.SENSES, 3).add(Aspect.AURA, 2)
        );
        corrosive_darklight  = new FocusUpgradeType(
                ThaumcraftConfig.CORROSIVEDARKLIGHT_UPGRADE_ID,
                new ResourceLocation("mbo", "textures/foci/IconCorrosive.png"),
                "focus.upgrade.corrosive_darklight.name",
                "focus.upgrade.corrosive_darklight.text",
                new AspectList().add(Aspect.TAINT, 1).add(Aspect.POISON, 1)
        );
    }
}
