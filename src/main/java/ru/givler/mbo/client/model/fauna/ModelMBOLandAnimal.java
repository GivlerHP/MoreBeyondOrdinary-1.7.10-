package ru.givler.mbo.client.model.fauna;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.fauna.EntityMBOFox;
import ru.givler.mbo.entity.fauna.EntityMBOGoat;
import ru.givler.mbo.entity.fauna.EntityMBOPanda;

/** Original adult/cub meshes and modern procedural quadruped animation. */
public final class ModelMBOLandAnimal extends ModelBase {
  private final boolean baby;
  private final Map<String, ModelRenderer> parts = new LinkedHashMap<String, ModelRenderer>();
  private final Map<String, float[]> scales = new LinkedHashMap<String, float[]>();
  private final Definition definition;

  public ModelMBOLandAnimal(String species, boolean baby) {
    this.baby = baby;
    String path = "/assets/mbo/models/fauna/" + species + (baby ? "_baby" : "") + ".json";
    InputStream stream = getClass().getResourceAsStream(path);
    if (stream == null) throw new IllegalStateException("Missing model " + path);
    try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      definition = new Gson().fromJson(reader, Definition.class);
    } catch (Exception error) {
      throw new IllegalStateException("Invalid model " + path, error);
    }
    textureWidth = definition.width;
    textureHeight = definition.height;
    for (Part part : definition.parts) {
      ModelRenderer renderer = new ModelRenderer(this);
      for (float[] box : part.boxes) {
        renderer.setTextureOffset((int) box[0], (int) box[1]);
        renderer.mirror = box[9] != 0;
        renderer.addBox(box[2], box[3], box[4], (int) box[5], (int) box[6], (int) box[7], box[8]);
      }
      parts.put(part.name, renderer);
      scales.put(part.name, new float[] {1, 1, 1});
      if (part.parent != null) {
        ModelRenderer parent = parts.get(part.parent);
        if (parent == null) throw new IllegalStateException("Missing parent " + part.parent);
        parent.addChild(renderer);
      }
    }
  }

  @Override
  public void render(
      Entity entity, float phase, float amount, float age, float yaw, float pitch, float scale) {
    for (Part part : definition.parts) {
      ModelRenderer model = parts.get(part.name);
      model.setRotationPoint(part.pose[0], part.pose[1], part.pose[2]);
      model.rotateAngleX = part.pose[3];
      model.rotateAngleY = part.pose[4];
      model.rotateAngleZ = part.pose[5];
      model.showModel = true;
      float[] scaling = scales.get(part.name);
      scaling[0] = scaling[1] = scaling[2] = 1;
    }
    ModelRenderer head = parts.get("head");
    head.rotateAngleX += pitch * (float) Math.PI / 180;
    head.rotateAngleY = yaw * (float) Math.PI / 180;
    float step = MathHelper.cos(phase * .6662F) * 1.4F * amount,
        opposite = MathHelper.cos(phase * .6662F + (float) Math.PI) * 1.4F * amount;
    parts.get("right_hind_leg").rotateAngleX = step;
    parts.get("left_front_leg").rotateAngleX = step;
    parts.get("left_hind_leg").rotateAngleX = opposite;
    parts.get("right_front_leg").rotateAngleX = opposite;
    if (entity instanceof EntityMBOGoat) {
      EntityMBOGoat goat = (EntityMBOGoat) entity;
      parts.get("left_horn").showModel = goat.leftHorn();
      parts.get("right_horn").showModel = goat.rightHorn();
      float ram = goat.ramHead(age - goat.ticksExisted);
      if (ram > 0) head.rotateAngleX = ram;
    } else if (entity instanceof EntityMBOFox) {
      if (baby) babyWalk(phase, amount);
      animateFox((EntityMBOFox) entity, age);
    } else animatePanda((EntityMBOPanda) entity, age);
    for (Part part : definition.parts)
      if (part.parent == null) {
        ModelRenderer model = parts.get(part.name);
        float[] scaling = scales.get(part.name);
        if (scaling[0] == 1 && scaling[1] == 1 && scaling[2] == 1) model.render(scale);
        else {
          float x = model.rotationPointX, y = model.rotationPointY, z = model.rotationPointZ;
          GL11.glPushMatrix();
          GL11.glTranslatef(x * scale, y * scale, z * scale);
          GL11.glScalef(scaling[0], scaling[1], scaling[2]);
          model.setRotationPoint(0, 0, 0);
          model.render(scale);
          model.setRotationPoint(x, y, z);
          GL11.glPopMatrix();
        }
      }
  }

  private void animatePanda(EntityMBOPanda panda, float age) {
    ModelRenderer head = parts.get("head"),
        body = parts.get("body"),
        rf = parts.get("right_front_leg"),
        lf = parts.get("left_front_leg"),
        rh = parts.get("right_hind_leg"),
        lh = parts.get("left_hind_leg");
    if (panda.activity() == 6) {
      head.rotateAngleY = head.rotateAngleZ = .35F * MathHelper.sin(.6F * age);
      rf.rotateAngleX = -.75F * MathHelper.sin(.3F * age);
      lf.rotateAngleX = .75F * MathHelper.sin(.3F * age);
    }
    if (panda.activity() == 4) {
      float time = panda.activityTime() + age - panda.ticksExisted;
      head.rotateAngleX =
          time < 15
              ? -(float) Math.PI / 4 * time / 14
              : -(float) Math.PI / 4
                  + (float) Math.PI / 4 * MathHelper.clamp_float((time - 15) / 5, 0, 1);
    }
    float sit = panda.sitting(age - panda.ticksExisted);
    if (sit > 0) {
      body.rotateAngleX += ((baby ? (float) Math.PI / 18 : 1.7407963F) - body.rotateAngleX) * sit;
      if (baby) {
        body.rotationPointZ += (-1.5F - body.rotationPointZ) * sit;
        head.rotationPointZ += (-11.5F - head.rotationPointZ) * sit;
        head.rotationPointY += (17.5F - head.rotationPointY) * sit;
        rf.rotationPointZ += (-5 - rf.rotationPointZ) * sit;
        lf.rotationPointZ += (-5 - lf.rotationPointZ) * sit;
        rh.rotationPointZ += (3 - rh.rotationPointZ) * sit;
        lh.rotationPointZ += (3 - lh.rotationPointZ) * sit;
      } else head.rotateAngleX += ((float) Math.PI / 2 - head.rotateAngleX) * sit;
      rf.rotateAngleZ = -.27079642F * sit;
      lf.rotateAngleZ = .27079642F * sit;
      rh.rotateAngleZ = .5707964F * sit;
      lh.rotateAngleZ = -.5707964F * sit;
      if (panda.activity() == 1) {
        head.rotateAngleX = (float) Math.PI / 2 + .2F * MathHelper.sin(age * .6F);
        rf.rotateAngleX = lf.rotateAngleX = -.4F - .2F * MathHelper.sin(age * .6F);
      }
      if (panda.activity() == 5) {
        head.rotateAngleX = 2.1707964F;
        rf.rotateAngleX = lf.rotateAngleX = -.9F;
      }
    }
    float back = panda.onBack(age - panda.ticksExisted);
    if (back > 0) {
      rh.rotateAngleX = -.6F * MathHelper.sin(age * .15F);
      lh.rotateAngleX = .6F * MathHelper.sin(age * .15F);
      rf.rotateAngleX = .3F * MathHelper.sin(age * .25F);
      lf.rotateAngleX = -.3F * MathHelper.sin(age * .25F);
      head.rotateAngleX += ((float) Math.PI / 2 - head.rotateAngleX) * back;
    }
    if (panda.activity() == 3) {
      head.rotateAngleX = 2.0561945F;
      rh.rotateAngleX = -.5F * MathHelper.sin(age * .5F);
      lh.rotateAngleX = .5F * MathHelper.sin(age * .5F);
      rf.rotateAngleX = .5F * MathHelper.sin(age * .5F);
      lf.rotateAngleX = -.5F * MathHelper.sin(age * .5F);
    }
  }

  private void animateFox(EntityMBOFox fox, float age) {
    ModelRenderer head = parts.get("head"),
        body = parts.get("body"),
        tail = parts.get("tail"),
        rf = parts.get("right_front_leg"),
        lf = parts.get("left_front_leg"),
        rh = parts.get("right_hind_leg"),
        lh = parts.get("left_hind_leg");
    float crouch = fox.crouch(age - fox.ticksExisted);
    if (fox.state() == 2) {
      body.rotateAngleX += .10471976F;
      body.rotationPointY += crouch / (baby ? 6 : 1);
      head.rotationPointY += crouch;
      float wiggle = MathHelper.cos(age) * .05F;
      body.rotateAngleY = rh.rotateAngleZ = lh.rotateAngleZ = wiggle;
      rf.rotateAngleZ = lf.rotateAngleZ = wiggle / 2;
    }
    if (fox.state() == 1) {
      rf.showModel = lf.showModel = rh.showModel = lh.showModel = false;
      body.rotateAngleZ = -(float) Math.PI / 2;
      head.rotateAngleX = 0;
      head.rotateAngleY = -(float) Math.PI * 2 / 3;
      head.rotateAngleZ = MathHelper.cos(age * .027F) / 22;
      if (baby) {
        body.rotateAngleX = -(float) Math.PI / 18;
        body.rotationPointY++;
        body.rotationPointZ--;
        body.rotationPointX--;
        tail.rotateAngleX = -2.1816616F;
        tail.rotationPointX -= .7F;
        tail.rotationPointZ += .6F;
        tail.rotationPointY += .9F;
        head.rotationPointX -= 2;
        head.rotationPointY += 2.8F;
        head.rotationPointZ -= 4;
      } else {
        body.rotationPointY += 5;
        tail.rotateAngleX = -(float) Math.PI * 5 / 6;
        head.rotationPointX += 2;
        head.rotationPointY += 2.99F;
      }
    }
    if (fox.state() == 5) {
      head.rotateAngleX = head.rotateAngleY = 0;
      rf.rotateAngleX = lf.rotateAngleX = -(float) Math.PI / 12;
      if (baby) {
        body.rotateAngleX = -.959931F;
        body.rotationPointZ -= 4.5F;
        body.rotationPointY += 3;
        tail.rotationPointY -= .6F;
        tail.rotationPointZ -= 2;
        tail.rotateAngleX = .95993114F;
        head.rotationPointY -= .75F;
        rf.rotationPointZ--;
        lf.rotationPointZ--;
        rh.rotationPointZ -= 3.75F;
        lh.rotationPointZ -= 3.75F;
      } else {
        body.rotateAngleX = (float) Math.PI / 6;
        body.rotationPointY -= 7;
        body.rotationPointZ += 3;
        tail.rotateAngleX = (float) Math.PI / 4;
        head.rotationPointY -= 6.5F;
        head.rotationPointZ += 2.75F;
        rh.rotateAngleX = lh.rotateAngleX = -(float) Math.PI * 5 / 12;
        rh.rotationPointY += 4;
        lh.rotationPointY += 4;
        rh.rotationPointZ -= .25F;
        lh.rotationPointZ -= .25F;
        tail.rotationPointZ--;
      }
    }
    if (fox.state() == 3 && !baby) {
      body.rotationPointY -= crouch / 2;
      head.rotationPointY -= crouch / 2;
    }
    if (fox.state() == 4) {
      rh.rotateAngleX = lf.rotateAngleX = MathHelper.cos(age * .67F * .4662F) * .1F;
      lh.rotateAngleX =
          rf.rotateAngleX = MathHelper.cos(age * .67F * .4662F + (float) Math.PI) * .1F;
    }
  }

  public void postRenderHead(float scale) {
    parts.get("head").postRender(scale);
  }

  public int walkChannels() {
    return definition.walk == null ? 0 : definition.walk.channels.length;
  }

  private void babyWalk(float phase, float amount) {
    if (definition.walk == null) return;
    for (String name :
        new String[] {"right_hind_leg", "left_hind_leg", "right_front_leg", "left_front_leg"})
      parts.get(name).rotateAngleX = 0;
    float time = (phase * .05F) % definition.walk.length, weight = Math.min(amount * 2.5F, 1);
    for (Channel channel : definition.walk.channels) {
      ModelRenderer model = parts.get(channel.bone);
      Frame[] frames = channel.frames;
      int end = 0;
      while (end < frames.length - 1 && frames[end].time < time) end++;
      int begin = Math.max(0, end - 1);
      float duration = frames[end].time - frames[begin].time,
          fraction =
              duration == 0
                  ? 0
                  : MathHelper.clamp_float((time - frames[begin].time) / duration, 0, 1);
      float[] value = new float[3];
      for (int axis = 0; axis < 3; axis++) {
        float a = frames[begin].value[axis], b = frames[end].value[axis];
        value[axis] =
            (frames[end].cubic
                    ? ModelMBOFrog.cubic(
                        frames[Math.max(0, begin - 1)].value[axis],
                        a,
                        b,
                        frames[Math.min(frames.length - 1, end + 1)].value[axis],
                        fraction)
                    : a + (b - a) * fraction)
                * weight;
      }
      if (channel.target.equals("ROTATION")) {
        model.rotateAngleX += value[0];
        model.rotateAngleY += value[1];
        model.rotateAngleZ += value[2];
      } else if (channel.target.equals("POSITION")) {
        model.rotationPointX += value[0];
        model.rotationPointY += value[1];
        model.rotationPointZ += value[2];
      } else {
        float[] scaling = scales.get(channel.bone);
        for (int axis = 0; axis < 3; axis++) scaling[axis] += value[axis];
      }
    }
  }

  private static final class Definition {
    int width, height;
    Part[] parts;
    Animation walk;
  }

  private static final class Animation {
    float length;
    Channel[] channels;
  }

  private static final class Channel {
    String bone, target;
    Frame[] frames;
  }

  private static final class Frame {
    float time;
    float[] value;
    boolean cubic;
  }

  private static final class Part {
    String name, parent;
    float[] pose;
    float[][] boxes;
  }
}
