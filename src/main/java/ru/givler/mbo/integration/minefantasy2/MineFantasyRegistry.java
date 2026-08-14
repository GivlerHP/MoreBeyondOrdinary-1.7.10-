package ru.givler.mbo.integration.minefantasy2;

import cpw.mods.fml.common.Loader;
import minefantasy.mf2.block.list.BlockListMF;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import ru.givler.mbo.integration.wood.WoodFamily;

public final class MineFantasyRegistry {
    private static final String[] NAMES = {"MFNailed", "MFRefined", "MFYew", "MFIronbark", "MFEbony"};
    public static WoodFamily[] families;

    private MineFantasyRegistry() { }

    public static void init() {
        if (!Loader.isModLoaded("minefantasy2") && !Loader.isModLoaded("MineFantasy2")) return;

        Block[] planks = {
                BlockListMF.nailed_planks, BlockListMF.refined_planks, BlockListMF.yew_planks,
                BlockListMF.ironbark_planks, BlockListMF.ebony_planks
        };
        families = new WoodFamily[planks.length];
        for (int i = 0; i < planks.length; ++i) {
            if (planks[i] != null) families[i] = new WoodFamily(NAMES[i], planks[i], 0, CreativeTabs.tabBlock);
        }
    }
}
