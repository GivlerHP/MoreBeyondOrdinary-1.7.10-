package ru.givler.mbo.integration.minefantasy2.knowledge;

import minefantasy.mf2.api.knowledge.InformationBase;
import minefantasy.mf2.knowledge.KnowledgeListMF;
import net.minecraft.init.Blocks;

public final class KnowledgeListMBO {
    public static InformationBase buildingFittings;

    private KnowledgeListMBO() {}

    public static void init() {
        buildingFittings = new InformationBase("MBOBuildingFittings", 2, 0, 0, Blocks.trapdoor,
                KnowledgeListMF.gettingStarted).registerStat().setUnlocked();
    }
}
