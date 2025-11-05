package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.item.ItemArmor;
import ru.givler.mbo.item.ItemCustomArmor;

public class ArmorRegistry {
    public static ItemArmor.ArmorMaterial RustyKnight, RustyMercenary, RustyWanderer, RustyWizard;
    public static ItemCustomArmor KnightHelmet, KnightChest, KnightLegs, KnightBoots;
    public static ItemCustomArmor MercenaryHelmet, MercenaryChest, MercenaryLegs, MercenaryBoots;
    public static ItemCustomArmor WandererHelmet, WandererChest, WandererLegs, WandererBoots;
    public static ItemCustomArmor PyromancerHelmet, PyromancerChest, PyromancerLegs, PyromancerBoots;
    public static ItemCustomArmor ClericHelmet, ClericChest, ClericLegs, ClericBoots;
    public static ItemCustomArmor WizardHelmet, WizardChest, WizardLegs, WizardBoots;
    static {
        RustyKnight = ItemCustomArmor.createArmorMaterial("RustyKnight", 4,
                new int[]{2, 4, 3, 2}, 10);
        RustyMercenary = ItemCustomArmor.createArmorMaterial("RustyMercenary", 4,
                new int[]{1, 4, 3, 1}, 10);
        RustyWanderer = ItemCustomArmor.createArmorMaterial("RustyWanderer", 4,
                new int[]{1, 3, 2, 1}, 10);
        RustyWizard = ItemCustomArmor.createArmorMaterial("RustyWizard", 4,
                new int[]{1, 2, 2, 1}, 10);

        KnightHelmet = new ItemCustomArmor("KnightHelmet", "knight_helmet", "knight", RustyKnight, 0, 0);
        KnightChest = new ItemCustomArmor("KnightChest", "knight_chest", "knight", RustyKnight, 0, 1);
        KnightLegs = new ItemCustomArmor("KnightLegs", "knight_legs", "knight", RustyKnight, 0, 2);
        KnightBoots = new ItemCustomArmor("KnightBoots", "knight_boots", "knight", RustyKnight, 0, 3);

        MercenaryHelmet = new ItemCustomArmor("MercenaryHelmet", "mercenary_helmet", "mercenary", RustyMercenary, 0, 0);
        MercenaryChest = new ItemCustomArmor("MercenaryChest", "mercenary_chest", "mercenary", RustyMercenary, 0, 1);
        MercenaryLegs = new ItemCustomArmor("MercenaryLegs", "mercenary_legs", "mercenary", RustyMercenary, 0, 2);
        MercenaryBoots = new ItemCustomArmor("MercenaryBoots", "mercenary_boots", "mercenary", RustyMercenary, 0, 3);

        WandererHelmet = new ItemCustomArmor("WandererHelmet", "wanderer_helmet", "wanderer", RustyWanderer, 0, 0);
        WandererChest = new ItemCustomArmor("WandererChest", "wanderer_chest", "wanderer", RustyWanderer, 0, 1);
        WandererLegs = new ItemCustomArmor("WandererLegs", "wanderer_legs", "wanderer", RustyWanderer, 0, 2);
        WandererBoots = new ItemCustomArmor("WandererBoots", "wanderer_boots", "wanderer", RustyWanderer, 0, 3);

        PyromancerHelmet = new ItemCustomArmor("PyromancerHelmet", "pyromancer_helmet", "pyromancer", RustyWizard, 0, 0);
        PyromancerChest = new ItemCustomArmor("PyromancerChest", "pyromancer_chest", "pyromancer", RustyWizard, 0, 1);
        PyromancerLegs = new ItemCustomArmor("PyromancerLegs", "pyromancer_legs", "pyromancer", RustyWizard, 0, 2);
        PyromancerBoots = new ItemCustomArmor("PyromancerBoots", "pyromancer_boots", "pyromancer", RustyWizard, 0, 3);

        ClericHelmet = new ItemCustomArmor("ClericHelmet", "cleric_helmet", "cleric", RustyWizard, 0, 0);
        ClericChest = new ItemCustomArmor("ClericChest", "cleric_chest", "cleric", RustyWizard, 0, 1);
        ClericLegs = new ItemCustomArmor("ClericLegs", "cleric_legs", "cleric", RustyWizard, 0, 2);
        ClericBoots = new ItemCustomArmor("ClericBoots", "cleric_boots", "cleric", RustyWizard, 0, 3);

        WizardHelmet = new ItemCustomArmor("WizardHelmet", "wizard_helmet", "wizard", RustyWizard, 0, 0);
        WizardChest = new ItemCustomArmor("WizardChest", "wizard_chest", "wizard", RustyWizard, 0, 1);
        WizardLegs = new ItemCustomArmor("WizardLegs", "wizard_legs", "wizard", RustyWizard, 0, 2);
        WizardBoots = new ItemCustomArmor("WizardBoots", "wizard_boots", "wizard", RustyWizard, 0, 3);
    }
    @Mod.EventHandler
    public static void preLoad(FMLPreInitializationEvent event) {
        KnightHelmet.register();
        KnightChest.register();
        KnightLegs.register();
        KnightBoots.register();

        MercenaryHelmet.register();
        MercenaryChest.register();
        MercenaryLegs.register();
        MercenaryBoots.register();

        WandererHelmet.register();
        WandererChest.register();
        WandererLegs.register();
        WandererBoots.register();

        PyromancerHelmet.register();
        PyromancerChest.register();
        PyromancerLegs.register();
        PyromancerBoots.register();

        ClericHelmet.register();
        ClericChest.register();
        ClericLegs.register();
        ClericBoots.register();

        WizardHelmet.register();
        WizardChest.register();
        WizardLegs.register();
        WizardBoots.register();
    }

}
