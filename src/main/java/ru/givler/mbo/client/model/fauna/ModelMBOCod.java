package ru.givler.mbo.client.model.fauna;

/** Cube geometry ported from Mojang 26.3-snapshot-1; regenerate with tools/port_fish_models.py. */
public final class ModelMBOCod extends ModelMBOFish {
  public ModelMBOCod(float grow) {
    super("Cod");
    part("body", null, 0, 0,
        -1.0F, -2.0F, 0.0F, 2, 4, 7,
        0.0F, 22.0F, 0.0F, 0.0F, 0.0F, 0.0F, grow);
    part("head", null, 11, 0,
        -1.0F, -2.0F, -3.0F, 2, 4, 3,
        0.0F, 22.0F, 0.0F, 0.0F, 0.0F, 0.0F, grow);
    part("nose", null, 0, 0,
        -1.0F, -2.0F, -1.0F, 2, 3, 1,
        0.0F, 22.0F, -3.0F, 0.0F, 0.0F, 0.0F, grow);
    part("right_fin", null, 22, 1,
        -2.0F, 0.0F, -1.0F, 2, 0, 2,
        -1.0F, 23.0F, 0.0F, 0.0F, 0.0F, -0.78539816F, grow);
    part("left_fin", null, 22, 4,
        0.0F, 0.0F, -1.0F, 2, 0, 2,
        1.0F, 23.0F, 0.0F, 0.0F, 0.0F, 0.78539816F, grow);
    part("tail_fin", null, 22, 3,
        0.0F, -2.0F, 0.0F, 0, 4, 4,
        0.0F, 22.0F, 7.0F, 0.0F, 0.0F, 0.0F, grow);
    part("top_fin", null, 20, -6,
        0.0F, -1.0F, -1.0F, 0, 1, 6,
        0.0F, 20.0F, 0.0F, 0.0F, 0.0F, 0.0F, grow);
  }
}
