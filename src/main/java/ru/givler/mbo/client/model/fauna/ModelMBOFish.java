package ru.givler.mbo.client.model.fauna;

import java.util.*;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import ru.givler.mbo.entity.fauna.EntityMBOFish;

public abstract class ModelMBOFish extends ModelBase {
  private final String kind;
  private final Map<String, ModelRenderer> parts = new HashMap<String, ModelRenderer>();
  private final List<ModelRenderer> roots = new ArrayList<ModelRenderer>();

  protected ModelMBOFish(String kind) {
    this.kind = kind;
    textureWidth = textureHeight = 32;
  }

  protected void part(
      String name,
      String parent,
      int u,
      int v,
      float x,
      float y,
      float z,
      int w,
      int h,
      int d,
      float px,
      float py,
      float pz,
      float rx,
      float ry,
      float rz,
      float grow) {
    ModelRenderer part = new ModelRenderer(this, u, v);
    part.addBox(x, y, z, w, h, d, grow);
    part.setRotationPoint(px, py, pz);
    part.rotateAngleX = rx;
    part.rotateAngleY = ry;
    part.rotateAngleZ = rz;
    parts.put(name, part);
    if (parent == null) roots.add(part);
    else parts.get(parent).addChild(part);
  }

  @Override
  public void render(
      Entity entity, float swing, float amount, float age, float yaw, float pitch, float scale) {
    EntityMBOFish fish = (EntityMBOFish) entity;
    if (kind.startsWith("Pufferfish")) {
      String suffix = kind.equals("PufferfishSmall") ? "_fin" : "_blue_fin";
      parts.get("right" + suffix).rotateAngleZ = -.2F + .4F * MathHelper.sin(age * .2F);
      parts.get("left" + suffix).rotateAngleZ = .2F - .4F * MathHelper.sin(age * .2F);
    } else if (kind.equals("Salmon")) {
      parts.get("body_back").rotateAngleY =
          -(fish.inFishWater() ? 1F : 1.3F)
              * .25F
              * MathHelper.sin((fish.inFishWater() ? 1F : 1.7F) * .6F * age);
    } else {
      parts.get(kind.equals("Cod") ? "tail_fin" : "tail").rotateAngleY =
          -(fish.inFishWater() ? 1F : 1.5F) * .45F * MathHelper.sin(.6F * age);
    }
    for (ModelRenderer part : roots) part.render(scale);
  }
}
