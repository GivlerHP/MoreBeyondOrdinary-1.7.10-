package ru.givler.mbo.client.model.fauna;

/** Cube geometry ported from Mojang 26.3-snapshot-1; regenerate with tools/port_fish_models.py. */
public final class ModelMBOPufferfishSmall extends ModelMBOFish {
  public ModelMBOPufferfishSmall(float grow) {
    super("PufferfishSmall");
    part("body", null, 0, 27,
        -1.5F, -2.0F, -1.5F, 3, 2, 3,
        0.0F, 23.0F, 0.0F, 0.0F, 0.0F, 0.0F, grow);
    part("right_eye", null, 24, 6,
        -1.5F, 0.0F, -1.5F, 1, 1, 1,
        0.0F, 20.0F, 0.0F, 0.0F, 0.0F, 0.0F, grow);
    part("left_eye", null, 28, 6,
        0.5F, 0.0F, -1.5F, 1, 1, 1,
        0.0F, 20.0F, 0.0F, 0.0F, 0.0F, 0.0F, grow);
    part("back_fin", null, -3, 0,
        -1.5F, 0.0F, 0.0F, 3, 0, 3,
        0.0F, 22.0F, 1.5F, 0.0F, 0.0F, 0.0F, grow);
    part("right_fin", null, 25, 0,
        -1.0F, 0.0F, 0.0F, 1, 0, 2,
        -1.5F, 22.0F, -1.5F, 0.0F, 0.0F, 0.0F, grow);
    part("left_fin", null, 25, 0,
        0.0F, 0.0F, 0.0F, 1, 0, 2,
        1.5F, 22.0F, -1.5F, 0.0F, 0.0F, 0.0F, grow);
  }
}
