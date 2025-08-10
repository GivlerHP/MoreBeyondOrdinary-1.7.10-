package ru.givler.mbo.recipes;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import java.util.ArrayList;
import java.util.List;

public class ArcanumRecipesManager {

    private static final ArcanumRecipesManager instance = new ArcanumRecipesManager();
    private final List<ArcanumRecipes> recipes = new ArrayList<>();

    public static ArcanumRecipesManager getInstance() {
        return instance;
    }

    public void addRecipe(ItemStack output, int cookTime, Object... input) {
        ShapedRecipes base = new ShapedRecipes(3, 3, RecipeHelper.buildInputArray(input), output);
        recipes.add(new ArcanumRecipes(output, base, cookTime));
    }

    public ArcanumRecipes getMatchingRecipe(ItemStack[] matrix) {
        InventoryCrafting fakeCrafting = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer player) {
                return false;
            }
        }, 3, 3);

        for (int i = 0; i < matrix.length; i++) {
            fakeCrafting.setInventorySlotContents(i, matrix[i]);
        }

        for (ArcanumRecipes recipe : recipes) {
            if (recipe.matches(fakeCrafting)) {
                return recipe;
            }
        }

        return null;
    }

    public List<ArcanumRecipes> getRecipes() {
        return recipes;
    }
}
