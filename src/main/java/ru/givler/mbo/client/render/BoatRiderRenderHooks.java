package ru.givler.mbo.client.render;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import ru.givler.mbo.entity.boat.EntityMBOBoat;

public final class BoatRiderRenderHooks {
  private BoatRiderRenderHooks() {}

  private static EntityMBOBoat driverBoat(Entity rider) {
    if (rider == null || !(rider.ridingEntity instanceof EntityMBOBoat)) return null;
    EntityMBOBoat boat = (EntityMBOBoat) rider.ridingEntity;
    return boat.riddenByEntity == rider ? boat : null;
  }

  public static float bodyYaw(EntityLivingBase rider, float original, float partial) {
    EntityMBOBoat boat = driverBoat(rider);
    return boat == null ? original : boat.prevRotationYaw
        + MathHelper.wrapAngleTo180_float(boat.rotationYaw - boat.prevRotationYaw) * partial;
  }

  public static void pose(ModelBiped model, Entity rider) {
    if (driverBoat(rider) == null) return;
    // Keep vanilla head/aimed-arm animation, but remove attack swing's torso twist.
    float turn = model.bipedBody.rotateAngleY;
    float sin = MathHelper.sin(turn), cos = MathHelper.cos(turn);
    untwistPivot(model.bipedRightArm, sin, cos);
    untwistPivot(model.bipedLeftArm, sin, cos);
    model.bipedBody.rotateAngleY = 0F;
  }

  private static void untwistPivot(net.minecraft.client.model.ModelRenderer arm, float sin, float cos) {
    float x = arm.rotationPointX, z = arm.rotationPointZ;
    arm.rotationPointX = x * cos - z * sin;
    arm.rotationPointZ = z * cos + x * sin;
  }
}
