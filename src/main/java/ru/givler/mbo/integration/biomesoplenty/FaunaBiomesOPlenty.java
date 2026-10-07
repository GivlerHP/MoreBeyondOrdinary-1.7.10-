package ru.givler.mbo.integration.biomesoplenty;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Optional fauna ingredients, resolved after BoP finishes registering its blocks. */
public final class FaunaBiomesOPlenty {
  private static Block bamboo;
  private static Item bambooItem;

  private FaunaBiomesOPlenty() {}

  public static void init() {
    bamboo = null;
    bambooItem = null;
    if (!Loader.isModLoaded("BiomesOPlenty")) return;
    bamboo = GameRegistry.findBlock("BiomesOPlenty", "bamboo");
    if (bamboo != null) bambooItem = Item.getItemFromBlock(bamboo);
    if (bambooItem == null)
      FMLLog.warning("[MBO fauna] BoP bamboo is unavailable; panda bamboo food cannot be enabled.");
  }

  public static boolean isBamboo(ItemStack stack) {
    return stack != null && bambooItem != null && stack.getItem() == bambooItem;
  }

  public static boolean isBamboo(Block block) {
    return bamboo != null && block == bamboo;
  }

  public static Item bambooItem() {
    return bambooItem;
  }
}
