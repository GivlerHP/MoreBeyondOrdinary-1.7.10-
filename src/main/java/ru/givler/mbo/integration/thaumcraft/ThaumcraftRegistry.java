package ru.givler.mbo.integration.thaumcraft;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import ru.givler.mbo.integration.thaumcraft.item.focus.ItemFocusCleansing;
import ru.givler.mbo.integration.thaumcraft.item.focus.ItemFocusMHealing;

public class ThaumcraftRegistry {

    public static Item FocusHealing, FocusCleansing;

    public static void init() {
        if (Loader.isModLoaded("Thaumcraft")) {
            FocusHealing = new ItemFocusMHealing();
            FocusCleansing = new ItemFocusCleansing();

            GameRegistry.registerItem(FocusHealing, "focusMHealing");
            GameRegistry.registerItem(FocusCleansing, "focusCleansing");
        }
    }
}
