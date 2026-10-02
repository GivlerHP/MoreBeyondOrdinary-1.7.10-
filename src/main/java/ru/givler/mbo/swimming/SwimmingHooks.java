package ru.givler.mbo.swimming;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;
import ru.givler.mbo.core.WaterloggingEntityHooks;

/** Shared swimming state: vanilla sprint flags already synchronize to other clients. */
public final class SwimmingHooks {
  private static final Method SET_SIZE = ReflectionHelper.findMethod(
      net.minecraft.entity.Entity.class, null,
      new String[] {"setSize", "func_70105_a"}, float.class, float.class);
  private static final Field IS_JUMPING = ReflectionHelper.findField(
      EntityLivingBase.class, "isJumping", "field_70703_bu");
  private static final Map<EntityPlayer, Integer> LAST_WET_TICK =
      new WeakHashMap<EntityPlayer, Integer>();

  private SwimmingHooks() {}

  public static boolean wantsToSwim(EntityPlayer player) {
    if (player == null || !player.isEntityAlive() || player.isRiding()
        || player.capabilities.isFlying || !player.isSprinting()) return false;
    boolean wet = waterForSwimming(player);
    return wet && (isSwimming(player) || player.isInsideOfMaterial(Material.water));
  }

  /** The vanilla water test cuts 0.4 blocks off the bottom of a 0.6-block swimmer. */
  public static boolean shouldUseWaterMovement(EntityPlayer player) {
    return isSwimming(player) && waterForSwimming(player);
  }

  private static boolean waterForSwimming(EntityPlayer player) {
    boolean wet = touchesWater(player);
    synchronized (LAST_WET_TICK) {
      if (wet) LAST_WET_TICK.put(player, player.ticksExisted);
      Integer lastWet = LAST_WET_TICK.get(player);
      boolean recentlyWet = isSwimming(player) && lastWet != null
          && player.ticksExisted - lastWet >= 0 && player.ticksExisted - lastWet <= 3;
      return wet || recentlyWet;
    }
  }

  private static boolean touchesWater(EntityPlayer player) {
    AxisAlignedBB box = player.boundingBox.contract(0.001D, 0.001D, 0.001D);
    return player.worldObj.isMaterialInBB(box, Material.water)
        || WaterloggingEntityHooks.touchesWaterlogged(player);
  }

  public static boolean isSwimming(EntityPlayer player) {
    return player != null && player.height < 1.0F;
  }

  /** A one-block opening must not cancel sprint before the swim hitbox can enter it. */
  public static boolean collisionStopsSprint(EntityPlayer player) {
    return player.isCollidedHorizontally && !isSwimming(player) && !wantsToSwim(player);
  }

  public static float eyeHeight(float vanilla, EntityPlayer player) {
    // In 1.7.10 posY already includes yOffset; getEyeHeight is an offset from posY.
    return isSwimming(player) ? 0.4F - player.yOffset + player.getDefaultEyeHeight() : vanilla;
  }

  public static boolean canTriggerWalking(boolean vanilla, EntityPlayer player) {
    return vanilla && !isSwimming(player);
  }

  public static double waterDrag(double vanilla, EntityLivingBase entity) {
    return entity instanceof EntityPlayer && wantsToSwim((EntityPlayer) entity)
        ? 0.9D : vanilla;
  }

  public static double swimGravity(double vanilla, EntityLivingBase entity) {
    return entity instanceof EntityPlayer && wantsToSwim((EntityPlayer) entity)
        ? 0.005D : vanilla;
  }

  public static void swimMoveFlying(EntityLivingBase entity, float strafe, float forward, float speed) {
    if (!(entity instanceof EntityPlayer) || !wantsToSwim((EntityPlayer) entity)) {
      entity.moveFlying(strafe, forward, speed);
      return;
    }
    double oldX = entity.motionX;
    double oldZ = entity.motionZ;
    entity.moveFlying(strafe, forward, speed);
    // Keep the existing horizontal water acceleration, redirecting it toward
    // the view so looking straight up does not also push the player forward.
    double pitch = Math.toRadians(entity.rotationPitch);
    double horizontal = Math.abs(Math.cos(pitch));
    entity.motionX = oldX + (entity.motionX - oldX) * horizontal;
    entity.motionZ = oldZ + (entity.motionZ - oldZ) * horizontal;
    // Player.travel (1.13.2) applies this before water drag and gravity.
    // At the surface upward steering requires water overhead or the jump key.
    double lookY = -Math.sin(pitch);
    if (lookY <= 0.0D || isJumping(entity) || hasWaterAbove((EntityPlayer) entity))
      entity.motionY = swimPitchVelocity(entity.motionY, lookY);
  }

  public static double swimPitchVelocity(double velocity, double lookY) {
    double response = lookY < -0.2D ? 0.085D : 0.06D;
    return velocity + (lookY - velocity) * response;
  }

  private static boolean isJumping(EntityLivingBase entity) {
    try {
      return IS_JUMPING.getBoolean(entity);
    } catch (IllegalAccessException exception) {
      throw new IllegalStateException("Cannot read swimming jump input", exception);
    }
  }

  private static boolean hasWaterAbove(EntityPlayer player) {
    double y = player.boundingBox.minY + 0.9D;
    int xBlock = MathHelper.floor_double(player.posX);
    int yBlock = MathHelper.floor_double(y);
    int zBlock = MathHelper.floor_double(player.posZ);
    return player.worldObj.getBlock(xBlock, yBlock, zBlock).getMaterial() == Material.water
        || WaterloggingEntityHooks.isInsideWaterloggedAt(player, player.posX, y, player.posZ);
  }

  public static void updateSize(EntityPlayer player) {
    if (player == null || player.worldObj == null) return;
    boolean swim = wantsToSwim(player);
    if (swim) {
      if (!isSwimming(player)) setSize(player, 0.6F);
    } else if (isSwimming(player) && canStand(player)) {
      setSize(player, 1.8F);
    }
  }

  private static boolean canStand(EntityPlayer player) {
    World world = player.worldObj;
    AxisAlignedBB standing = AxisAlignedBB.getBoundingBox(
        player.posX - 0.3D, player.boundingBox.minY, player.posZ - 0.3D,
        player.posX + 0.3D, player.boundingBox.minY + 1.8D, player.posZ + 0.3D);
    return world.getCollidingBoundingBoxes(player, standing).isEmpty();
  }

  private static void setSize(EntityPlayer player, float height) {
    try {
      SET_SIZE.invoke(player, 0.6F, height);
    } catch (Exception exception) {
      throw new IllegalStateException("Cannot change player swimming size", exception);
    }
  }
}
