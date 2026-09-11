package ru.givler.mbo.dungeon;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import ru.givler.mbo.lootcontainer.LootContainerData;
import ru.givler.mbo.lootcontainer.action.LootContainerAction;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

/** Reuses the validated LootContainer actions without placing a TileEntity in the world. */
final class DungeonTriggerExecutor {
  private DungeonTriggerExecutor() {}

  static void execute(World world, int x, int y, int z, Entity source, String json, boolean multi) {
    if (world == null || world.isRemote) return;
    List<LootContainerAction> actions;
    try {
      actions = LootContainerAction.fromJsonList(json);
    } catch (Exception ignored) {
      return;
    }
    if (actions.size() > LootContainerData.MAX_ACTIONS)
      actions = actions.subList(0, LootContainerData.MAX_ACTIONS);
    if (actions.isEmpty()) return;
    TileEntityLootContainer context = new TileEntityLootContainer();
    context.setWorldObj(world);
    context.xCoord = x;
    context.yCoord = y;
    context.zCoord = z;
    if (multi) {
      for (LootContainerAction action : actions)
        if (action != null
            && world.rand.nextFloat() * 100F < Math.max(0F, Math.min(100F, action.getChance())))
          action.execute(context, source);
      return;
    }
    List<LootContainerAction> valid = new ArrayList<LootContainerAction>();
    float total = 0F;
    for (LootContainerAction action : actions)
      if (action != null && action.getChance() > 0F) {
        valid.add(action);
        total += action.getChance();
      }
    if (valid.isEmpty() || total <= 0F) return;
    float roll = world.rand.nextFloat() * total, at = 0F;
    for (LootContainerAction action : valid) {
      at += action.getChance();
      if (roll < at) {
        action.execute(context, source);
        return;
      }
    }
    valid.get(valid.size() - 1).execute(context, source);
  }
}
