package ru.givler.mbo.entity.ai;

import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.MathHelper;

/** Adds the modern strafe operation while retaining vanilla path movement and attribute speed. */
public final class SkeletonMoveHelper extends EntityMoveHelper {
  private static final Field MOVE_HELPER =
      ReflectionHelper.findField(EntityLiving.class, "moveHelper", "field_70765_h");
  private final EntitySkeleton skeleton;
  private boolean pending;
  private boolean blocked;
  private float forward, side;
  private EntityLivingBase target;

  private SkeletonMoveHelper(EntitySkeleton skeleton) {
    super(skeleton);
    this.skeleton = skeleton;
  }

  public static void install(EntitySkeleton skeleton) {
    if (skeleton.getMoveHelper().getClass() != EntityMoveHelper.class) return;
    try {
      MOVE_HELPER.set(skeleton, new SkeletonMoveHelper(skeleton));
    } catch (IllegalAccessException failure) {
      throw new IllegalStateException("Cannot install skeleton strafe control", failure);
    }
  }

  public void strafe(float forward, float side, EntityLivingBase target) {
    this.forward = forward;
    this.side = side;
    this.target = target;
    pending = true;
  }

  public void cancelStrafe() {
    pending = false;
    blocked = false;
    target = null;
    skeleton.moveStrafing = 0F;
  }

  public boolean consumeBlockedStrafe() {
    boolean result = blocked;
    blocked = false;
    return result;
  }

  @Override
  public void onUpdateMoveHelper() {
    if (!pending) {
      skeleton.moveStrafing = 0F;
      super.onUpdateMoveHelper();
      return;
    }
    pending = false;
    float yaw =
        (float)
                (Math.atan2(target.posZ - skeleton.posZ, target.posX - skeleton.posX)
                    * 180D
                    / Math.PI)
            - 90F;
    skeleton.rotationYaw +=
        MathHelper.clamp_float(
            MathHelper.wrapAngleTo180_float(yaw - skeleton.rotationYaw), -30F, 30F);
    double angle = skeleton.rotationYaw * Math.PI / 180D;
    double dx = (side * Math.cos(angle) - forward * Math.sin(angle)) * .75D;
    double dz = (forward * Math.cos(angle) + side * Math.sin(angle)) * .75D;
    int x = MathHelper.floor_double(skeleton.posX + dx);
    int y = MathHelper.floor_double(skeleton.boundingBox.minY);
    int z = MathHelper.floor_double(skeleton.posZ + dz);
    boolean safe =
        skeleton.worldObj.blockExists(x, y - 1, z)
            && skeleton
                .worldObj
                .getCollidingBoundingBoxes(
                    skeleton, skeleton.boundingBox.getOffsetBoundingBox(dx, 0D, dz))
                .isEmpty()
            && (!skeleton.onGround
                || skeleton.worldObj.getBlock(x, y - 1, z).getMaterial().isSolid());
    blocked = !safe;
    skeleton.setAIMoveSpeed(
        (float)
                skeleton
                    .getEntityAttribute(SharedMonsterAttributes.movementSpeed)
                    .getAttributeValue()
            * .25F);
    skeleton.setMoveForward(safe ? forward : 0F);
    skeleton.moveStrafing = safe ? side : 0F;
  }
}
