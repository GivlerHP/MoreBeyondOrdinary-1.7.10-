package ru.givler.mbo.client.model.fauna;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import ru.givler.mbo.entity.fauna.EntityMBOAxolotl;

/** Original adult procedural poses and all reference baby keyframe channels. */
public final class ModelMBOAxolotl extends ModelMBOFrog {
  private final boolean baby;

  public ModelMBOAxolotl(boolean baby) {
    super(baby ? "axolotl_baby" : "axolotl", false);
    this.baby = baby;
  }

  @Override
  public void render(
      Entity entity, float phase, float amount, float age, float yaw, float pitch, float scale) {
    for (Node node : nodes.values()) node.reset();
    EntityMBOAxolotl animal = (EntityMBOAxolotl) entity;
    float partial = age - animal.ticksExisted,
        dead = animal.poseFactor(0, partial),
        water = animal.poseFactor(1, partial),
        moving = animal.poseFactor(2, partial),
        ground = animal.poseFactor(3, partial);
    if (baby) {
      if (animal.deadTicks() > 0) apply("BABY_AXOLOTL_PLAY_DEAD", age / 20, 1);
      else if (animal.isInWater())
        apply(
            animal.onGround
                ? (moving > .1F ? "WALK_FLOOR_UNDERWATER" : "IDLE_FLOOR_UNDERWATER")
                : (moving > .1F ? "BABY_AXOLOTL_SWIM" : "IDLE_UNDERWATER"),
            age / 20,
            1);
      else if (moving > .1F) apply("AXOLOTL_WALK_FLOOR", phase * .75F, Math.min(1, amount * 30));
      else apply("BABY_AXOLOTL_IDLE_FLOOR", age / 20, 1);
    } else {
      nodes.get("body").rotation[1] += yaw * (float) Math.PI / 180;
      float live = 1 - dead;
      setupSwimmingAnimation(age, pitch, Math.min(moving, water) * live);
      setupWaterHoveringAnimation(age, Math.min(1 - moving, water) * live);
      setupGroundCrawlingAnimation(age, Math.min(moving, ground) * live);
      setupLayStillOnGroundAnimation(age, Math.min(1 - moving, ground) * live);
      setupPlayDeadAnimation(dead);
      applyMirrorLegRotations(1 - Math.min(ground, moving));
    }
    for (Node node : roots) draw(node, scale);
  }

  private void setupLayStillOnGroundAnimation(final float ageInTicks, final float factor) {
    if (!(factor <= 1.0E-5F)) {
      float animMoveSpeed = ageInTicks * 0.09F;
      float sineSway = MathHelper.sin(animMoveSpeed);
      float cosineSway = MathHelper.cos(animMoveSpeed);
      float movement = sineSway * sineSway - 2.0F * sineSway;
      float movement2 = cosineSway * cosineSway - 3.0F * sineSway;
      nodes.get("head").rotation[0] += -0.09F * movement * factor;
      nodes.get("head").rotation[2] += -0.2F * factor;
      nodes.get("tail").rotation[1] += (-0.1F + 0.1F * movement) * factor;
      float gillAngle = (0.6F + 0.05F * movement2) * factor;
      nodes.get("top_gills").rotation[0] += gillAngle;
      nodes.get("left_gills").rotation[1] -= gillAngle;
      nodes.get("right_gills").rotation[1] += gillAngle;
      nodes.get("left_hind_leg").rotation[0] += 1.1F * factor;
      nodes.get("left_hind_leg").rotation[1] += 1.0F * factor;
      nodes.get("left_front_leg").rotation[0] += 0.8F * factor;
      nodes.get("left_front_leg").rotation[1] += 2.3F * factor;
      nodes.get("left_front_leg").rotation[2] -= 0.5F * factor;
    }
  }

  private void setupGroundCrawlingAnimation(final float ageInTicks, final float factor) {
    if (!(factor <= 1.0E-5F)) {
      float animMoveSpeed = ageInTicks * 0.11F;
      float cosineSway = MathHelper.cos(animMoveSpeed);
      float hindLegYRotSway = (cosineSway * cosineSway - 2.0F * cosineSway) / 5.0F;
      float frontLegYRotSway = 0.7F * cosineSway;
      float headAndTailYRot = 0.09F * cosineSway * factor;
      nodes.get("head").rotation[1] += headAndTailYRot;
      nodes.get("tail").rotation[1] += headAndTailYRot;
      float gillAngle =
          (0.6F - 0.08F * (cosineSway * cosineSway + 2.0F * MathHelper.sin(animMoveSpeed)))
              * factor;
      nodes.get("top_gills").rotation[0] += gillAngle;
      nodes.get("left_gills").rotation[1] -= gillAngle;
      nodes.get("right_gills").rotation[1] += gillAngle;
      float hindLegXRot = 0.9424779F * factor;
      float frontLegXRot = 1.0995574F * factor;
      nodes.get("left_hind_leg").rotation[0] += hindLegXRot;
      nodes.get("left_hind_leg").rotation[1] += (1.5F - hindLegYRotSway) * factor;
      nodes.get("left_hind_leg").rotation[2] += -0.1F * factor;
      nodes.get("left_front_leg").rotation[0] += frontLegXRot;
      nodes.get("left_front_leg").rotation[1] +=
          ((float) (Math.PI / 2) - frontLegYRotSway) * factor;
      nodes.get("right_hind_leg").rotation[0] += hindLegXRot;
      nodes.get("right_hind_leg").rotation[1] += (-1.0F - hindLegYRotSway) * factor;
      nodes.get("right_front_leg").rotation[0] += frontLegXRot;
      nodes.get("right_front_leg").rotation[1] +=
          ((float) (-Math.PI / 2) - frontLegYRotSway) * factor;
    }
  }

