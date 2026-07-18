package ru.givler.mbo.integration.biomesoplenty;

import cpw.mods.fml.common.Loader;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import ru.givler.mbo.block.BlockBasicFence;
import ru.givler.mbo.block.BlockBasicFenceGate;
import ru.givler.mbo.config.IntegrationConfig;

public final class FenceRegistry {

    private static final String[] WOOD_NAMES = {
            "Sacred", "Cherry", "Dark", "Fir", "Ethereal",
            "Magic", "Mangrove", "Palm", "Redwood", "Willow",
            "Bamboo", "Pine", "Hellbark", "Jacaranda", "Mahogany"
    };

    public static BlockBasicFence FenceBoP;
    public static BlockBasicFenceGate[] FenceGatesBoP;

    private FenceRegistry() { }

    public static void init() {
        if (!Loader.isModLoaded("BiomesOPlenty")) {
            return;
        }

        Block bopPlanks = biomesoplenty.api.content.BOPCBlocks.planks;

        if (IntegrationConfig.enableBoPFences) {
            FenceBoP = new BlockBasicFence("FenceBoP", bopPlanks,
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14);
            FenceBoP.addStandardRecipes();
        }

        if (IntegrationConfig.enableBoPFenceGates) {
            FenceGatesBoP = new BlockBasicFenceGate[WOOD_NAMES.length];
            for (int meta = 0; meta < WOOD_NAMES.length; ++meta) {
                FenceGatesBoP[meta] = new BlockBasicFenceGate(
                        "FenceGateBoP" + WOOD_NAMES[meta], bopPlanks, meta);
                FenceGatesBoP[meta].addStandardRecipe();
            }
        }
    }

    public static void setCreativeTab(CreativeTabs tab) {
        if (FenceBoP != null) {
            FenceBoP.setCreativeTab(tab);
        }
        if (FenceGatesBoP != null) {
            for (BlockBasicFenceGate gate : FenceGatesBoP) {
                gate.setCreativeTab(tab);
            }
        }
    }
}
