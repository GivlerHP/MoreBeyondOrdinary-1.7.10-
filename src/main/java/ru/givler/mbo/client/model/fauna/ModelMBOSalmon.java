package ru.givler.mbo.client.model.fauna;

/** Cube geometry ported from Mojang 26.3-snapshot-1; regenerate with tools/port_fish_models.py. */
public final class ModelMBOSalmon extends ModelMBOFish {
  public ModelMBOSalmon(float grow) {
    super("Salmon");
    part("body_front", null, 0, 0,
        -1.5F, -2.5F, 0.0F, 3, 5, 8,
        0.0F, 20.0F, -7.2F, 0.0F, 0.0F, 0.0F, grow);
    part("body_back", null, 0, 13,
        -1.5F, -2.5F, 0.0F, 3, 5, 8,
        0.0F, 20.0F, 0.8000002F, 0.0F, 0.0F, 0.0F, grow);
    part("head", null, 22, 0,
        -1.0F, -2.0F, -3.0F, 2, 4, 3,
        0.0F, 20.0F, -7.2F, 0.0F, 0.0F, 0.0F, grow);
    part("back_fin", "body_back", 20, 10,
        0.0F, -2.5F, 0.0F, 0, 5, 6,
        0.0F, 0.0F, 8.0F, 0.0F, 0.0F, 0.0F, grow);
    part("top_front_fin", "body_front", 2, 1,
        0.0F, 0.0F, 0.0F, 0, 2, 3,
        0.0F, -4.5F, 5.0F, 0.0F, 0.0F, 0.0F, grow);
    part("top_back_fin", "body_back", 0, 2,
        0.0F, 0.0F, 0.0F, 0, 2, 4,
        0.0F, -4.5F, -1.0F, 0.0F, 0.0F, 0.0F, grow);
    part("right_fin", null, -4, 0,
        -2.0F, 0.0F, 0.0F, 2, 0, 2,
        -1.5F, 21.5F, -7.2F, 0.0F, 0.0F, -0.78539816F, grow);
    part("left_fin", null, 0, 0,
        0.0F, 0.0F, 0.0F, 2, 0, 2,
        1.5F, 21.5F, -7.2F, 0.0F, 0.0F, 0.78539816F, grow);
  }
}