  private void setupWaterHoveringAnimation(final float ageInTicks, final float factor) {
    if (!(factor <= 1.0E-5F)) {
      float animMoveSpeed = ageInTicks * 0.075F;
      float cosineSway = MathHelper.cos(animMoveSpeed);
      float sineSway = MathHelper.sin(animMoveSpeed) * 0.15F;
      float bodyXRot = (-0.15F + 0.075F * cosineSway) * factor;
      nodes.get("body").rotation[0] += bodyXRot;
      nodes.get("body").position[1] -= sineSway * factor;
      nodes.get("head").rotation[0] -= bodyXRot;
      nodes.get("top_gills").rotation[0] += 0.2F * cosineSway * factor;
      float gillYRot = (-0.3F * cosineSway - 0.19F) * factor;
      nodes.get("left_gills").rotation[1] += gillYRot;
      nodes.get("right_gills").rotation[1] -= gillYRot;
      nodes.get("left_hind_leg").rotation[0] +=
          ((float) (Math.PI * 3.0 / 4.0) - cosineSway * 0.11F) * factor;
      nodes.get("left_hind_leg").rotation[1] += 0.47123894F * factor;
      nodes.get("left_hind_leg").rotation[2] += 1.7278761F * factor;
      nodes.get("left_front_leg").rotation[0] +=
          ((float) (Math.PI / 4) - cosineSway * 0.2F) * factor;
      nodes.get("left_front_leg").rotation[1] += 2.042035F * factor;
      nodes.get("tail").rotation[1] += 0.5F * cosineSway * factor;
    }
  }

  private void setupSwimmingAnimation(
      final float ageInTicks, final float xRot, final float factor) {
    if (!(factor <= 1.0E-5F)) {
      float animMoveSpeed = ageInTicks * 0.33F;
      float sineSway = MathHelper.sin(animMoveSpeed);
      float cosineSway = MathHelper.cos(animMoveSpeed);
      float bodySway = 0.13F * sineSway;
      nodes.get("body").rotation[0] += (xRot * (float) (Math.PI / 180.0) + bodySway) * factor;
      nodes.get("head").rotation[0] -= bodySway * 1.8F * factor;
      nodes.get("body").position[1] -= 0.45F * cosineSway * factor;
      nodes.get("top_gills").rotation[0] += (-0.5F * sineSway - 0.8F) * factor;
      float gillYRot = (0.3F * sineSway + 0.9F) * factor;
      nodes.get("left_gills").rotation[1] += gillYRot;
      nodes.get("right_gills").rotation[1] -= gillYRot;
      nodes.get("tail").rotation[1] =
          nodes.get("tail").rotation[1] + 0.3F * MathHelper.cos(animMoveSpeed * 0.9F) * factor;
      nodes.get("left_hind_leg").rotation[0] += 1.8849558F * factor;
      nodes.get("left_hind_leg").rotation[1] += -0.4F * sineSway * factor;
      nodes.get("left_hind_leg").rotation[2] += (float) (Math.PI / 2) * factor;
      nodes.get("left_front_leg").rotation[0] += 1.8849558F * factor;
      nodes.get("left_front_leg").rotation[1] += (-0.2F * cosineSway - 0.1F) * factor;
      nodes.get("left_front_leg").rotation[2] += (float) (Math.PI / 2) * factor;
    }
  }

  private void setupPlayDeadAnimation(final float factor) {
    if (!(factor <= 1.0E-5F)) {
      nodes.get("left_hind_leg").rotation[0] += 1.4137167F * factor;
      nodes.get("left_hind_leg").rotation[1] += 1.0995574F * factor;
      nodes.get("left_hind_leg").rotation[2] += (float) (Math.PI / 4) * factor;
      nodes.get("left_front_leg").rotation[0] += (float) (Math.PI / 4) * factor;
      nodes.get("left_front_leg").rotation[1] += 2.042035F * factor;
      nodes.get("body").rotation[0] += -0.15F * factor;
      nodes.get("body").rotation[2] += 0.35F * factor;
    }
  }

  private void applyMirrorLegRotations(final float factor) {
    if (!(factor <= 1.0E-5F)) {
      nodes.get("right_hind_leg").rotation[0] =
          nodes.get("right_hind_leg").rotation[0] + nodes.get("left_hind_leg").rotation[0] * factor;
      Node var2 = nodes.get("right_hind_leg");
      var2.rotation[1] = var2.rotation[1] + -nodes.get("left_hind_leg").rotation[1] * factor;
      var2 = nodes.get("right_hind_leg");
      var2.rotation[2] = var2.rotation[2] + -nodes.get("left_hind_leg").rotation[2] * factor;
      nodes.get("right_front_leg").rotation[0] =
          nodes.get("right_front_leg").rotation[0]
              + nodes.get("left_front_leg").rotation[0] * factor;
      var2 = nodes.get("right_front_leg");
      var2.rotation[1] = var2.rotation[1] + -nodes.get("left_front_leg").rotation[1] * factor;
      var2 = nodes.get("right_front_leg");
      var2.rotation[2] = var2.rotation[2] + -nodes.get("left_front_leg").rotation[2] * factor;
    }
  }
}
