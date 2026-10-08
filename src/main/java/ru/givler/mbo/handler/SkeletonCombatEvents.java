package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import ru.givler.mbo.entity.ai.EntityAISkeletonBowAttack;
import ru.givler.mbo.entity.ai.EntityAISkeletonMeleeAttack;
import ru.givler.mbo.entity.ai.SkeletonCombatState;

/** Adapt vanilla arrow goals reinstalled by MF2 modes without restoring removed panic tasks. */
public final class SkeletonCombatEvents {
  private static final Field RUNNING_TASKS =
      ReflectionHelper.findField(EntityAITasks.class, "executingTaskEntries", "field_75780_b");
  private static final Field ARROW_SPEED =
      ReflectionHelper.findField(EntityAIArrowAttack.class, "entityMoveSpeed", "field_75321_e");
  private static final Field ARROW_RADIUS =
      ReflectionHelper.findField(EntityAIArrowAttack.class, "field_96562_i");
  private static final Field MELEE_SPEED =
      ReflectionHelper.findField(
          EntityAIAttackOnCollide.class, "speedTowardsTarget", "field_75440_e");
  private static final Field MELEE_MEMORY =
      ReflectionHelper.findField(EntityAIAttackOnCollide.class, "longMemory", "field_75437_f");

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void join(EntityJoinWorldEvent event) {
    if (!event.world.isRemote && event.entity instanceof EntitySkeleton) {
      EntitySkeleton skeleton = (EntitySkeleton) event.entity;
      refresh(skeleton);
      boolean avoids = false;
      for (Object raw : skeleton.tasks.taskEntries)
        if (((EntityAITaskEntry) raw).action instanceof EntityAIAvoidEntity) avoids = true;
      if (!avoids)
        skeleton.tasks.addTask(
            3, new EntityAIAvoidEntity(skeleton, EntityWolf.class, 6F, 1D, 1.2D));
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void tick(LivingUpdateEvent event) {
    if (event.entityLiving.worldObj.isRemote || !(event.entityLiving instanceof EntitySkeleton))
      return;
    EntitySkeleton skeleton = (EntitySkeleton) event.entityLiving;
    if (skeleton.ticksExisted % 20 == 0) refresh(skeleton);
    boolean fighting = false;
    if (skeleton.getAttackTarget() != null && skeleton.getAttackTarget().isEntityAlive()) {
      try {
        for (Object raw : (List) RUNNING_TASKS.get(skeleton.tasks)) {
          EntityAIBase action = ((EntityAITaskEntry) raw).action;
          if (action instanceof EntityAIArrowAttack || action instanceof EntityAIAttackOnCollide) {
            fighting = true;
            break;
          }
        }
      } catch (IllegalAccessException failure) {
        throw new IllegalStateException("Cannot read skeleton combat pose", failure);
      }
    }
    SkeletonCombatState.update(skeleton, fighting);
  }

  public static void refresh(EntitySkeleton skeleton) {
    List<EntityAITaskEntry> replacements = null;
    for (Object raw : skeleton.tasks.taskEntries) {
      EntityAITaskEntry entry = (EntityAITaskEntry) raw;
      if (entry.action.getClass() == EntityAIArrowAttack.class
          || entry.action.getClass() == EntityAIAttackOnCollide.class) {
        if (replacements == null) replacements = new ArrayList<EntityAITaskEntry>();
        replacements.add(entry);
      }
    }
    if (replacements == null) return;
    for (EntityAITaskEntry entry : replacements) {
      try {
        if (entry.action.getClass() == EntityAIArrowAttack.class) {
          EntityAISkeletonBowAttack goal =
              new EntityAISkeletonBowAttack(
                  skeleton,
                  ARROW_SPEED.getDouble(entry.action),
                  20,
                  60,
                  ARROW_RADIUS.getFloat(entry.action));
          replaceTask(skeleton, entry, goal);
        } else {
          EntityAISkeletonMeleeAttack goal =
              new EntityAISkeletonMeleeAttack(
                  skeleton,
                  null,
                  MELEE_SPEED.getDouble(entry.action),
                  MELEE_MEMORY.getBoolean(entry.action));
          replaceTask(skeleton, entry, goal);
        }
      } catch (IllegalAccessException failure) {
        throw new IllegalStateException("Cannot preserve skeleton combat task parameters", failure);
      }
    }
  }

  private static void replaceTask(
      EntitySkeleton skeleton, EntityAITaskEntry entry, EntityAIBase goal) {
    int index = skeleton.tasks.taskEntries.indexOf(entry);
    skeleton.tasks.removeTask(entry.action);
    skeleton.tasks.addTask(entry.priority, goal);
    // Equal-priority MF2 tasks keep their original scheduling order.
    Object added = skeleton.tasks.taskEntries.remove(skeleton.tasks.taskEntries.size() - 1);
    skeleton.tasks.taskEntries.add(index, added);
  }
}
