package ru.givler.mbo.integration.thaumcraft;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import ru.givler.mbo.integration.thaumcraft.item.focus.ItemFocusCleansing;
import ru.givler.mbo.integration.thaumcraft.item.focus.ItemFocusDarkMatter;
import ru.givler.mbo.integration.thaumcraft.item.focus.ItemFocusMHealing;
import ru.givler.mbo.integration.thaumcraft.item.focus.ItemFocusVisShard;
import ru.givler.mbo.integration.thaumcraft.item.focus.TMFocusUpgrades;
import ru.givler.mbo.integration.thaumcraft.registry.TMEntityRegistry;
import ru.givler.mbo.integration.thaumcraft.handler.ConfigHandler;

import net.minecraftforge.common.config.Configuration;

public class ThaumcraftRegistry {

    public static Item FocusHealing, FocusCleansing, FocusVisShard, FocusDarkMatter;

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

            FocusVisShard = new ItemFocusVisShard();
            FocusDarkMatter = new ItemFocusDarkMatter();

            GameRegistry.registerItem(FocusHealing, "focusMHealing");
            GameRegistry.registerItem(FocusCleansing, "focusCleansing");
            GameRegistry.registerItem(FocusVisShard, "focusVisShard");
            GameRegistry.registerItem(FocusDarkMatter, "focusDarkMatter");
        }
    }
}