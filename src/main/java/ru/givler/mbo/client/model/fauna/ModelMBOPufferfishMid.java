package ru.givler.mbo.client.model.fauna;

/** Cube geometry ported from Mojang 26.3-snapshot-1; regenerate with tools/port_fish_models.py. */
public final class ModelMBOPufferfishMid extends ModelMBOFish {
  public ModelMBOPufferfishMid(float grow) {
    super("PufferfishMid");
    part("body", null, 12, 22,
        -2.5F, -5.0F, -2.5F, 5, 5, 5,
        0.0F, 22.0F, 0.0F, 0.0F, 0.0F, 0.0F, grow);
    part("right_blue_fin", null, 24, 0,
        -2.0F, 0.0F, 0.0F, 2, 0, 2,
        -2.5F, 18.0F, -1.5F, 0.0F, 0.0F, 0.0F, grow);
    part("left_blue_fin", null, 24, 3,
        0.0F, 0.0F, 0.0F, 2, 0, 2,
        2.5F, 18.0F, -1.5F, 0.0F, 0.0F, 0.0F, grow);
    part("top_front_fin", null, 19, 17,
        -2.5F, -1.0F, 0.0F, 5, 1, 0,
        0.0F, 17.0F, -2.5F, 0.78539816F, 0.0F, 0.0F, grow);
    part("top_back_fin", null, 11, 17,
        -2.5F, -1.0F, 0.0F, 5, 1, 0,
        0.0F, 17.0F, 2.5F, -0.78539816F, 0.0F, 0.0F, grow);
    part("right_front_fin", null, 5, 17,
        -1.0F, -5.0F, 0.0F, 1, 5, 0,
        -2.5F, 22.0F, -2.5F, 0.0F, -0.78539816F, 0.0F, grow);
    part("right_back_fin", null, 9, 17,
        -1.0F, -5.0F, 0.0F, 1, 5, 0,
        -2.5F, 22.0F, 2.5F, 0.0F, 0.78539816F, 0.0F, grow);
    part("left_back_fin", null, 1, 17,
        0.0F, -5.0F, 0.0F, 1, 5, 0,
        2.5F, 22.0F, 2.5F, 0.0F, -0.78539816F, 0.0F, grow);
    part("left_front_fin", null, 1, 17,
        0.0F, -5.0F, 0.0F, 1, 5, 0,
        2.5F, 22.0F, -2.5F, 0.0F, 0.78539816F, 0.0F, grow);
    part("bottom_back_fin", null, 18, 20,
        0.0F, 0.0F, 0.0F, 5, 1, 0,
        -2.5F, 22.0F, 2.5F, 0.78539816F, 0.0F, 0.0F, grow);
    part("bottom_front_fin", null, 17, 19,
        -2.5F, 0.0F, 0.0F, 5, 1, 1,
        0.0F, 22.0F, -2.5F, -0.78539816F, 0.0F, 0.0F, grow);
  }
}
