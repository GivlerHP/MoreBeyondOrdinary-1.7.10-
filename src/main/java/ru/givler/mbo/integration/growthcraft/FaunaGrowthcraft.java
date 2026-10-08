package ru.givler.mbo.integration.growthcraft;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.block.Block;

/** Resolves registered Growthcraft bamboo stalks without a required Growthcraft dependency. */
public final class FaunaGrowthcraft {
  private static final Set<Block> bamboo = new LinkedHashSet<Block>();

  private FaunaGrowthcraft() {}

  public static void init() {
    bamboo.clear();
    for (Object entry : Block.blockRegistry) {
      Block block = (Block) entry;
      String owner = block.getClass().getName().toLowerCase(Locale.ROOT);
      String name =
          String.valueOf(Block.blockRegistry.getNameForObject(block)).toLowerCase(Locale.ROOT);
      if ((owner.startsWith("growthcraft.bamboo.")
              && block.getClass().getSimpleName().equals("BlockBamboo"))
          || name.startsWith("growthcraft") && name.endsWith(":bamboo")) bamboo.add(block);
    }
  }

  public static Set<Block> bambooBlocks() {
    return Collections.unmodifiableSet(bamboo);
  }
}
