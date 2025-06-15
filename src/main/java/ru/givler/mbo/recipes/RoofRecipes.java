package ru.givler.mbo.recipes;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.registry.BlockRegistry;

public class RoofRecipes {
    public static void init() {
        //необоженная черепицы
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofUnfired, 2, 0),
                "yyy",
                " x ",
                "yyy",
                'x', Blocks.clay,
                'y', Items.clay_ball
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofUnfired, 2, 1),
                " yy",
                "yxy",
                "yy ",
                'x', Blocks.clay,
                'y', Items.clay_ball
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofUnfired, 2, 2),
                "yyy",
                "yxy",
                " y ",
                'x', Blocks.clay,
                'y', Items.clay_ball
        );
        //стандартная черепица
        GameRegistry.addSmelting(new ItemStack(BlockRegistry.RoofUnfired, 1, 0),
                new ItemStack(BlockRegistry.RoofStandart, 1, 0), 0.2F);
        GameRegistry.addSmelting(new ItemStack(BlockRegistry.RoofUnfired, 1, 1),
                new ItemStack(BlockRegistry.RoofStandart, 1, 1), 0.2F);
        GameRegistry.addSmelting(new ItemStack(BlockRegistry.RoofUnfired, 1, 2),
                new ItemStack(BlockRegistry.RoofStandart, 1, 2), 0.2F);

    }

}
