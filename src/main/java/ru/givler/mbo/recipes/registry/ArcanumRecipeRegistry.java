package ru.givler.mbo.recipes.registry;

import cpw.mods.fml.common.Loader;
import minefantasy.mf2.item.list.ComponentListMF;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.recipes.ArcanumRecipesManager;
import ru.givler.mbo.registry.ItemRegistry;

public class ArcanumRecipeRegistry {
    public static void init() {
        if(Loader.isModLoaded("minefantasy2")) {
            ArcanumRecipesManager.getInstance().addRecipe(
                    new ItemStack(ItemRegistry.Crystall, 1, 0),
                    500,
                    "AIY",
                    "ZBY",
                    "ZXA",
                    'I', ComponentListMF.ruby_dust, 'X', ComponentListMF.topaz_dust, 'Y', ComponentListMF.water_shard_dust,
                    'Z', ComponentListMF.earth_shard_dust, 'A', ComponentListMF.fire_shard_dust, 'B', ComponentListMF.diamond_dust,
                    ' ', null
            );
            ArcanumRecipesManager.getInstance().addRecipe(
                    new ItemStack(ItemRegistry.Crystall, 1, 1),
                    500,
                    "AIY",
                    "ZBY",
                    "ZXA",
                    'I', ComponentListMF.peridot_dust, 'X', ComponentListMF.emerald_dust, 'Y', ComponentListMF.earth_shard_dust,
                    'Z', ComponentListMF.air_shard_dust, 'A', ComponentListMF.order_shard_dust, 'B', ComponentListMF.diamond_dust,
                    ' ', null
            );
            ArcanumRecipesManager.getInstance().addRecipe(
                    new ItemStack(ItemRegistry.Crystall, 1, 2),
                    500,
                    "AIY",
                    "ZBY",
                    "ZXA",
                    'I', ComponentListMF.tanzanite_dust, 'X', ComponentListMF.malachite_dust, 'Y', ComponentListMF.entropy_shard_dust,
                    'Z', ComponentListMF.fire_shard_dust, 'A', ComponentListMF.order_shard_dust, 'B', ComponentListMF.diamond_dust,
                    ' ', null
            );
            ArcanumRecipesManager.getInstance().addRecipe(
                    new ItemStack(ItemRegistry.Crystall, 1, 3),
                    500,
                    "AIY",
                    "ZBY",
                    "ZXA",
                    'I', ComponentListMF.sapphire_dust, 'X', ComponentListMF.amber_dust, 'Y', ComponentListMF.water_shard_dust,
                    'Z', ComponentListMF.air_shard_dust, 'A', ComponentListMF.entropy_shard_dust, 'B', ComponentListMF.diamond_dust,
                    ' ', null
            );

            ArcanumRecipesManager.getInstance().addRecipe(
                    new ItemStack(ItemRegistry.Crystall, 1, 8),
                    1200,
                    "XYZ",
                    "AIA",
                    "BAC",
                    'I', new ItemStack(ItemRegistry.Crystall, 1, 4),
                    'X', ComponentListMF.sapphire_dust, 'Y', ComponentListMF.ruby_dust, 'Z', ComponentListMF.amber_dust,
                    'A', ComponentListMF.fire_shard_dust, 'B', ComponentListMF.water_shard_dust, 'C', ComponentListMF.earth_shard_dust,
                    ' ', null
            );
            ArcanumRecipesManager.getInstance().addRecipe(
                    new ItemStack(ItemRegistry.Crystall, 1, 9),
                    1200,
                    "XYZ",
                    "CIC",
                    "ABA",
                    'I', new ItemStack(ItemRegistry.Crystall, 1, 7),
                    'X', ComponentListMF.peridot_dust, 'Y', ComponentListMF.ruby_dust, 'Z', ComponentListMF.emerald_dust,
                    'A', ComponentListMF.order_shard_dust, 'B', ComponentListMF.air_shard_dust, 'C', ComponentListMF.earth_shard_dust,
                    ' ', null
            );
            ArcanumRecipesManager.getInstance().addRecipe(
                    new ItemStack(ItemRegistry.Crystall, 1, 10),
                    1200,
                    "XYZ",
                    "AIA",
                    "ABA",
                    'I', new ItemStack(ItemRegistry.Crystall, 1, 6),
                    'X', ComponentListMF.ruby_dust, 'Y', ComponentListMF.emerald_dust, 'Z', ComponentListMF.tanzanite_dust,
                    'A', ComponentListMF.entropy_shard_dust, 'B', ComponentListMF.fire_shard_dust,
                    ' ', null
            );
            ArcanumRecipesManager.getInstance().addRecipe(
                    new ItemStack(ItemRegistry.Crystall, 1, 11),
                    1200,
                    "XYZ",
                    "AIB",
                    "ACB",
                    'I', new ItemStack(ItemRegistry.Crystall, 1, 7),
                    'X', ComponentListMF.sapphire_dust, 'Y', ComponentListMF.emerald_dust, 'Z', ComponentListMF.diamond_dust,
                    'A', ComponentListMF.water_shard_dust, 'B', ComponentListMF.air_shard_dust, 'C', ComponentListMF.order_shard_dust,
                    ' ', null
            );
            ArcanumRecipesManager.getInstance().addRecipe(
                    new ItemStack(ItemRegistry.Crystall, 1, 11),
                    1200,
                    "XYZ",
                    "AIB",
                    "ACB",
                    'I', new ItemStack(ItemRegistry.Crystall, 1, 7),
                    'X', ComponentListMF.sapphire_dust, 'Y', ComponentListMF.emerald_dust, 'Z', ComponentListMF.diamond_dust,
                    'A', ComponentListMF.water_shard_dust, 'B', ComponentListMF.air_shard_dust, 'C', ComponentListMF.order_shard_dust,
                    ' ', null
            );
            ArcanumRecipesManager.getInstance().addRecipe(
                    new ItemStack(ItemRegistry.Crystall, 1, 12),
                    1200,
                    "XYZ",
                    "BIB",
                    "BAB",
                    'I', new ItemStack(ItemRegistry.Crystall, 1, 4),
                    'X', ComponentListMF.topaz_dust, 'Y', ComponentListMF.malachite_dust, 'Z', ComponentListMF.peridot_dust,
                    'A', ComponentListMF.entropy_shard_dust, 'B', ComponentListMF.fire_shard_dust,
                    ' ', null
            );
            ArcanumRecipesManager.getInstance().addRecipe(
                    new ItemStack(ItemRegistry.Crystall, 1, 13),
                    1200,
                    "XYZ",
                    "AIA",
                    "CBC",
                    'I', new ItemStack(ItemRegistry.Crystall, 1, 6),
                    'X', ComponentListMF.tanzanite_dust, 'Y', ComponentListMF.peridot_dust, 'Z', ComponentListMF.diamond_dust,
                    'A', ComponentListMF.entropy_shard_dust, 'B', ComponentListMF.fire_shard_dust, 'C', ComponentListMF.order_shard_dust,
                    ' ', null
            );
            ArcanumRecipesManager.getInstance().addRecipe(
                    new ItemStack(ItemRegistry.Crystall, 1, 14),
                    1200,
                    "XYZ",
                    "BIB",
                    "BAB",
                    'I', new ItemStack(ItemRegistry.Crystall, 1, 5),
                    'X', ComponentListMF.peridot_dust, 'Y', ComponentListMF.amber_dust, 'Z', ComponentListMF.topaz_dust,
                    'A', ComponentListMF.air_shard_dust, 'B', ComponentListMF.order_shard_dust,
                    ' ', null
            );
        }
    }
}
