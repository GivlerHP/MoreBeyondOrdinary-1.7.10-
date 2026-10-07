package ru.givler.mbo.client.model.fauna;

/** Cube geometry ported from Mojang 26.3-snapshot-1; regenerate with tools/port_fish_models.py. */
public final class ModelMBOTropicalFishSmall extends ModelMBOFish {
  public ModelMBOTropicalFishSmall(float grow) {
    super("TropicalFishSmall");
    part("body", null, 0, 0,
        -1.0F, -1.5F, -3.0F, 2, 3, 6,
        0.0F, 22.0F, 0.0F, 0.0F, 0.0F, 0.0F, grow);
    part("tail", null, 22, -6,
        0.0F, -1.5F, 0.0F, 0, 3, 6,
        0.0F, 22.0F, 3.0F, 0.0F, 0.0F, 0.0F, grow);
    part("right_fin", null, 2, 16,
        -2.0F, -1.0F, 0.0F, 2, 2, 0,
        -1.0F, 22.5F, 0.0F, 0.0F, 0.78539816F, 0.0F, grow);
    part("left_fin", null, 2, 12,
        0.0F, -1.0F, 0.0F, 2, 2, 0,
        1.0F, 22.5F, 0.0F, 0.0F, -0.78539816F, 0.0F, grow);
    part("top_fin", null, 10, -5,
        0.0F, -3.0F, 0.0F, 0, 3, 6,
        0.0F, 20.5F, -3.0F, 0.0F, 0.0F, 0.0F, grow);
  }
}
