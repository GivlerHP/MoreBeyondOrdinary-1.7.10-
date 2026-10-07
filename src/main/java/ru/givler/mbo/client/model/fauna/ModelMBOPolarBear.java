package ru.givler.mbo.client.model.fauna;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.fauna.EntityMBOPolarBear;

/** Adult and cub geometry and standing animation from 26.3-snapshot-1. */
public final class ModelMBOPolarBear extends ModelBase {
  private final ModelRenderer head, body;
  private final ModelRenderer[] legs = new ModelRenderer[4];
  private final boolean cub;

  public ModelMBOPolarBear(boolean cub) {
    this.cub = cub;
    textureWidth = cub ? 64 : 128;
    textureHeight = 64;
    head = new ModelRenderer(this);
    body = new ModelRenderer(this);
    if (cub) {
      box(head, 0, 0, -3, -2.625F, -4.25F, 6, 5, 4);
      box(head, 20, 3, -2, .375F, -6.25F, 4, 2, 2);
      box(head, 20, 0, -4, -3.625F, -2.75F, 2, 2, 1);
      box(head, 26, 0, 2, -3.625F, -2.75F, 2, 2, 1);
      box(body, 0, 9, -4, -3.5F, -6, 8, 7, 12);
      for (int i = 0; i < 4; i++) {
        legs[i] = new ModelRenderer(this);
        box(legs[i], i % 2 * 12, i < 2 ? 34 : 28, -1.5F, -.5F, -1.5F, 3, 3, 3);
      }
    } else {
      box(head, 0, 0, -3.5F, -3, -3, 7, 7, 7);
      box(head, 0, 44, -2.5F, 1, -6, 5, 3, 3);
      box(head, 26, 0, -4.5F, -4, -1, 2, 2, 1);
      head.mirror = true;
      box(head, 26, 0, 2.5F, -4, -1, 2, 2, 1);
      box(body, 0, 19, -5, -13, -7, 14, 14, 11);
      box(body, 39, 0, -4, -25, -7, 12, 12, 10);
      for (int i = 0; i < 4; i++) {
        legs[i] = new ModelRenderer(this);
        box(legs[i], 50, i < 2 ? 22 : 40, -2, 0, -2, 4, 10, i < 2 ? 8 : 6);
      }
    }
  }

  private void box(
      ModelRenderer part, int u, int v, float x, float y, float z, int w, int h, int d) {
    part.setTextureOffset(u, v);
    part.addBox(x, y, z, w, h, d);
  }

  @Override
  public void render(
      Entity entity, float phase, float amount, float age, float yaw, float pitch, float scale) {
    EntityMBOPolarBear bear = (EntityMBOPolarBear) entity;
    float stand = bear.standScale(age - bear.ticksExisted);
    stand *= stand;
    head.setRotationPoint(0, cub ? 18.625F : 10, cub ? -5.75F : -16);
    head.rotateAngleY = yaw / (180F / (float) Math.PI);
    head.rotateAngleX = pitch / (180F / (float) Math.PI);
    body.setRotationPoint(cub ? 0 : -2, cub ? 17.5F : 9, cub ? 0 : 12);
    body.rotateAngleX = cub ? 0 : (float) Math.PI / 2;
    for (int i = 0; i < 4; i++) {
      legs[i].setRotationPoint(
          (i % 2 == 0 ? -1 : 1) * (cub ? 2.5F : i < 2 ? 4.5F : 3.5F),
          cub ? 21.5F : 14,
          cub ? (i < 2 ? 4.5F : -4.5F) : (i < 2 ? 6 : -8));
      legs[i].rotateAngleX =
          MathHelper.cos(phase * .6662F + ((i == 0 || i == 3) ? 0 : (float) Math.PI))
              * 1.4F
              * amount;
    }
    body.rotateAngleX -= stand * (float) Math.PI * .35F;
    body.rotationPointY += stand * (cub ? .5F : 1F) * 2;
    for (int i = 2; i < 4; i++) {
      legs[i].rotationPointY -= stand * (cub ? .5F : 1F) * 20;
      legs[i].rotationPointZ += stand * (cub ? .5F : 1F) * 4;
      legs[i].rotateAngleX -= stand * (float) Math.PI * .45F;
    }
    head.rotationPointY -= stand * 24;
    head.rotationPointZ += stand * 13;
    head.rotateAngleX += stand * (float) Math.PI * .15F;
    GL11.glPushMatrix();
    if (!cub) {
      GL11.glTranslatef(0, -.3F, 0);
      GL11.glScalef(1.2F, 1.2F, 1.2F);
    }
    head.render(scale);
    body.render(scale);
    for (ModelRenderer leg : legs) leg.render(scale);
    GL11.glPopMatrix();
  }
}
