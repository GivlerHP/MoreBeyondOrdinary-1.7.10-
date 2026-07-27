package ru.givler.mbo.recipes.registry;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.block.BlockMeta;
import ru.givler.mbo.block.BlockMetaSlab;
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

        GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairsGreyCobblestone, 4),
                "x  ",
                "xx ",
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

        GameRegistry.addRecipe(new ItemStack(BlockRegistry.WallStonebrick, 6),
                "xxx",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockStonebrick, 1, 0)
        );

        GameRegistry.addRecipe(new ItemStack(BlockRegistry.WallVanillaStonebrick, 6),
                "xxx",
                "xxx",
                'x', Blocks.stonebrick
        );

        GameRegistry.addRecipe(new ItemStack(BlockRegistry.WallVanillaBrick, 6),
                "xxx",
                "xxx",
                'x', Blocks.brick_block
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
   ;
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

        GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairsIrgadBrick, 4 ),
                "x  ",
                "xx ",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockIrgadBrick, 8, 0)
        );

        GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairsHeneizenBrick, 4 ),
                "x  ",
                "xx ",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockHeneizenBrick, 8, 0)
        );

        GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairsEndbrick, 4 ),
                "x  ",
                "xx ",
                "xxx",
                'x', new ItemStack(BlockRegistry.BlockEndbrick, 8, 0)
        );

        // Туф
        BlockMetaSlab.addStandardRecipes(
                BlockRegistry.SlabTuff, (BlockMeta) BlockRegistry.BlockTuff);

        for (int meta = 0; meta < 3; meta++) {
            GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairsTuff[meta], 4),
                    "x  ",
                    "xx ",
                    "xxx",
                    'x', new ItemStack(BlockRegistry.BlockTuff, 1, meta)
            );
            GameRegistry.addRecipe(new ItemStack(BlockRegistry.WallTuff[meta], 6),
                    "xxx",
                    "xxx",
                    'x', new ItemStack(BlockRegistry.BlockTuff, 1, meta)
            );
        }

        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockChiseledTuff),
                "x",
                "x",
                'x', new ItemStack(BlockRegistry.SlabTuff[0])
        );
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockChiseledTuffBricks),
                "x",
                "x",
                'x', new ItemStack(BlockRegistry.SlabTuff[2])
        );

        // Призмарин
        BlockMetaSlab.addStandardRecipes(
                BlockRegistry.SlabPrismarine, (BlockMeta) BlockRegistry.BlockPrismarine);

        for (int meta = 0; meta < 3; meta++) {
            GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairsPrismarine[meta], 4),
                    "x  ",
                    "xx ",
                    "xxx",
                    'x', new ItemStack(BlockRegistry.BlockPrismarine, 1, meta)
            );
            GameRegistry.addRecipe(new ItemStack(BlockRegistry.WallPrismarine[meta], 6),
                    "xxx",
                    "xxx",
                    'x', new ItemStack(BlockRegistry.BlockPrismarine, 1, meta)
            );
        }

    }
}
