package ru.givler.mbo.stonecutter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;

/** Public registration point for MBO and addon stonecutter recipes. */
public final class StonecutterRecipes {
    private static final List<StonecutterRecipe> RECIPES = new ArrayList<StonecutterRecipe>();
    private StonecutterRecipes() {}

    public static void add(ItemStack input, ItemStack output) {
        if (input == null || output == null) throw new IllegalArgumentException("Stonecutter stacks cannot be null");
        RECIPES.add(new StonecutterRecipe(input, output));
    }

    public static List<StonecutterRecipe> getRecipes(ItemStack input) {
        if (input == null) return Collections.emptyList();
        List<StonecutterRecipe> result = new ArrayList<StonecutterRecipe>();
        for (StonecutterRecipe recipe : RECIPES) if (recipe.matches(input)) result.add(recipe);
        return result;
    }
}
