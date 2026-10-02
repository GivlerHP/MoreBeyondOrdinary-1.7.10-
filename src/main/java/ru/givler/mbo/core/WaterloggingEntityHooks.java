package ru.givler.mbo.core;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
import ru.givler.mbo.waterlogging.ClientWaterloggedBlocks;
import ru.givler.mbo.waterlogging.WaterloggedBlockSupport;
import ru.givler.mbo.waterlogging.WaterloggedGeometry;
import ru.givler.mbo.waterlogging.WaterloggedWorldData;

public final class WaterloggingEntityHooks {
  private static final Field IN_WATER = ReflectionHelper.findField(
      Entity.class, "inWater", "field_70171_ac");
  private WaterloggingEntityHooks() {}

  public static boolean mergeWaterMovement(boolean vanilla, Entity entity) {
    if (vanilla) return true;
    if (!touchesWaterlogged(entity)
        && !(entity instanceof net.minecraft.entity.player.EntityPlayer
            && ru.givler.mbo.swimming.SwimmingHooks.shouldUseWaterMovement(
                (net.minecraft.entity.player.EntityPlayer) entity))) return false;
    try {
      IN_WATER.setBoolean(entity, true);
    } catch (IllegalAccessException exception) {
      throw new IllegalStateException("Cannot set entity water state", exception);
    }
    return true;
  }

  public static boolean isInsideWaterlogged(Entity entity, Material material) {
    if (material != Material.water || entity == null || entity.worldObj == null) return false;
    double eyeY = entity.posY + entity.getEyeHeight();
    return isInsideWaterloggedAt(entity, entity.posX, eyeY, entity.posZ);
  }

  public static boolean isInsideWaterloggedAt(Entity entity, double eyeX, double eyeY, double eyeZ) {
    if (entity == null || entity.worldObj == null) return false;
    World world = entity.worldObj;
    int x = floor(eyeX);
    int y = floor(eyeY);
    int z = floor(eyeZ);
    boolean waterlogged =
        world.isRemote
            ? ClientWaterloggedBlocks.contains(world.provider.dimensionId, x, y, z)
            : WaterloggedWorldData.get(world).contains(x, y, z);
    if (!waterlogged || !WaterloggedBlockSupport.canWaterlog(world, x, y, z)) return false;
    AxisAlignedBB eye =
        AxisAlignedBB.getBoundingBox(
            eyeX - 0.001D,
            eyeY - 0.001D,
            eyeZ - 0.001D,
            eyeX + 0.001D,
            eyeY + 0.001D,
            eyeZ + 0.001D);
    return WaterloggedGeometry.intersects(world, x, y, z, eye);
  }

  public static boolean touchesWaterlogged(Entity entity) {
    if (entity == null || entity.worldObj == null) return false;
    World world = entity.worldObj;
    AxisAlignedBB box = entity.boundingBox.contract(0.001D, 0.0D, 0.001D);
    int minX = floor(box.minX), maxX = floor(box.maxX);
    int minY = floor(box.minY), maxY = floor(box.maxY);
    int minZ = floor(box.minZ), maxZ = floor(box.maxZ);
    for (int x = minX; x <= maxX; x++)
      for (int y = minY; y <= maxY; y++)
        for (int z = minZ; z <= maxZ; z++) {
          boolean waterlogged =
              world.isRemote
                  ? ClientWaterloggedBlocks.contains(world.provider.dimensionId, x, y, z)
                  : WaterloggedWorldData.get(world).contains(x, y, z);
          if (waterlogged
              && WaterloggedBlockSupport.canWaterlog(world, x, y, z)
              && WaterloggedGeometry.intersects(world, x, y, z, box)) return true;
        }
    return false;
  }

  public static void resetFallDistanceInWaterlogged(Entity entity) {
    if (!touchesWaterlogged(entity)) return;
    float fallDistance = entity.fallDistance;
    if (entity.worldObj.isRemote && fallDistance > 2.0F) spawnLandingSplash(entity);
    entity.fallDistance = 0.0F;
  }

  private static void spawnLandingSplash(Entity entity) {
    float speed = (float) Math.sqrt(entity.motionX * entity.motionX * 0.2D + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ * 0.2D);
    float volume = Math.min(1.0F, speed * 0.35F);
    entity.worldObj.playSound(
        entity.posX,
        entity.posY,
        entity.posZ,
        "game.neutral.swim.splash",
        volume,
        1.0F + (entity.worldObj.rand.nextFloat() - entity.worldObj.rand.nextFloat()) * 0.4F,
        false);
    int particles = Math.max(4, (int) (1.0F + entity.width * 20.0F));
    double surfaceY = floor(entity.boundingBox.minY) + 0.89D;
    for (int i = 0; i < particles; i++) {
      double x =
          entity.posX + (entity.worldObj.rand.nextDouble() * 2.0D - 1.0D) * entity.width;
      double z =
          entity.posZ + (entity.worldObj.rand.nextDouble() * 2.0D - 1.0D) * entity.width;
      entity.worldObj.spawnParticle(
          "bubble",
          x,
          surfaceY,
          z,
          entity.motionX,
          entity.motionY - entity.worldObj.rand.nextDouble() * 0.2D,
          entity.motionZ);
      entity.worldObj.spawnParticle("splash", x, surfaceY, z, entity.motionX, entity.motionY, entity.motionZ);
    }
  }

  private static int floor(double value) {
    int integer = (int) value;
    return value < integer ? integer - 1 : integer;
  }
}
