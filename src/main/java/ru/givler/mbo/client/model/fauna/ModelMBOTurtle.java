package ru.givler.mbo.client.model.fauna;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.fauna.EntityMBOTurtle;

public abstract class ModelMBOTurtle extends ModelBase {
  protected final Map<String, ModelRenderer> parts = new LinkedHashMap<String, ModelRenderer>();

  protected ModelMBOTurtle(int width, int height) {
    textureWidth = width;
    textureHeight = height;
  }

  protected void part(String name, float x, float y, float z, float rx, float ry, float rz) {
    ModelRenderer part = new ModelRenderer(this);
    part.setRotationPoint(x, y, z);
    part.rotateAngleX = rx;
    part.rotateAngleY = ry;
    part.rotateAngleZ = rz;
    parts.put(name, part);
  }

  protected void box(
      String name, int u, int v, float x, float y, float z, int width, int height, int depth) {
    parts.get(name).setTextureOffset(u, v);
    parts.get(name).addBox(x, y, z, width, height, depth);
  }

  @Override
  public void render(
      Entity entity, float phase, float amount, float age, float yaw, float pitch, float scale) {
    EntityMBOTurtle turtle = (EntityMBOTurtle) entity;
    ModelRenderer head = parts.get("head");
    head.rotateAngleY = yaw * (float) Math.PI / 180;
    head.rotateAngleX = pitch * (float) Math.PI / 180;
    String[] names = {"right_hind_leg", "left_hind_leg", "right_front_leg", "left_front_leg"};
    for (String name : names) {
      ModelRenderer leg = parts.get(name);
      leg.rotateAngleX = leg.rotateAngleY = leg.rotateAngleZ = 0;
    }
    if (!turtle.isInWater() && turtle.onGround) {
      float lay = turtle.isLayingEgg() ? 4 : 1, amplitude = turtle.isLayingEgg() ? 2 : 1;
      float front = MathHelper.cos(lay * phase * 5) * 8 * amount * amplitude,
          hind = MathHelper.cos(phase * 5) * 3 * amount;
      parts.get(names[0]).rotateAngleY = -hind;
      parts.get(names[1]).rotateAngleY = hind;
      parts.get(names[2]).rotateAngleY = -front;
      parts.get(names[3]).rotateAngleY = front;
    } else {
      float swing = MathHelper.cos(phase * .6662F * .6F) * .5F * amount;
      parts.get(names[0]).rotateAngleX = swing;
      parts.get(names[1]).rotateAngleX = -swing;
      parts.get(names[2]).rotateAngleZ = -swing;
      parts.get(names[3]).rotateAngleZ = swing;
    }
    GL11.glPushMatrix();
    if (turtle.hasEgg()) GL11.glTranslatef(0, -scale, 0);
    for (Map.Entry<String, ModelRenderer> part : parts.entrySet())
      if (!part.getKey().equals("egg_belly") || turtle.hasEgg()) part.getValue().render(scale);
    GL11.glPopMatrix();
  }
}
