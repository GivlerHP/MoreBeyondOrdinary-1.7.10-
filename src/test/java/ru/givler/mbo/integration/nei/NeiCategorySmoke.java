package ru.givler.mbo.integration.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.ICraftingHandler;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.recipes.ArcanumRecipesManager;
import ru.givler.mbo.recipes.stonecutter.StonecutterRecipes;

/** Exercises the actual GTNH category dispatch and checks that every variant fits. */
public final class NeiCategorySmoke {
    public static void main(String[] args) throws Exception {
        java.lang.reflect.Field uf = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        uf.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) uf.get(null);
        java.lang.reflect.Field loader = cpw.mods.fml.common.Loader.class.getDeclaredField("instance");
        loader.setAccessible(true);
        loader.set(null, unsafe.allocateInstance(cpw.mods.fml.common.Loader.class));
        java.lang.reflect.Field mods = cpw.mods.fml.common.Loader.class.getDeclaredField("namedMods");
        mods.setAccessible(true);
        mods.set(loader.get(null), new java.util.HashMap<String, cpw.mods.fml.common.ModContainer>());
        net.minecraft.init.Bootstrap.func_151354_b();
        codechicken.nei.recipe.FurnaceRecipeHandler.afuels = new java.util.ArrayList<>();
        codechicken.nei.recipe.FurnaceRecipeHandler.afuels.add(null);
        Item input = new Item(), output = new Item();
        for (int i = 0; i < 25; i++)
            StonecutterRecipes.add(new ItemStack(input), new ItemStack(output, 1, i));
        StonecutterRecipeHandler template = new StonecutterRecipeHandler();
        ICraftingHandler all = template.getAllRecipeHandler();
        check(all.numRecipes() == 3, "25 outputs must occupy three pages");
        Set<Integer> seen = new HashSet<Integer>();
        for (int i = 0; i < all.numRecipes(); i++) {
            check(all.getIngredientStacks(i).size() == 1, "Input missing");
            seen.add(all.getResultStack(i).item.getItemDamage());
            for (PositionedStack stack : all.getOtherStacks(i)) {
                check(stack.rely + 16 <= 83, "Stonecutter output outside background");
                seen.add(stack.item.getItemDamage());
            }
        }
        check(seen.size() == 25, "Category browsing lost stonecutter outputs");

        Item reverseOutput = new Item();
        for (int i = 0; i < 13; i++)
            StonecutterRecipes.add(new ItemStack(input, 1, i + 30), new ItemStack(reverseOutput));
        StonecutterRecipeHandler reverse = new StonecutterRecipeHandler();
        reverse.loadCraftingRecipes("item", new ItemStack(reverseOutput));
        check(reverse.numRecipes() == 2, "13 inputs must occupy two pages");
        int inputs = 0;
        for (int i = 0; i < reverse.numRecipes(); i++) {
            inputs += reverse.getIngredientStacks(i).size();
            check(reverse.getOtherStacks(i).isEmpty(), "Inputs must be ingredients, not byproducts");
        }
        check(inputs == 13, "Reverse lookup lost ingredients");

        ArcanumRecipesManager.getInstance().addRecipe(new ItemStack(output), 20,
                "A", 'A', new ItemStack(input));
        ArcanumRecipeHandler arcanum = new ArcanumRecipeHandler();
        ICraftingHandler magic = arcanum.getAllRecipeHandler();
        check(magic.numRecipes() == 1, "Magic crucible category empty");
        check(magic.getIngredientStacks(0).size() == 1, "Magic crucible input missing");
        check(template.numRecipes() == 0 && arcanum.numRecipes() == 0, "Template was mutated");
        System.out.println("PASS: GTNH category dispatch, stonecutter pagination and reverse inputs, magic crucible recipes");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
