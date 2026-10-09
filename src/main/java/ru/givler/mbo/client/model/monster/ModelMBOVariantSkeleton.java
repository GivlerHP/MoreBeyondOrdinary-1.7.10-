package ru.givler.mbo.client.model.monster;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import ru.givler.mbo.entity.monster.EntityMBOBogged;

/** Original modern part geometry and UVs, with vanilla skeleton animation. */
public final class ModelMBOVariantSkeleton extends ModelSkeleton {
  private ModelRenderer mushrooms;
  private final boolean parched;

  public ModelMBOVariantSkeleton(String variant) {
    super(0);
    parched = variant.equals("parched");
    if (variant.equals("parched")) {
      textureHeight = 64;
      bipedHead = new ModelRenderer(this, 0, 0).addBox(-4, -8, -4, 8, 8, 8);
      bipedHead.addChild(grown(0, 32, -4, -8, -4, 8, 8, 8, .2F));
      bipedHeadwear = new ModelRenderer(this);
      bipedHeadwear.showModel = false;
      bipedBody = new ModelRenderer(this, 16, 16).addBox(-4, 0, -2, 8, 12, 4);
      bipedBody.addChild(new ModelRenderer(this, 28, 0).addBox(-4, 10, -2, 8, 1, 4));
      bipedBody.addChild(grown(16, 48, -4, 0, -2, 8, 12, 4, .025F));
      bipedRightArm = limb(40, 16, -5.5F, 2, 0, 2, false);
      bipedRightArm.addChild(
          new ModelRenderer(this, 42, 33).addBox(-1.55F, -2.025F, -1.5F, 3, 12, 3));
      bipedLeftArm = limb(56, 16, 5.5F, 2, 0, 2, false);
      bipedLeftArm.addChild(
          new ModelRenderer(this, 40, 48).addBox(-1.45F, -2.025F, -1.5F, 3, 12, 3));
      bipedRightLeg = limb(0, 16, -2, 12, 0, 2, true);
      bipedRightLeg.addChild(new ModelRenderer(this, 0, 49).addBox(-1.5F, 0, -1.5F, 3, 12, 3));
      bipedLeftLeg = limb(0, 16, 2, 12, 0, 2, true);
      bipedLeftLeg.addChild(new ModelRenderer(this, 4, 49).addBox(-1.5F, 0, -1.5F, 3, 12, 3));
    } else if (variant.equals("bogged")) {
      mushrooms = new ModelRenderer(this);
      bipedHead.addChild(mushrooms);
      mushroom(50, 16, 3, -8, 3, 0, .7853982F, 0, -3);
      mushroom(50, 16, 3, -8, 3, 0, 2.3561945F, 0, -3);
      mushroom(50, 22, -3, -8, -3, 0, .7853982F, 0, -3);
      mushroom(50, 22, -3, -8, -3, 0, 2.3561945F, 0, -3);
      mushroom(50, 28, -2, -1, 4, -1.5707964F, 0, .7853982F, -4);
      mushroom(50, 28, -2, -1, 4, -1.5707964F, 0, 2.3561945F, -4);
    }
  }

  private ModelRenderer limb(int u, int v, float x, float y, float z, int width, boolean leg) {
    ModelRenderer part = new ModelRenderer(this, u, v).addBox(-1, leg ? 0 : -2, -1, width, 12, 2);
    part.setRotationPoint(x, y, z);
    return part;
  }

  private void mushroom(
      int u, int v, float x, float y, float z, float rx, float ry, float rz, float by) {
    ModelRenderer part = new ModelRenderer(this, u, v).addBox(-3, by, 0, 6, 4, 0);
    part.setRotationPoint(x, y, z);
    part.rotateAngleX = rx;
    part.rotateAngleY = ry;
    part.rotateAngleZ = rz;
    mushrooms.addChild(part);
  }

  @Override
  public void setRotationAngles(
      float walk, float amount, float age, float yaw, float pitch, float scale, Entity entity) {
    super.setRotationAngles(walk, amount, age, yaw, pitch, scale, entity);
    if (parched) {
      float x = .5F * MathHelper.cos(bipedBody.rotateAngleY),
          z = .5F * MathHelper.sin(bipedBody.rotateAngleY);
      bipedRightArm.rotationPointX -= x;
      bipedLeftArm.rotationPointX += x;
      bipedRightArm.rotationPointZ += z;
      bipedLeftArm.rotationPointZ -= z;
    }
    if (mushrooms != null)
      mushrooms.showModel =
          !(entity instanceof EntityMBOBogged) || !((EntityMBOBogged) entity).isSheared();
  }

  private ModelRenderer grown(
      int u, int v, float x, float y, float z, int w, int h, int d, float grow) {
    ModelRenderer part = new ModelRenderer(this, u, v);
    part.addBox(x, y, z, w, h, d, grow);
    return part;
  }
}
