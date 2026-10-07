package ru.givler.mbo.client.model.fauna;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/** Exact modern adult and baby squid meshes, rather than scaling the adult. */
public final class ModelMBOGlowSquid extends ModelBase {
  private final ModelRenderer body, tentacles[] = new ModelRenderer[8];

  public ModelMBOGlowSquid(boolean baby) {
    textureWidth = baby ? 32 : 64;
    textureHeight = 32;
    body = new ModelRenderer(this, 0, 0);
    body.addBox(
        baby ? -4 : -6,
        baby ? -5 : -8,
        baby ? -4 : -6,
        baby ? 8 : 12,
        baby ? 10 : 16,
        baby ? 8 : 12,
        baby ? 0 : .02F);
    body.setRotationPoint(0, baby ? 13 : 8, 0);
    for (int i = 0; i < 8; i++) {
      ModelRenderer leg = new ModelRenderer(this, baby ? 0 : 48, baby ? 18 : 0);
      leg.addBox(-1, baby ? -.5F : 0, -1, 2, baby ? 6 : 18, 2);
      double angle = i * Math.PI * 2 / 8;
      leg.setRotationPoint(
          (float) Math.cos(angle) * (baby ? 3 : 5),
          baby ? 18.5F : 15,
          (float) Math.sin(angle) * (baby ? 3 : 5));
      leg.rotateAngleY = (float) (-angle + Math.PI / 2);
      tentacles[i] = leg;
    }
  }

  @Override
  public void render(
      Entity entity, float phase, float amount, float age, float yaw, float pitch, float scale) {
    body.render(scale);
    for (ModelRenderer leg : tentacles) {
      leg.rotateAngleX = age;
      leg.render(scale);
    }
  }
}
