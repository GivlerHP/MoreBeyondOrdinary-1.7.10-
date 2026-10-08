package ru.givler.mbo.entity.ai;

import net.minecraft.entity.monster.EntitySkeleton;

/** Server-owned pose state; vanilla skeleton metadata slots end at 13. */
public final class SkeletonCombatState {
  private static final int WATCHER = 14;

  private SkeletonCombatState() {}

  public static void init(EntitySkeleton skeleton) {
    skeleton.getDataWatcher().addObject(WATCHER, Byte.valueOf((byte) 0));
  }

  public static void update(EntitySkeleton skeleton, boolean fighting) {
    skeleton.getDataWatcher().updateObject(WATCHER, Byte.valueOf((byte) (fighting ? 1 : 0)));
  }

  public static boolean fighting(EntitySkeleton skeleton) {
    return skeleton.getDataWatcher().getWatchableObjectByte(WATCHER) != 0;
  }
}
