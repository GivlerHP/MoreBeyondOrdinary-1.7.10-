package ru.givler.mbo.client.model.monster;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelZombie;

/** Excalibur husk atlas uses mirrored base limbs; the lower left-limb regions are transparent. */
public final class ModelMBOHusk extends ModelZombie {
  public ModelMBOHusk() {
    super(0F, false);
    bipedBody.addChild(grown(16, 32, -4, 0, -2, 8, 12, 4, .25F));
    bipedRightArm.addChild(grown(40, 32, -3, -2, -2, 4, 12, 4, .25F));
    bipedLeftArm.addChild(grown(48, 48, -1, -2, -2, 4, 12, 4, .25F));
    bipedRightLeg.addChild(grown(0, 32, -2, 0, -2, 4, 12, 4, .25F));
    bipedLeftLeg.addChild(grown(0, 48, -2, 0, -2, 4, 12, 4, .25F));
  }

  private ModelRenderer grown(
      int u, int v, float x, float y, float z, int w, int h, int d, float grow) {
    ModelRenderer part = new ModelRenderer(this, u, v);
    part.addBox(x, y, z, w, h, d, grow);
    return part;
  }
}
