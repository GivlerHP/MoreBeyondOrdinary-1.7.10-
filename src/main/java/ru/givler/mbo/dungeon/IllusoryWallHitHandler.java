package ru.givler.mbo.dungeon;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import java.util.HashSet;
import java.util.Set;

/** Turns the first ordinary left click into an activation instead of block damage. */
public final class IllusoryWallHitHandler {
  private static boolean activate(World world, DungeonAreaRecord area) {
    if (area == null || area.getType() != DungeonAreaRecord.ILLUSORY || area.isActivated()
        || !area.activate(world)) return false;
    world.playSoundEffect(area.getX() + 0.5D, area.getY() + 0.5D, area.getZ() + 0.5D,
        "mbo:illusory_wall", 1.0F, 1.0F);
    return true;
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onExplosion(ExplosionEvent.Detonate event) {
    if (event.world.isRemote) return;
    DungeonAreaSavedData data = DungeonAreaSavedData.get(event.world);
    Set<DungeonAreaRecord> hit = new HashSet<DungeonAreaRecord>();
    for (ChunkPosition pos : event.getAffectedBlocks()) {
      DungeonAreaRecord area = data.at(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
      if (area != null) hit.add(area);
    }
    boolean changed = false;
    for (DungeonAreaRecord area : hit) changed |= activate(event.world, area);
    if (changed) data.changed(event.world);
  }

  /** Check the projectile's next movement before vanilla embeds or removes it on impact. */
  public static void hitProjectiles(World world) {
    DungeonAreaSavedData data = DungeonAreaSavedData.get(world);
    boolean hasWall = false;
    for (DungeonAreaRecord area : data.all())
      if (area.getType() == DungeonAreaRecord.ILLUSORY && !area.isActivated()) {
        hasWall = true;
        break;
      }
    if (!hasWall) return;
    boolean changed = false;
    for (Object entry : world.loadedEntityList) {
      Entity projectile = (Entity) entry;
      if (projectile.isDead || !(projectile instanceof IProjectile || projectile instanceof EntityFireball)) continue;
      if (projectile.motionX * projectile.motionX + projectile.motionY * projectile.motionY
          + projectile.motionZ * projectile.motionZ < 1.0E-8D) continue;
      Vec3 start = Vec3.createVectorHelper(projectile.posX, projectile.posY, projectile.posZ);
      Vec3 end = start.addVector(projectile.motionX, projectile.motionY, projectile.motionZ);
      MovingObjectPosition block = world.func_147447_a(start, end, false, true, false);
      if (block == null || block.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) continue;
      DungeonAreaRecord area = data.at(block.blockX, block.blockY, block.blockZ);
      if (area == null || area.getType() != DungeonAreaRecord.ILLUSORY || area.isActivated()) continue;
      // An entity in front of the wall must receive the impact instead.
      Entity owner = projectile instanceof EntityArrow ? ((EntityArrow) projectile).shootingEntity
          : projectile instanceof EntityThrowable ? ((EntityThrowable) projectile).getThrower()
          : projectile instanceof EntityFireball ? ((EntityFireball) projectile).shootingEntity : null;
      boolean intercepted = false;
      for (Object candidate : world.getEntitiesWithinAABBExcludingEntity(projectile,
          projectile.boundingBox.addCoord(projectile.motionX, projectile.motionY, projectile.motionZ).expand(1D, 1D, 1D))) {
        Entity entity = (Entity) candidate;
        if (!entity.canBeCollidedWith() || entity == owner && projectile.ticksExisted < 5) continue;
        MovingObjectPosition impact = entity.boundingBox.expand(0.3D, 0.3D, 0.3D).calculateIntercept(start, block.hitVec);
        if (impact != null) { intercepted = true; break; }
      }
      if (!intercepted) changed |= activate(world, area);
    }
    if (changed) data.changed(world);
  }

  /** Area tools raise a break event without a direct left click on the wall. */
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onBreak(BlockEvent.BreakEvent event) {
    if (event.world.isRemote) return;
    DungeonAreaRecord area = DungeonAreaSavedData.get(event.world).at(event.x, event.y, event.z);
    if (area != null && area.getType() == DungeonAreaRecord.ILLUSORY && !area.isActivated()) {
      event.setCanceled(true);
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onInteract(PlayerInteractEvent event) {
    if (event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) return;
    boolean matched;
    if (event.world.isRemote) {
      DungeonAreaRecord area = ClientDungeonAreas.at(event.x, event.y, event.z);
      matched = area != null && area.getType() == 1 && !area.isActivated();
    } else {
      DungeonAreaSavedData data = DungeonAreaSavedData.get(event.world);
      DungeonAreaRecord area = data.at(event.x, event.y, event.z);
      matched = area != null && area.getType() == 1 && !area.isActivated();
      if (matched && area.activate(event.world)) {
        event.world.playSoundEffect(
            event.x + 0.5D, event.y + 0.5D, event.z + 0.5D, "mbo:illusory_wall", 1.0F, 1.0F);
        data.changed(event.world);
      }
    }
    if (matched) event.setCanceled(true);
  }
}
