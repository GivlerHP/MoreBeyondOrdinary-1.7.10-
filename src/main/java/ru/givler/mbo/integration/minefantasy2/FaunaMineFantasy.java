package ru.givler.mbo.integration.minefantasy2;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.givler.mbo.entity.fauna.EntityMBOFox;

/** Registry-based optional integration: no MF2 classes are loaded when the mod is absent. */
public final class FaunaMineFantasy {
  private static Block berryBush;
  private static Item berries, juicyBerries;

  private FaunaMineFantasy() {}

  public static void init() {
    berryBush = null;
    berries = null;
    juicyBerries = null;
    if (!Loader.isModLoaded("minefantasy2")) return;
    berryBush = GameRegistry.findBlock("minefantasy2", "berries");
    berries = GameRegistry.findItem("minefantasy2", "MF2_food_berries");
    juicyBerries = GameRegistry.findItem("minefantasy2", "MF2_food_berriesJuicy");
  }

  public static boolean isBerry(ItemStack stack) {
    return stack != null
        && (berries != null && stack.getItem() == berries
            || juicyBerries != null && stack.getItem() == juicyBerries);
  }

  public static boolean ripe(World world, int x, int y, int z) {
    return berryBush != null
        && world.blockExists(x, y, z)
        && world.getBlock(x, y, z) == berryBush
        && world.getBlockMetadata(x, y, z) == 0;
  }

  public static boolean harvest(World world, int x, int y, int z, EntityMBOFox fox) {
    if (world.isRemote
        || berries == null
        || !world.getGameRules().getGameRuleBooleanValue("mobGriefing")
        || !ripe(world, x, y, z)) return false;
    // MF2: one berry, a 1/10 chance of juicy berries, then empty metadata 1.
    Item drop = juicyBerries != null && world.rand.nextInt(10) == 0 ? juicyBerries : berries;
    if (!world.setBlockMetadataWithNotify(x, y, z, 1, 2)) return false;
    if (fox.getHeldItem() == null) fox.setCurrentItemOrArmor(0, new ItemStack(drop));
    else fox.entityDropItem(new ItemStack(drop), 0F);
    return true;
  }
}
