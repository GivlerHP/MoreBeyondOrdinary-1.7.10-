package ru.givler.mbo.core;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatList;

public final class GuiStatsHooks {
  private GuiStatsHooks() {}

  public static List<StatCrafting> getSafeBlockStats() {
    List<StatCrafting> safeStats = new ArrayList<StatCrafting>(StatList.objectMineStats.size());
    int skipped = 0;

    for (Object entry : StatList.objectMineStats) {
      if (!(entry instanceof StatCrafting)) {
        skipped++;
        continue;
      }

      StatCrafting stat = (StatCrafting) entry;
      Item item = stat.func_150959_a();
      int itemId = Item.getIdFromItem(item);
      if (item == null
          || itemId < 0
          || itemId >= StatList.objectUseStats.length
          || itemId >= StatList.objectCraftStats.length) {
        skipped++;
        continue;
      }
      safeStats.add(stat);
    }

    if (skipped > 0) {
      System.err.println(
          "[MBO] Ignored " + skipped + " invalid block statistic(s) while opening GuiStats");
    }
    return safeStats;
  }
}
