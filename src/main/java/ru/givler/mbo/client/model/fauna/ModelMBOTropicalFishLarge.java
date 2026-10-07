package ru.givler.mbo.client.model.fauna;

/** Cube geometry ported from Mojang 26.3-snapshot-1; regenerate with tools/port_fish_models.py. */
public final class ModelMBOTropicalFishLarge extends ModelMBOFish {
  public ModelMBOTropicalFishLarge(float grow) {
    super("TropicalFishLarge");
    part("body", null, 0, 20,
        -1.0F, -3.0F, -3.0F, 2, 6, 6,
        0.0F, 19.0F, 0.0F, 0.0F, 0.0F, 0.0F, grow);
    part("tail", null, 21, 16,
        0.0F, -3.0F, 0.0F, 0, 6, 5,
        0.0F, 19.0F, 3.0F, 0.0F, 0.0F, 0.0F, grow);
    part("right_fin", null, 2, 16,
        -2.0F, 0.0F, 0.0F, 2, 2, 0,
        -1.0F, 20.0F, 0.0F, 0.0F, 0.78539816F, 0.0F, grow);
    part("left_fin", null, 2, 12,
        0.0F, 0.0F, 0.0F, 2, 2, 0,
        1.0F, 20.0F, 0.0F, 0.0F, -0.78539816F, 0.0F, grow);
    part("top_fin", null, 20, 11,
        0.0F, -4.0F, 0.0F, 0, 4, 6,
        0.0F, 16.0F, -3.0F, 0.0F, 0.0F, 0.0F, grow);
    part("bottom_fin", null, 20, 21,
        0.0F, 0.0F, 0.0F, 0, 4, 6,
        0.0F, 22.0F, -3.0F, 0.0F, 0.0F, 0.0F, grow);
  }
}
