package ru.givler.mbo.recipes;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.registry.BlockRegistry;

public class BlockRecipes {
    public static void init() {
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
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockStonebrick, 4, 3),
                " x ",
                "x x",
                " x ",
                'x', new ItemStack(BlockRegistry.BlockStonebrick, 1, 0)
        );
    }
}
