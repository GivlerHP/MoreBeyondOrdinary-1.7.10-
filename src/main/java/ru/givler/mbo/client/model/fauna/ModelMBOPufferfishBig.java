package ru.givler.mbo.client.model.fauna;

/** Cube geometry ported from Mojang 26.3-snapshot-1; regenerate with tools/port_fish_models.py. */
public final class ModelMBOPufferfishBig extends ModelMBOFish {
  public ModelMBOPufferfishBig(float grow) {
    super("PufferfishBig");
    part("body", null, 0, 0,
        -4.0F, -8.0F, -4.0F, 8, 8, 8,
        0.0F, 22.0F, 0.0F, 0.0F, 0.0F, 0.0F, grow);
    part("right_blue_fin", null, 24, 0,
        -2.0F, 0.0F, -1.0F, 2, 1, 2,
        -4.0F, 15.0F, -2.0F, 0.0F, 0.0F, 0.0F, grow);
    part("left_blue_fin", null, 24, 3,
        0.0F, 0.0F, -1.0F, 2, 1, 2,
        4.0F, 15.0F, -2.0F, 0.0F, 0.0F, 0.0F, grow);
    part("top_front_fin", null, 15, 17,
        -4.0F, -1.0F, 0.0F, 8, 1, 0,
        0.0F, 14.0F, -4.0F, 0.78539816F, 0.0F, 0.0F, grow);
    part("top_middle_fin", null, 14, 16,
        -4.0F, -1.0F, 0.0F, 8, 1, 1,
        0.0F, 14.0F, 0.0F, 0.0F, 0.0F, 0.0F, grow);
    part("top_back_fin", null, 23, 18,
        -4.0F, -1.0F, 0.0F, 8, 1, 0,
        0.0F, 14.0F, 4.0F, -0.78539816F, 0.0F, 0.0F, grow);
    part("right_front_fin", null, 5, 17,
        -1.0F, -8.0F, 0.0F, 1, 8, 0,
        -4.0F, 22.0F, -4.0F, 0.0F, -0.78539816F, 0.0F, grow);
    part("left_front_fin", null, 1, 17,
        0.0F, -8.0F, 0.0F, 1, 8, 0,
        4.0F, 22.0F, -4.0F, 0.0F, 0.78539816F, 0.0F, grow);
    part("bottom_front_fin", null, 15, 20,
        -4.0F, 0.0F, 0.0F, 8, 1, 0,
        0.0F, 22.0F, -4.0F, -0.78539816F, 0.0F, 0.0F, grow);
    part("bottom_middle_fin", null, 15, 20,
        -4.0F, 0.0F, 0.0F, 8, 1, 0,
        0.0F, 22.0F, 0.0F, 0.0F, 0.0F, 0.0F, grow);
    part("bottom_back_fin", null, 15, 20,
        -4.0F, 0.0F, 0.0F, 8, 1, 0,
        0.0F, 22.0F, 4.0F, 0.78539816F, 0.0F, 0.0F, grow);
    part("right_back_fin", null, 9, 17,
        -1.0F, -8.0F, 0.0F, 1, 8, 0,
        -4.0F, 22.0F, 4.0F, 0.0F, 0.78539816F, 0.0F, grow);
    part("left_back_fin", null, 9, 17,
        0.0F, -8.0F, 0.0F, 1, 8, 0,
        4.0F, 22.0F, 4.0F, 0.0F, -0.78539816F, 0.0F, grow);
  }
}
