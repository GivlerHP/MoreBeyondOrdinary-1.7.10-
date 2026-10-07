package ru.givler.mbo.client.render.fauna;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.client.model.fauna.*;
import ru.givler.mbo.entity.fauna.EntityMBOFish;

public final class RenderMBOFish extends RenderLiving {
  private final ModelMBOFish[] models = {
    new ModelMBOCod(0),
    new ModelMBOSalmon(0),
    new ModelMBOTropicalFishSmall(0),
    new ModelMBOTropicalFishLarge(0),
    new ModelMBOPufferfishSmall(0),
    new ModelMBOPufferfishMid(0),
    new ModelMBOPufferfishBig(0)
  };
  private final ModelMBOFish[] patterns = {
    new ModelMBOTropicalFishSmall(.008F), new ModelMBOTropicalFishLarge(.008F)
  };
  private static final float[][] COLORS = {
    {1, 1, 1},
    {.85F, .5F, .2F},
    {.7F, .3F, .85F},
    {.4F, .6F, .85F},
    {.9F, .9F, .2F},
    {.5F, .8F, .1F},
    {.95F, .5F, .65F},
    {.3F, .3F, .3F},
    {.6F, .6F, .6F},
    {.3F, .5F, .6F},
    {.5F, .25F, .7F},
    {.2F, .3F, .7F},
    {.4F, .3F, .2F},
    {.4F, .5F, .2F},
    {.6F, .2F, .2F},
    {.1F, .1F, .1F}
  };

  public RenderMBOFish() {
    super(new ModelMBOCod(0), .15F);
  }

  private static boolean large(EntityMBOFish fish) {
    return (fish.variant() & 255) == 1;
  }

  @Override
  public void doRender(
      EntityLiving entity, double x, double y, double z, float yaw, float partial) {
    EntityMBOFish fish = (EntityMBOFish) entity;
    mainModel =
        models[
            fish.foodMeta() == 0
                ? 0
                : fish.foodMeta() == 1
                    ? 1
                    : fish.foodMeta() == 2 ? (large(fish) ? 3 : 2) : 4 + fish.puff()];
    super.doRender(entity, x, y, z, yaw, partial);
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    EntityMBOFish fish = (EntityMBOFish) entity;
    String name =
        fish.foodMeta() == 2 ? (large(fish) ? "tropical_b" : "tropical_a") : fish.species();
    return new ResourceLocation("mbo", "textures/entity/fish/" + name + ".png");
  }

  @Override
  protected void preRenderCallback(EntityLivingBase entity, float partial) {
    EntityMBOFish fish = (EntityMBOFish) entity;
    if (fish.foodMeta() == 1) {
      float scale = fish.variant() == 0 ? .5F : fish.variant() == 2 ? 1.5F : 1F;
      GL11.glScalef(scale, scale, scale);
    }
  }

  @Override
  protected void renderModel(
      EntityLivingBase entity,
      float phase,
      float amount,
      float age,
      float yaw,
      float pitch,
      float scale) {
    EntityMBOFish fish = (EntityMBOFish) entity;
    if (fish.foodMeta() == 2) color((fish.variant() >>> 16) & 15);
    try {
      super.renderModel(entity, phase, amount, age, yaw, pitch, scale);
    } finally {
      GL11.glColor4f(1, 1, 1, 1);
    }
  }

  private void color(int index) {
    float[] c = COLORS[index];
    GL11.glColor3f(c[0], c[1], c[2]);
  }

  @Override
  protected int shouldRenderPass(EntityLivingBase entity, int pass, float partial) {
    EntityMBOFish fish = (EntityMBOFish) entity;
    if (fish.foodMeta() != 2 || pass != 0) {
      GL11.glColor4f(1, 1, 1, 1);
      return -1;
    }
    setRenderPassModel(patterns[large(fish) ? 1 : 0]);
    int pattern = ((fish.variant() >>> 8) & 255) % 6 + 1;
    bindTexture(
        new ResourceLocation(
            "mbo",
            "textures/entity/fish/tropical_"
                + (large(fish) ? "b" : "a")
                + "_pattern_"
                + pattern
                + ".png"));
    color((fish.variant() >>> 24) & 15);
    return 1;
  }

  @Override
  protected void rotateCorpse(EntityLivingBase entity, float age, float yaw, float partial) {
    super.rotateCorpse(entity, age, yaw, partial);
    EntityMBOFish fish = (EntityMBOFish) entity;
    float amplitude = fish.foodMeta() == 1 ? 4.3F : fish.foodMeta() == 0 ? 4.3F : 0F;
    GL11.glRotatef(amplitude * MathHelper.sin(.6F * age), 0, 1, 0);
    if (!fish.inFishWater()) {
      GL11.glTranslatef(.1F, .1F, -.1F);
      GL11.glRotatef(90F, 0, 0, 1);
    }
  }
}
