package ru.givler.mbo.client.render;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import ru.givler.mbo.entity.boat.EntityMBOBoat;

public final class LivingEntityRenderHooks {
  private LivingEntityRenderHooks() {}

  public static float modelHeadYaw(EntityLivingBase animal,float original,float body,float partial) {
    if(!ru.givler.mbo.magic.possession.PossessionManager.isControlled(animal))return original;
    float torso=bodyYaw(animal,body,partial);
    // EntityLookHelper's normal horizontal limit, relative to the rendered torso.
    return torso+MathHelper.clamp_float(MathHelper.wrapAngleTo180_float(original-torso),-75F,75F);
  }

  public static float modelHeadPitch(EntityLivingBase animal,float original) {
    return ru.givler.mbo.magic.possession.PossessionManager.isControlled(animal)?MathHelper.clamp_float(original,-90F,90F):original;
  }

  private static EntityMBOBoat driverBoat(Entity rider) {
    if (rider == null || !(rider.ridingEntity instanceof EntityMBOBoat)) return null;
    EntityMBOBoat boat = (EntityMBOBoat) rider.ridingEntity;
    return boat.riddenByEntity == rider ? boat : null;
  }

  public static float bodyYaw(EntityLivingBase rider, float original, float partial) {
    if (rider.ridingEntity instanceof ru.givler.mbo.entity.EntityHangingAnchor)
      return rider.ridingEntity.rotationYaw;
    EntityMBOBoat boat = driverBoat(rider);
    return boat == null ? original : boat.prevRotationYaw
        + MathHelper.wrapAngleTo180_float(boat.rotationYaw - boat.prevRotationYaw) * partial;
  }

  public static void pose(ModelBiped model, Entity rider) {
    boolean hanging = rider.ridingEntity instanceof ru.givler.mbo.entity.EntityHangingAnchor;
    if (!hanging && driverBoat(rider) == null) return;
    // Keep vanilla head/aimed-arm animation, but remove attack swing's torso twist.
    float turn = model.bipedBody.rotateAngleY;
    float sin = MathHelper.sin(turn), cos = MathHelper.cos(turn);
    untwistPivot(model.bipedRightArm, sin, cos);
    untwistPivot(model.bipedLeftArm, sin, cos);
    model.bipedBody.rotateAngleY = 0F;
    if (hanging && ((ru.givler.mbo.entity.EntityHangingAnchor) rider.ridingEntity).isSuffocating()) {
      model.bipedHead.rotateAngleX = model.bipedHead.rotateAngleY = 0;
      model.bipedHeadwear.rotateAngleX = model.bipedHeadwear.rotateAngleY = 0;
    }
  }

  private static void untwistPivot(net.minecraft.client.model.ModelRenderer arm, float sin, float cos) {
    float x = arm.rotationPointX, z = arm.rotationPointZ;
    arm.rotationPointX = x * cos - z * sin;
    arm.rotationPointZ = z * cos + x * sin;
  }
}
