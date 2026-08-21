package ru.givler.mbo.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.Name("MBOCore")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"ru.givler.mbo.core"})
public class MBOCorePlugin implements IFMLLoadingPlugin {
    public String[] getASMTransformerClass() {
        return new String[]{
                "ru.givler.mbo.core.BlockButtonTransformer",
                "ru.givler.mbo.core.TrapdoorPlacementTransformer",
                "ru.givler.mbo.core.LadderTransformer",
                "ru.givler.mbo.core.RailTransformer",
                "ru.givler.mbo.core.CauldronTransformer",
                "ru.givler.mbo.core.PistonTransformer",
                "ru.givler.mbo.core.FenceConnectionTransformer",
                "ru.givler.mbo.core.LootingPotionTransformer"
                ,"ru.givler.mbo.core.SmoothOpeningTransformer"
        };
    }
    public String getModContainerClass() { return null; }
    public String getSetupClass() { return null; }
    public void injectData(Map<String, Object> data) { }
    public String getAccessTransformerClass() { return null; }
}
