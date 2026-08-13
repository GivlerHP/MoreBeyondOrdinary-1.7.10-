package ru.givler.mbo.recipes.registry;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.block.BlockMeta;
import ru.givler.mbo.block.BlockMetaSlab;
import ru.givler.mbo.registry.BlockRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class BlockRecipeRegistry {
    public static void init() {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BlockRegistry.Barrel),
                "PSP", "P P", "PSP", 'P', "plankWood", 'S', "slabWood"));
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
        //Обоженные глиняные кирпичи
        GameRegistry.addSmelting(new ItemStack(Blocks.brick_block, 1, 0),
                new ItemStack(BlockRegistry.BlockFiredClay, 1, 0), 0.2F);
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
        //МФ кирпичи
        // Туф
        BlockMetaSlab.addStandardRecipes(
                BlockRegistry.SlabTuff, (BlockMeta) BlockRegistry.BlockTuff);

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

    }
}
