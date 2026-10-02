package ru.givler.mbo.client.render;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.swimming.SwimmingHooks;
import java.util.Map;
import java.util.WeakHashMap;
import java.lang.reflect.Method;

/** Applies the pose after ModelBiped's normal and Battlegear arm animation. */
public final class SwimmingRenderHooks {
  private static final Map<EntityPlayer, Pose> POSES = new WeakHashMap<EntityPlayer, Pose>();
  private static final Method OFFHAND_SWING = findOffhandSwing();

  private static final class Pose {
    int tick = Integer.MIN_VALUE;
    float previous;
    float current;
  }

  private SwimmingRenderHooks() {}

  public static float cameraEyeHeight() {
    Entity view = Minecraft.getMinecraft().renderViewEntity;
    return view instanceof EntityPlayer && SwimmingHooks.isSwimming((EntityPlayer) view)
        ? 0.4F : 1.62F;
  }

  public static boolean skipViewBobbing() {
    Entity view = Minecraft.getMinecraft().renderViewEntity;
    return view instanceof EntityPlayer && SwimmingHooks.isSwimming((EntityPlayer) view);
  }

  public static void rotate(AbstractClientPlayer player, float partialTicks) {
    float amount = poseAmount(player, partialTicks);
    if (amount <= 0.0F) return;
    float pitch = player.prevRotationPitch
        + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
    GL11.glRotatef((-90.0F - pitch) * amount, 1.0F, 0.0F, 0.0F);
    GL11.glTranslatef(0.0F, -amount, 0.3F * amount);
  }

  public static void animate(ModelBiped model, Entity entity, float limbSwing, float age) {
    if (!(entity instanceof EntityPlayer)) return;
    float amount = poseAmount((EntityPlayer) entity, age - entity.ticksExisted);
    if (amount <= 0.0F) return;
    float stroke = age % 26.0F;
    if (stroke < 0.0F) stroke += 26.0F;
    model.bipedHead.rotateAngleX = mix(model.bipedHead.rotateAngleX,
        -(float) Math.PI / 4.0F, amount);
    model.bipedRightArm.rotateAngleY = mix(model.bipedRightArm.rotateAngleY,
        (float) Math.PI, amount);
    model.bipedLeftArm.rotateAngleY = mixAngle(model.bipedLeftArm.rotateAngleY,
        (float) Math.PI, amount);
    if (stroke < 14.0F) {
      float sweep = (-65.0F * stroke + stroke * stroke) / (-65.0F * 14.0F + 14.0F * 14.0F);
      model.bipedRightArm.rotateAngleX = mix(model.bipedRightArm.rotateAngleX, 0.0F, amount);
      model.bipedLeftArm.rotateAngleX = mixAngle(model.bipedLeftArm.rotateAngleX, 0.0F, amount);
      model.bipedRightArm.rotateAngleZ = mix(model.bipedRightArm.rotateAngleZ,
          (float) Math.PI - 1.8707964F * sweep, amount);
      model.bipedLeftArm.rotateAngleZ = mixAngle(model.bipedLeftArm.rotateAngleZ,
          (float) Math.PI + 1.8707964F * sweep, amount);
    } else if (stroke < 22.0F) {
      float sweep = (stroke - 14.0F) / 8.0F;
      model.bipedRightArm.rotateAngleX = mix(model.bipedRightArm.rotateAngleX,
          1.5707964F * sweep, amount);
      model.bipedLeftArm.rotateAngleX = mixAngle(model.bipedLeftArm.rotateAngleX,
          1.5707964F * sweep, amount);
      model.bipedRightArm.rotateAngleZ = mix(model.bipedRightArm.rotateAngleZ,
          1.2707963F + 1.8707964F * sweep, amount);
      model.bipedLeftArm.rotateAngleZ = mixAngle(model.bipedLeftArm.rotateAngleZ,
          5.012389F - 1.8707964F * sweep, amount);
    } else {
      float sweep = (stroke - 22.0F) / 4.0F;
      model.bipedRightArm.rotateAngleX = mix(model.bipedRightArm.rotateAngleX,
          1.5707964F * (1.0F - sweep), amount);
      model.bipedLeftArm.rotateAngleX = mixAngle(model.bipedLeftArm.rotateAngleX,
          1.5707964F * (1.0F - sweep), amount);
      model.bipedRightArm.rotateAngleZ = mix(model.bipedRightArm.rotateAngleZ,
          (float) Math.PI, amount);
      model.bipedLeftArm.rotateAngleZ = mixAngle(model.bipedLeftArm.rotateAngleZ,
          (float) Math.PI, amount);
    }
    applySwing(model.bipedRightArm, model, model.onGround, amount);
    applySwing(model.bipedLeftArm, model,
        offhandSwing((EntityPlayer) entity, age - entity.ticksExisted), amount);
    model.bipedRightLeg.rotateAngleX = mix(model.bipedRightLeg.rotateAngleX,
        0.3F * (float) Math.cos(limbSwing / 3.0F), amount);
    model.bipedLeftLeg.rotateAngleX = mix(model.bipedLeftLeg.rotateAngleX,
        0.3F * (float) Math.cos(limbSwing / 3.0F + Math.PI), amount);
    model.bipedHeadwear.rotateAngleX = model.bipedHead.rotateAngleX;
    model.bipedHeadwear.rotateAngleY = model.bipedHead.rotateAngleY;
  }

