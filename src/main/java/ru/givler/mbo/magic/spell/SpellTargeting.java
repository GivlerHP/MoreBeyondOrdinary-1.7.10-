package ru.givler.mbo.magic.spell;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

final class SpellTargeting {
  private SpellTargeting() {}

  @SuppressWarnings("unchecked")
  static EntityLivingBase livingInSight(EntityLivingBase caster, double range) {
    return livingInSight(caster, range, 0.0D);
  }

  @SuppressWarnings("unchecked")
  static EntityLivingBase livingInSight(EntityLivingBase caster, double range, double aimMargin) {
    Vec3 start =
        Vec3.createVectorHelper(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);
    Vec3 look = caster.getLookVec();
    Vec3 end = start.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);
    MovingObjectPosition blockHit = caster.worldObj.rayTraceBlocks(start, end);
    double closest = blockHit == null ? range : start.distanceTo(blockHit.hitVec);
    EntityLivingBase result = null;
    AxisAlignedBB search =
        caster
            .boundingBox
            .addCoord(look.xCoord * range, look.yCoord * range, look.zCoord * range)
            .expand(1.0D, 1.0D, 1.0D);
    List<Entity> entities = caster.worldObj.getEntitiesWithinAABBExcludingEntity(caster, search);
    for (Entity entity : entities) {
      if (!(entity instanceof EntityLivingBase) || !entity.isEntityAlive()) continue;
      double border = Math.max(entity.getCollisionBorderSize(), aimMargin);
      AxisAlignedBB bounds = entity.boundingBox.expand(border, border, border);
      MovingObjectPosition hit = bounds.calculateIntercept(start, end);
      if (bounds.isVecInside(start)) {
        if (closest >= 0.0D) {
          result = (EntityLivingBase) entity;
          closest = 0.0D;
        }
      } else if (hit != null) {
        double distance = start.distanceTo(hit.hitVec);
        if (distance < closest) {
          result = (EntityLivingBase) entity;
          closest = distance;
        }
      }
    }
    return result;
  }

  /** Forgiving server-side targeting for instant rays; never selects through a solid block. */
  @SuppressWarnings("unchecked")
  static EntityLivingBase livingNearSight(EntityLivingBase caster, double range, double radius) {
    EntityLivingBase exact = livingInSight(caster, range, radius);
    if (exact != null) return exact;

    Vec3 start =
        Vec3.createVectorHelper(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);
    Vec3 look = caster.getLookVec().normalize();
    Vec3 end = start.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);
    MovingObjectPosition blockHit = caster.worldObj.rayTraceBlocks(start, end);
    double visibleRange = blockHit == null ? range : start.distanceTo(blockHit.hitVec);
    AxisAlignedBB search =
        caster
            .boundingBox
            .addCoord(
                look.xCoord * visibleRange, look.yCoord * visibleRange, look.zCoord * visibleRange)
            .expand(radius, radius, radius);
    List<Entity> entities = caster.worldObj.getEntitiesWithinAABBExcludingEntity(caster, search);
    EntityLivingBase result = null;
    double nearest = visibleRange + 1.0D;
    for (Entity entity : entities) {
      if (!(entity instanceof EntityLivingBase) || !entity.isEntityAlive()) continue;
      double cx = entity.posX;
      double cy = entity.boundingBox.minY + entity.height * 0.5D;
      double cz = entity.posZ;
      double dx = cx - start.xCoord;
      double dy = cy - start.yCoord;
      double dz = cz - start.zCoord;
      double along = dx * look.xCoord + dy * look.yCoord + dz * look.zCoord;
      if (along < 0.0D || along > visibleRange || along >= nearest) continue;
      double distanceSq = dx * dx + dy * dy + dz * dz - along * along;
      double allowed = radius + entity.width * 0.5D;
      if (distanceSq <= allowed * allowed) {
        result = (EntityLivingBase) entity;
        nearest = along;
      }
    }
    return result;
  }
}
