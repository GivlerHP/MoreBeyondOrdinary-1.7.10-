package ru.givler.mbo.client.model.fauna;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.fauna.EntityMBOFrog;
import ru.givler.mbo.entity.fauna.EntityMBOTadpole;

/** Reference geometry and linear/Catmull-Rom animation channels, including local scales. */
public class ModelMBOFrog extends ModelBase {
  private final boolean tadpole;
  private final Definition definition;
  protected final Map<String, Node> nodes = new LinkedHashMap<String, Node>();
  protected final List<Node> roots = new ArrayList<Node>();

  public ModelMBOFrog(boolean tadpole) {
    this(tadpole ? "tadpole" : "frog", tadpole);
  }

  protected ModelMBOFrog(String name, boolean tadpole) {
    this.tadpole = tadpole;
    String file = "/assets/mbo/models/fauna/" + name + ".json";
    InputStream source = ModelMBOFrog.class.getResourceAsStream(file);
    if (source == null) throw new IllegalStateException("Missing fauna model: " + file);
    try (InputStreamReader reader = new InputStreamReader(source, StandardCharsets.UTF_8)) {
      definition = new Gson().fromJson(reader, Definition.class);
    } catch (Exception error) {
      throw new IllegalStateException("Invalid fauna model: " + file, error);
    }
    textureWidth = definition.width;
    textureHeight = definition.height;
    for (Part part : definition.parts) {
      Node node = new Node(part, new ModelRenderer(this));
      for (float[] box : part.boxes) {
        node.geometry.setTextureOffset((int) box[0], (int) box[1]);
        node.geometry.addBox(
            box[2], box[3], box[4], (int) box[5], (int) box[6], (int) box[7], box[8]);
      }
      nodes.put(part.name, node);
      if (part.parent == null) roots.add(node);
      else {
        Node parent = nodes.get(part.parent);
        if (parent == null) throw new IllegalStateException("Missing parent: " + part.parent);
        parent.children.add(node);
      }
    }
    for (Animation animation : definition.animations.values())
      for (Channel channel : animation.channels)
        if (!nodes.containsKey(channel.bone))
          throw new IllegalStateException("Missing animation bone: " + channel.bone);
  }

  public int animationCount() {
    return definition.animations.size();
  }

  @Override
  public void render(
      Entity entity, float phase, float amount, float age, float yaw, float pitch, float scale) {
    for (Node node : nodes.values()) node.reset();
    if (tadpole) {
      EntityMBOTadpole fish = (EntityMBOTadpole) entity;
      nodes.get("tail").rotation[1] =
          -(fish.inFishWater() ? 1F : 1.5F) * .25F * MathHelper.sin(.3F * age);
    } else {
      EntityMBOFrog frog = (EntityMBOFrog) entity;
      float partial = age - frog.ticksExisted;
      if (frog.activity(0)) apply("FROG_JUMP", frog.animationTime(0, partial), 1F);
      if (frog.activity(1)) apply("FROG_CROAK", frog.animationTime(1, partial), 1F);
      if (frog.activity(2)) apply("FROG_TONGUE", frog.animationTime(2, partial), 1F);
      apply(
          frog.isInWater() ? "FROG_SWIM" : "FROG_WALK",
          phase * (frog.isInWater() ? .05F : .075F),
          Math.min(1F, amount * 2.5F));
      if (frog.isInWater()) apply("FROG_IDLE_WATER", age / 20F, 1F);
      nodes.get("croaking_body").visible = frog.activity(1);
      nodes.get("tongue").visible = frog.activity(2);
    }
    for (Node node : roots) draw(node, scale);
  }

  protected void apply(String name, float time, float weight) {
    Animation animation = definition.animations.get(name);
    if (animation == null || weight == 0) return;
    time =
        animation.loop && animation.length > 0
            ? time % animation.length
            : MathHelper.clamp_float(time, 0, animation.length);
    for (Channel channel : animation.channels) {
      Node node = nodes.get(channel.bone);
      float[] target =
          channel.target.equals("ROTATION")
              ? node.rotation
              : channel.target.equals("POSITION") ? node.position : node.scale;
      Frame[] frames = channel.frames;
      int end = 0;
      while (end < frames.length - 1 && frames[end].time < time) end++;
      int begin = Math.max(0, end - 1);
      float duration = frames[end].time - frames[begin].time;
      float fraction =
          duration == 0 ? 0 : MathHelper.clamp_float((time - frames[begin].time) / duration, 0, 1);
      for (int axis = 0; axis < 3; axis++) {
        float a = frames[begin].value[axis], b = frames[end].value[axis], value;
        if (frames[end].cubic) {
          float before = frames[Math.max(0, begin - 1)].value[axis],
              after = frames[Math.min(frames.length - 1, end + 1)].value[axis];
          value = cubic(before, a, b, after, fraction);
        } else value = a + (b - a) * fraction;
        target[axis] += value * weight;
      }
    }
  }

  public static float cubic(float before, float a, float b, float after, float fraction) {
    return .5F
        * (2 * a
            + (b - before) * fraction
            + (2 * before - 5 * a + 4 * b - after) * fraction * fraction
            + (3 * a - before - 3 * b + after) * fraction * fraction * fraction);
  }

  protected void draw(Node node, float size) {
    if (!node.visible || node.scale[0] <= 0 || node.scale[1] <= 0 || node.scale[2] <= 0) return;
    GL11.glPushMatrix();
    GL11.glTranslatef(node.position[0] * size, node.position[1] * size, node.position[2] * size);
    GL11.glRotatef(node.rotation[2] * 180F / (float) Math.PI, 0, 0, 1);
    GL11.glRotatef(node.rotation[1] * 180F / (float) Math.PI, 0, 1, 0);
    GL11.glRotatef(node.rotation[0] * 180F / (float) Math.PI, 1, 0, 0);
    GL11.glScalef(node.scale[0], node.scale[1], node.scale[2]);
    node.geometry.render(size);
    for (Node child : node.children) draw(child, size);
    GL11.glPopMatrix();
  }

  protected static final class Node {
    final Part part;
    final ModelRenderer geometry;
    final List<Node> children = new ArrayList<Node>();
    final float[] position = new float[3], rotation = new float[3], scale = new float[3];
    boolean visible;

    Node(Part part, ModelRenderer geometry) {
      this.part = part;
      this.geometry = geometry;
    }

    void reset() {
      for (int i = 0; i < 3; i++) {
        position[i] = part.pose[i];
        rotation[i] = part.pose[i + 3];
        scale[i] = 1;
      }
      visible = true;
    }
  }

  private static final class Definition {
    int width, height;
    Part[] parts;
    Map<String, Animation> animations;
  }

  private static final class Part {
    String name, parent;
    float[] pose;
    float[][] boxes;
  }

  private static final class Animation {
    float length;
    boolean loop;
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
}
