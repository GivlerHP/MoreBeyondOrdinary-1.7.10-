package ru.givler.mbo.integration.thaumcraft;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import ru.givler.mbo.integration.thaumcraft.item.focus.*;
import ru.givler.mbo.integration.thaumcraft.item.staff.*;
import ru.givler.mbo.integration.thaumcraft.registry.TMEntityRegistry;
import ru.givler.mbo.integration.thaumcraft.handler.ConfigHandler;

import net.minecraftforge.common.config.Configuration;

public class ThaumcraftRegistry {

    public static Item FocusHealing, FocusCleansing, FocusVisShard, FocusDarkMatter, FocusDarkLightning;
    public static Item StaffFire, StaffNature, StaffFrost, StaffLantern, StaffLight, StaffChillSorrow, StaffNaturalMoon,
            StaffLightningDragon, StaffDarkMoon, StaffDemonic;

    public static void preLoad(FMLPreInitializationEvent event){
        if (Loader.isModLoaded("Thaumcraft")) {
            ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile());
            ConfigHandler.initConfig();
            TMFocusUpgrades.initFocusUpgrades();
        }
    }

    public static void init() {
        if (Loader.isModLoaded("Thaumcraft")) {

            TMEntityRegistry.initEntities();

            FocusHealing = new ItemFocusMHealing();
            FocusCleansing = new ItemFocusCleansing();

            FocusDarkLightning = new ItemFocusDarkLightning();

            FocusVisShard = new ItemFocusVisShard();
            FocusDarkMatter = new ItemFocusDarkMatter();

            StaffFire = new ItemStaffFire();
            StaffNature = new ItemStaffNature();
            StaffFrost = new ItemStaffFrost();
            StaffLantern = new ItemStaffLantern();
            StaffLight = new ItemStaffLight();
            StaffChillSorrow = new ItemStaffChillSorrow();
            StaffNaturalMoon = new ItemStaffNaturalMoon();
            StaffLightningDragon = new ItemStaffLightningDragon();
            StaffDarkMoon = new ItemStaffDarkMoon();
            StaffDemonic = new ItemStaffDemonic();

            GameRegistry.registerItem(FocusHealing, "focusMHealing");
            GameRegistry.registerItem(FocusCleansing, "focusCleansing");
            GameRegistry.registerItem(FocusDarkLightning, "focusDarkLightning");
            GameRegistry.registerItem(FocusVisShard, "focusVisShard");
            GameRegistry.registerItem(FocusDarkMatter, "focusDarkMatter");

            GameRegistry.registerItem(StaffFire, "staffFire");
            GameRegistry.registerItem(StaffNature, "staffNature");
            GameRegistry.registerItem(StaffFrost, "staffFrost");
            GameRegistry.registerItem(StaffLantern, "staffLantern");
            GameRegistry.registerItem(StaffLight, "staffLight");
            GameRegistry.registerItem(StaffChillSorrow, "staffChillSorrow");
            GameRegistry.registerItem(StaffNaturalMoon, "staffNaturalMoon");
            GameRegistry.registerItem(StaffLightningDragon, "staffLightningDragon");
            GameRegistry.registerItem(StaffDarkMoon, "staffDarkMoon");
            GameRegistry.registerItem(StaffDemonic, "staffDemonic");
        }
    }
}