  private static float mix(float from, float to, float amount) {
    return from + (to - from) * amount;
  }

  private static void applySwing(ModelRenderer arm, ModelBiped model, float swing, float amount) {
    if (swing <= 0.0F) return;
    float eased = 1.0F - swing;
    eased *= eased;
    eased *= eased;
    eased = 1.0F - eased;
    float strike = (float) Math.sin(eased * Math.PI);
    float pitch = (float) Math.sin(swing * Math.PI)
        * -(model.bipedHead.rotateAngleX - 0.7F) * 0.75F;
    arm.rotateAngleX -= (strike * 1.2F + pitch) * amount;
    arm.rotateAngleZ -= (float) Math.sin(swing * Math.PI) * 0.4F * amount;
  }

  private static Method findOffhandSwing() {
    try {
      return Class.forName("mods.battlegear2.api.core.IBattlePlayer")
          .getMethod("getOffSwingProgress", float.class);
    } catch (ClassNotFoundException exception) {
      return null;
    } catch (NoSuchMethodException exception) {
      return null;
    }
  }

  private static float offhandSwing(EntityPlayer player, float partialTicks) {
    if (OFFHAND_SWING == null || !OFFHAND_SWING.getDeclaringClass().isInstance(player)) return 0.0F;
    try {
      return ((Number) OFFHAND_SWING.invoke(player, partialTicks)).floatValue();
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("Cannot read MF2 offhand animation", exception);
    }
  }

  private static float mixAngle(float from, float to, float amount) {
    float difference = (to - from) % ((float) Math.PI * 2.0F);
    if (difference >= Math.PI) difference -= (float) Math.PI * 2.0F;
    if (difference < -Math.PI) difference += (float) Math.PI * 2.0F;
    return from + difference * amount;
  }

  private static float poseAmount(EntityPlayer player, float partialTicks) {
    Pose pose = POSES.get(player);
    if (pose == null) {
      pose = new Pose();
      pose.tick = player.ticksExisted - 1;
      POSES.put(player, pose);
    }
    if (pose.tick != player.ticksExisted) {
      int elapsed = Math.min(20, Math.max(1, player.ticksExisted - pose.tick));
      for (int i = 0; i < elapsed; i++) {
        pose.previous = pose.current;
        pose.current = mix(pose.current, SwimmingHooks.isSwimming(player) ? 1.0F : 0.0F, 0.2F);
      }
      pose.tick = player.ticksExisted;
    }
    float result = mix(pose.previous, pose.current,
        Math.max(0.0F, Math.min(1.0F, partialTicks)));
    return result < 0.001F ? 0.0F : result;
  }
}
