package ru.givler.mbo.client.model.fauna;

import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import ru.givler.mbo.entity.fauna.EntityMBOCamel;

public final class ModelMBOCamel extends ModelMBOFrog {
  private final String prefix;
  public boolean saddlePass;

  public ModelMBOCamel(boolean baby) {
    super(baby ? "camel_baby" : "camel", false);
    prefix = baby ? "CAMEL_BABY_" : "CAMEL_";
  }

  @Override
  public void render(
      Entity entity, float phase, float amount, float age, float yaw, float pitch, float scale) {
    EntityMBOCamel camel = (EntityMBOCamel) entity;
    for (Node node : nodes.values()) node.reset();
    Node head = nodes.get("head");
    head.rotation[1] += MathHelper.clamp_float(yaw, -30, 30) * (float) Math.PI / 180;
    head.rotation[0] +=
        MathHelper.clamp_float(
                pitch + 45 * camel.dashCooldown() / 55F, -25, camel.dashCooldown() > 0 ? 70 : 45)
            * (float) Math.PI
            / 180;
    float partial = age - camel.ticksExisted, time = (camel.poseTicks() + partial) / 20F;
    if (camel.sitting())
      apply(
          prefix + (camel.transitioning() ? "SIT" : "SIT_POSE"),
          camel.transitioning() ? time : age / 20F,
          1);
    else if (camel.transitioning()) apply(prefix + "STANDUP", time, 1);
    else {
      apply(prefix + "WALK", phase * .1F, Math.min(1, amount * 2.5F));
      float idleTime = (age + camel.getEntityId() * 13) % 200;
      if (amount < .05F && idleTime < 80) apply(prefix + "IDLE", idleTime / 20F, 1);
      if (camel.dashCooldown() > 0 && (!camel.onGround || camel.dashCooldown() > 50))
        apply(prefix + "DASH", (55 - camel.dashCooldown() + partial) / 20F, 1);
    }
    for (Map.Entry<String, Node> entry : nodes.entrySet()) {
      String name = entry.getKey();
      Node node = entry.getValue();
      boolean equipment = name.equals("saddle") || name.equals("bridle") || name.equals("reins");
      node.geometry.isHidden = saddlePass ? !equipment : equipment;
      if (name.equals("reins") && camel.riddenByEntity == null) node.geometry.isHidden = true;
    }
    for (Node root : roots) draw(root, scale);
  }
}
