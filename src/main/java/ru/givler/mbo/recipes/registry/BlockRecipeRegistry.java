package ru.givler.mbo.recipes.registry;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.registry.BlockRegistry;

public class BlockRecipeRegistry {
    public static void init() {
        //Серый камень
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockGreyStone, 8),
                "xxx",
                "xyx",
                "xxx",
                'x', Blocks.stone,
                'y', new ItemStack(Items.dye, 1, 8)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockGreyCobblestone, 8),
                "xxx",
                "xyx",
                "xxx",
                'x', Blocks.cobblestone,
                'y', new ItemStack(Items.dye, 1, 8)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockGreyCobblesMossy, 1, 2),
                "xy",
                'x', new ItemStack(BlockRegistry.BlockGreyCobblestone, 1, 0),
                'y', Blocks.vine
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairsStone, 4),
                "x  ",
                "xx ",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockGreyStone, 1)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.SlabStone, 6),
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockGreyStone, 1)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairsGreyCobblestone, 4),
                "x  ",
                "xx ",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockGreyCobblestone, 1)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.SlabCobblestone, 6),
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockGreyCobblestone, 1)
        );

        //Серые кирпичи
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockStonebrick, 4, 0),
                "xx",
                "xx",
                'x', BlockRegistry.BlockGreyStone
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockStonebrick, 1, 1),
                "x",
                'x', new ItemStack(BlockRegistry.BlockStonebrick, 1, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockStonebrick, 1, 2),
                "xy",
                'x', new ItemStack(BlockRegistry.BlockStonebrick, 1, 0),
                'y', Blocks.vine
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.TotemStone, 6),
                "xyx",
                "x x",
                "xyx",
                'x', new ItemStack(Blocks.stonebrick, 1),
                'y', Blocks.stone_slab
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockStonebrick, 4, 3),
                " x ",
                "x x",
                " x ",
                'x', new ItemStack(BlockRegistry.BlockStonebrick, 1, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairsStonebrick, 4),
                "x  ",
                "xx ",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockStonebrick, 1, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.SlabStonebrick, 6),
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockStonebrick, 1, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.WallStonebrick, 6),
                "xxx",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockStonebrick, 1, 0)
        );

        //Обоженные глиняные кирпичи
        GameRegistry.addSmelting(new ItemStack(Blocks.brick_block, 1, 0),
                new ItemStack(BlockRegistry.BlockFiredClay, 1, 0), 0.2F);
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairsFiredClay, 4 ),
                "x  ",
                "xx ",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockFiredClay, 8, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.SlabFiredClay, 6 ),
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockFiredClay, 8, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.WallFiredClay, 6 ),
                "xxx",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockFiredClay, 8, 0)
        );

        //Крепкий песчаник
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockSandstone, 8, 0),
                "xyx",
                "yyy",
                "xyx",
                'x', Blocks.sandstone,
                'y', Blocks.sand
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockSandstone, 4, 1),
                "xx",
                "xx",
                'x', new ItemStack(BlockRegistry.BlockSandstone, 8, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockSandstone, 4, 2),
                " x ",
                "x x",
                " x ",
                'x', new ItemStack(BlockRegistry.BlockSandstone, 8, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairsSandstone, 4 ),
                "x  ",
                "xx ",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockSandstone, 8, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.SlabSandstone, 6 ),
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockSandstone, 8, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.WallSandstone, 6 ),
                "xxx",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockSandstone, 8, 0)
        );
        //МФ кирпичи
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairsImperialBrick, 4 ),
                "x  ",
                "xx ",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockImperialBrick, 8, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.SlabImperialBrick, 6 ),
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockImperialBrick, 8, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairsIrgadBrick, 4 ),
                "x  ",
                "xx ",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockIrgadBrick, 8, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.SlabIrgadBrick, 6 ),
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockIrgadBrick, 8, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairsHeneizenBrick, 4 ),
                "x  ",
                "xx ",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockHeneizenBrick, 8, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.SlabHeneizenBrick, 6 ),
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockHeneizenBrick, 8, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairsEndbrick, 4 ),
                "x  ",
                "xx ",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockEndbrick, 8, 0)
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.SlabEndbrick, 6 ),
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockEndbrick, 8, 0)
        );
    }
}
