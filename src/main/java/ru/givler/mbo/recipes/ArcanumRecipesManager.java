package ru.givler.mbo.recipes;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArcanumRecipesManager {

    private static final ArcanumRecipesManager instance = new ArcanumRecipesManager();
    private final List<ArcanumRecipes> recipes = new ArrayList<>();
    private final InventoryCrafting matchingInventory = new InventoryCrafting(new MatchingContainer(), 3, 3);

    private static final class MatchingContainer extends Container {
        @Override public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer player) {
            return false;
        }
    }

    public static ArcanumRecipesManager getInstance() {
        return instance;
    }

    public void addRecipe(ItemStack output, int cookTime, Object... input) {
        if (output == null) throw new IllegalArgumentException("Arcanum output cannot be null");
        ItemStack result = output.copy();
        ShapedRecipes base = new ShapedRecipes(3, 3, RecipeHelper.buildInputArray(input), result);
        recipes.add(new ArcanumRecipes(result, base, Math.max(1, cookTime)));
    }

    public synchronized ArcanumRecipes getMatchingRecipe(ItemStack[] matrix) {
        for (int i = 0; i < 9; i++) {
            matchingInventory.setInventorySlotContents(i, i < matrix.length ? matrix[i] : null);
        }

        for (ArcanumRecipes recipe : recipes) {
            if (recipe.matches(matchingInventory)) {
                return recipe;
            }
        }

        return null;
    }

    public List<ArcanumRecipes> getRecipes() {
        return Collections.unmodifiableList(recipes);
    }
}
