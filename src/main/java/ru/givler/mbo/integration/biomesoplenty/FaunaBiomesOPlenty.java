package ru.givler.mbo.integration.biomesoplenty;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.integration.growthcraft.FaunaGrowthcraft;

/** Optional fauna ingredients, resolved after BoP finishes registering its blocks. */
public final class FaunaBiomesOPlenty {
  private static Block bamboo;
  private static final Set<Block> bambooBlocks = new LinkedHashSet<Block>();
  private static final Set<Item> bambooItems = new LinkedHashSet<Item>();
  private static Item bambooItem;

  private FaunaBiomesOPlenty() {}

  public static void init() {
    bamboo = null;
    bambooItem = null;
    bambooBlocks.clear();
    bambooItems.clear();
    bamboo = GameRegistry.findBlock("BiomesOPlenty", "bamboo");
    if (bamboo != null) {
      bambooBlocks.add(bamboo);
      bambooItem = Item.getItemFromBlock(bamboo);
      if (bambooItem != null) bambooItems.add(bambooItem);
    }
    FaunaGrowthcraft.init();
    for (Block block : FaunaGrowthcraft.bambooBlocks()) {
      bambooBlocks.add(block);
      Item item = Item.getItemFromBlock(block);
      if (item != null) bambooItems.add(item);
    }
    if (bambooItem == null && !bambooItems.isEmpty()) bambooItem = bambooItems.iterator().next();
    if (bambooItem == null)
      FMLLog.warning(
          "[MBO fauna] No registered bamboo stalks are available; panda bamboo food cannot be enabled.");
  }

  public static boolean isBamboo(ItemStack stack) {
    return stack != null && bambooItems.contains(stack.getItem());
  }

  public static boolean isBamboo(Block block) {
    return bambooBlocks.contains(block);
  }

  public static Set<Item> bambooItems() {
    return Collections.unmodifiableSet(bambooItems);
  }

  public static Item bambooItem() {
    return bambooItem;
  }
}
