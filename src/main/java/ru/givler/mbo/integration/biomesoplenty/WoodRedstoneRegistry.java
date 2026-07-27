package ru.givler.mbo.integration.biomesoplenty;

import cpw.mods.fml.common.Loader;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import ru.givler.mbo.block.BlockBasicWoodButton;
import ru.givler.mbo.block.BlockBasicWoodPressurePlate;

public final class WoodRedstoneRegistry {
    private static final String[] VANILLA_NAMES =
            {"Spruce", "Birch", "Jungle", "Acacia", "DarkOak"};
    private static final String[] BOP_NAMES = {
            "Sacred", "Cherry", "Dark", "Fir", "Ethereal",
            "Magic", "Mangrove", "Palm", "Redwood", "Willow",
            "Bamboo", "Pine", "Hellbark", "Jacaranda", "Mahogany"
    };

    public static BlockBasicWoodButton[] vanillaButtons;
    public static BlockBasicWoodPressurePlate[] vanillaPressurePlates;
    public static BlockBasicWoodButton[] bopButtons;
    public static BlockBasicWoodPressurePlate[] bopPressurePlates;

    private WoodRedstoneRegistry() { }

    public static void init() {
        vanillaButtons = new BlockBasicWoodButton[VANILLA_NAMES.length];
        vanillaPressurePlates = new BlockBasicWoodPressurePlate[VANILLA_NAMES.length];
        for (int i = 0; i < VANILLA_NAMES.length; ++i) {
            int plankMeta = i + 1;
            vanillaButtons[i] = new BlockBasicWoodButton(
                    "Button" + VANILLA_NAMES[i], Blocks.planks, plankMeta);
            vanillaPressurePlates[i] = new BlockBasicWoodPressurePlate(
                    "PressurePlate" + VANILLA_NAMES[i], Blocks.planks, plankMeta);
            vanillaButtons[i].addStandardRecipe();
            vanillaPressurePlates[i].addStandardRecipe();
        }

        if (Loader.isModLoaded("BiomesOPlenty")) {
            Block planks = biomesoplenty.api.content.BOPCBlocks.planks;
            bopButtons = new BlockBasicWoodButton[BOP_NAMES.length];
            bopPressurePlates = new BlockBasicWoodPressurePlate[BOP_NAMES.length];
            for (int i = 0; i < BOP_NAMES.length; ++i) {
                bopButtons[i] = new BlockBasicWoodButton(
                        "ButtonBoP" + BOP_NAMES[i], planks, i);
                bopPressurePlates[i] = new BlockBasicWoodPressurePlate(
                        "PressurePlateBoP" + BOP_NAMES[i], planks, i);
                bopButtons[i].addStandardRecipe();
                bopPressurePlates[i].addStandardRecipe();
            }
        }
    }

    public static void setBoPCreativeTab(CreativeTabs tab) {
        if (bopButtons != null) for (Block block : bopButtons) block.setCreativeTab(tab);
        if (bopPressurePlates != null) for (Block block : bopPressurePlates) block.setCreativeTab(tab);
    }
}
