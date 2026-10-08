package ru.givler.mbo.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import java.util.Map;

@IFMLLoadingPlugin.Name("MBOCore")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"ru.givler.mbo.core", "ru.givler.mbo.integration.thaumcraft.core"})
public class MBOCorePlugin implements IFMLLoadingPlugin {
  public String[] getASMTransformerClass() {
    return new String[] {
      "ru.givler.mbo.core.TickRateTransformer",
      "ru.givler.mbo.core.BlockButtonTransformer",
      "ru.givler.mbo.core.BoatCreativeTransformer",
      "ru.givler.mbo.core.BoatRiderRenderTransformer",
      "ru.givler.mbo.core.TrapdoorPlacementTransformer",
      "ru.givler.mbo.core.LadderTransformer",
      "ru.givler.mbo.core.RailTransformer",
      "ru.givler.mbo.core.CauldronTransformer",
      "ru.givler.mbo.core.PistonTransformer",
      "ru.givler.mbo.core.FenceConnectionTransformer",
      "ru.givler.mbo.core.PaneTransformer",
      "ru.givler.mbo.core.LootingPotionTransformer",
      "ru.givler.mbo.core.SmoothOpeningTransformer",
      "ru.givler.mbo.core.TessellatorEmptyBufferTransformer",
      "ru.givler.mbo.core.ThaumcraftFontTransformer",
      "ru.givler.mbo.integration.thaumcraft.core.ThaumometerLensTransformer",
      "ru.givler.mbo.core.TooltipFrameTransformer",
      "ru.givler.mbo.core.ItemTooltipContextTransformer",
      "ru.givler.mbo.core.NeiTooltipFrameTransformer",
      "ru.givler.mbo.core.NeiRecipeTextColorTransformer",
      "ru.givler.mbo.core.PotionDurationFontTransformer",
      "ru.givler.mbo.core.PotionParticleTransformer",
      "ru.givler.mbo.core.AirRefillTransformer",
      "ru.givler.mbo.core.AirHudTransformer",
      "ru.givler.mbo.core.ColoredBurningTransformer",
      "ru.givler.mbo.core.SpectatorCollisionTransformer",
      "ru.givler.mbo.core.WaterloggingFlowTransformer",
      "ru.givler.mbo.core.WaterloggingNeighborTransformer",
      "ru.givler.mbo.core.ObserverTransformer",
      "ru.givler.mbo.core.WaterloggingEntityTransformer",
      "ru.givler.mbo.core.WaterloggingCameraTransformer",
      "ru.givler.mbo.core.WaterloggingMovementTransformer",
      "ru.givler.mbo.core.WaterloggingRenderTransformer",
      "ru.givler.mbo.core.WaterloggedPaneChunkTransformer",
      "ru.givler.mbo.core.PlatformClippingTransformer",
      "ru.givler.mbo.core.PlatformPassengerBodyTransformer",
      "ru.givler.mbo.core.WaterloggingLiquidHeightTransformer",
      "ru.givler.mbo.core.WaterloggingFarmlandTransformer",
      "ru.givler.mbo.core.GuiStatsTransformer",
      "ru.givler.mbo.core.PlayerPingTransformer",
      "ru.givler.mbo.core.SwimmingTransformer",
      "ru.givler.mbo.core.SkeletonCombatTransformer",
      "ru.givler.mbo.core.SkeletonPoseTransformer"
    };
  }

  public String getModContainerClass() {
    return null;
  }

  public String getSetupClass() {
    return null;
  }

  public void injectData(Map<String, Object> data) {}

  public String getAccessTransformerClass() {
    return null;
  }
}
