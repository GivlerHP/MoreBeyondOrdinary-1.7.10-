package ru.givler.mbo.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.world.World;

public class ArcanumRecipes {
    private final ItemStack output;
    public final ShapedRecipes shapedRecipe;
    private final int cookTime;

    public ArcanumRecipes(ItemStack output, ShapedRecipes shapedRecipe, int cookTime) {
        this.output = output;
        this.shapedRecipe = shapedRecipe;
        this.cookTime = cookTime;
    }

    public boolean matches(InventoryCrafting inv) {
        return shapedRecipe.matches(inv, null);
    }

    public ItemStack getOutput() {
        return output;
    }

    public int getCookTime() {
        return cookTime;
    }
}
