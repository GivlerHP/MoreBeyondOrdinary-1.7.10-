package ru.givler.mbo.recipes.registry;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.registry.BlockRegistry;

public class RoofRecipeRegistry {
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
        //Пластинчитая черепица
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 0),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 1),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 4)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 2),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 3)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 3),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 12)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 4),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 8)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 5),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 2)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 6),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 6)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 7),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 10)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 8),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 13)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 9),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 11)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 10),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 9)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 11),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 5)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 12),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 1)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 13),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 7)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 14),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 15)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofSheet, 8, 15),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 1),
                'y', new ItemStack(Items.dye, 1, 14)
        );
        //Листовая черепица
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 0),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 1),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 4)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 2),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 3)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 3),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 12)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 4),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 8)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 5),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 2)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 6),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 6)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 7),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 10)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 8),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 13)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 9),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 11)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 10),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 9)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 11),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 5)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 12),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 1)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 13),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 7)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 14),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 15)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofLaminated, 8, 15),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 0),
                'y', new ItemStack(Items.dye, 1, 14)
        );
        //чешуйчитая черепицы
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 0),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 1),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 4)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 2),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 3)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 3),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 12)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 4),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 8)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 5),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 2)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 6),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 6)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 7),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 10)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 8),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 13)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 9),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 11)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 10),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 9)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 11),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 5)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 12),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 1)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 13),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 7)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 14),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 15)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.RoofFlake, 8, 15),
                "xxx",
                "xyx",
                "xxx",
                'x', new ItemStack(BlockRegistry.RoofStandart, 1, 2),
                'y', new ItemStack(Items.dye, 1, 14)
        );
        //ступеньки из черепицы
        for (int meta = 0; meta < 16; meta++) {
            GameRegistry.addRecipe(
                    new ItemStack(BlockRegistry.StairsRoofLaminated[meta], 4),
                    "x  ",
                    "xx ",
                    "xxx",
                    'x', new ItemStack(BlockRegistry.RoofLaminated, 1, meta)
            );
        }
        for (int meta = 0; meta < 16; meta++) {
            GameRegistry.addRecipe(
                    new ItemStack(BlockRegistry.StairsRoofSheet[meta], 4),
                    "x  ",
                    "xx ",
                    "xxx",
                    'x', new ItemStack(BlockRegistry.RoofSheet, 1, meta)
            );
        }
        for (int meta = 0; meta < 16; meta++) {
            GameRegistry.addRecipe(
                    new ItemStack(BlockRegistry.StairsRoofFlake[meta], 4),
                    "x  ",
                    "xx ",
                    "xxx",
                    'x', new ItemStack(BlockRegistry.RoofFlake, 1, meta)
            );
        }
        for (int meta = 0; meta < 3; meta++) {
            GameRegistry.addRecipe(
                    new ItemStack(BlockRegistry.StairsRoofStandart[meta], 4),
                    "x  ",
                    "xx ",
                    "xxx",
                    'x', new ItemStack(BlockRegistry.RoofStandart, 1, meta)
            );
        }
        for (int meta = 0; meta < 6; meta++) {
            GameRegistry.addRecipe(
                    new ItemStack(BlockRegistry.StairsRoofWood[meta], 4),
                    "x  ",
                    "xx ",
                    "xxx",
                    'x', new ItemStack(BlockRegistry.RoofWood, 1, meta)
            );
        }
        //плиты из черепицы
        for (int meta = 0; meta < 16; meta++) {
            GameRegistry.addRecipe(
                    new ItemStack(BlockRegistry.SlabRoofLaminated[meta], 6),
                    "xxx",
                    'x', new ItemStack(BlockRegistry.RoofLaminated, 1, meta)
            );
        }
        for (int meta = 0; meta < 16; meta++) {
            GameRegistry.addRecipe(
                    new ItemStack(BlockRegistry.SlabRoofSheet[meta], 6),
                    "xxx",
                    'x', new ItemStack(BlockRegistry.RoofSheet, 1, meta)
            );
        }
        for (int meta = 0; meta < 16; meta++) {
            GameRegistry.addRecipe(
                    new ItemStack(BlockRegistry.SlabRoofFlake[meta], 6),
                    "xxx",
                    'x', new ItemStack(BlockRegistry.RoofFlake, 1, meta)
            );
        }
        for (int meta = 0; meta < 3; meta++) {
            GameRegistry.addRecipe(
                    new ItemStack(BlockRegistry.SlabRoofStandart[meta], 6),
                    "xxx",
                    'x', new ItemStack(BlockRegistry.RoofStandart, 1, meta)
            );
        }
        for (int meta = 0; meta < 6; meta++) {
            GameRegistry.addRecipe(
                    new ItemStack(BlockRegistry.SlabRoofWood[meta], 6),
                    "xxx",
                    'x', new ItemStack(BlockRegistry.RoofWood, 1, meta)
            );
        }
    }

}
