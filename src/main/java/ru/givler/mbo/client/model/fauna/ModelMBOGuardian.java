package ru.givler.mbo.client.model.fauna;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import ru.givler.mbo.entity.fauna.EntityMBOGuardian;

public final class ModelMBOGuardian extends ModelBase {
  private static final float[] XR = {1.75F, .25F, 0, 0, .5F, .5F, .5F, .5F, 1.25F, .75F, 0, 0},
      YR = {0, 0, 0, 0, .25F, 1.75F, 1.25F, .75F, 0, 0, 0, 0},
      ZR = {0, 0, .25F, 1.75F, 0, 0, 0, 0, 0, 0, .75F, 1.25F};
  private static final float[] X = {0, 0, 8, -8, -8, 8, 8, -8, 0, 0, 8, -8},
      Y = {-8, -8, -8, -8, 0, 0, 0, 0, 8, 8, 8, 8},
      Z = {8, -8, 0, 0, -8, -8, 8, 8, 8, -8, 0, 0};
  private final ModelRenderer head,
      eye,
      spikes[] = new ModelRenderer[12],
      tail[] = new ModelRenderer[3];

  public ModelMBOGuardian() {
    textureWidth = textureHeight = 64;
    head = new ModelRenderer(this, 0, 0);
    head.addBox(-6, 10, -8, 12, 12, 16);
    head.setTextureOffset(0, 28).addBox(-8, 10, -6, 2, 12, 12);
    head.mirror = true;
    head.addBox(6, 10, -6, 2, 12, 12);
    head.mirror = false;
    head.setTextureOffset(16, 40).addBox(-6, 8, -6, 12, 2, 12);
    head.addBox(-6, 22, -6, 12, 2, 12);
    for (int i = 0; i < 12; i++) {
      ModelRenderer part = new ModelRenderer(this, 0, 0);
      part.addBox(-1, -4.5F, -1, 2, 9, 2);
      part.rotateAngleX = (float) Math.PI * XR[i];
      part.rotateAngleY = (float) Math.PI * YR[i];
      part.rotateAngleZ = (float) Math.PI * ZR[i];
      spikes[i] = part;
      head.addChild(part);
    }
    eye = new ModelRenderer(this, 8, 0);
    eye.addBox(-1, 15, 0, 2, 2, 1);
    eye.setRotationPoint(0, 0, -8.25F);
    head.addChild(eye);
    tail[0] = new ModelRenderer(this, 40, 0);
    tail[0].addBox(-2, 14, 7, 4, 4, 8);
    head.addChild(tail[0]);
    tail[1] = new ModelRenderer(this, 0, 54);
    tail[1].addBox(0, 14, 0, 3, 3, 7);
    tail[1].setRotationPoint(-1.5F, .5F, 14);
    tail[0].addChild(tail[1]);
    tail[2] = new ModelRenderer(this, 41, 32);
    tail[2].addBox(0, 14, 0, 2, 2, 6);
    tail[2].setTextureOffset(25, 19).addBox(1, 10.5F, 3, 1, 9, 9);
    tail[2].setRotationPoint(.5F, .5F, 6);
    tail[1].addChild(tail[2]);
  }

  @Override
  public void render(
      Entity entity, float phase, float amount, float age, float yaw, float pitch, float scale) {
    EntityMBOGuardian guardian = (EntityMBOGuardian) entity;
    head.rotateAngleY = yaw * (float) Math.PI / 180;
    head.rotateAngleX = pitch * (float) Math.PI / 180;
    for (int i = 0; i < 12; i++) {
      float offset = 1 + MathHelper.cos(age * 1.5F + i) * .01F - (guardian.swimming() ? .55F : 0);
      spikes[i].setRotationPoint(X[i] * offset, 16 + Y[i] * offset, Z[i] * offset);
    }
    float wave = MathHelper.sin(age * (guardian.swimming() ? .6F : .15F));
    tail[0].rotateAngleY = wave * (float) Math.PI * .05F;
    tail[1].rotateAngleY = wave * (float) Math.PI * .1F;
    tail[2].rotateAngleY = wave * (float) Math.PI * .15F;
    Entity target = entity.worldObj.getEntityByID(guardian.beamTarget());
    eye.rotationPointY = target != null && target.posY > entity.posY ? 0 : 1;
    head.render(scale);
  }
}
