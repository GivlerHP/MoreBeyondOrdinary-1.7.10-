package ru.givler.mbo.integration.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.recipes.ArcanumRecipes;
import ru.givler.mbo.recipes.ArcanumRecipesManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ArcanumRecipeHandler extends TemplateRecipeHandler {

    public class CachedMagicFurnaceRecipe extends CachedRecipe {
        List<PositionedStack> inputStacks = new ArrayList<>();
        PositionedStack fuelStack;
        PositionedStack outputStack;
        int cookTime;

        public CachedMagicFurnaceRecipe(ArcanumRecipes recipe) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = recipe.shapedRecipe.recipeItems[i];
                if (stack != null) {
                    int x = 36 + (i % 3) * 20;
                    int y = 8 + (i / 3) * 20;
                    inputStacks.add(new PositionedStack(stack, x, y));
                }
            }
            // Не забыть заменить потом на новое топливо
            fuelStack = new PositionedStack(new ItemStack(net.minecraft.init.Items.coal), 132, 61);
            outputStack = new PositionedStack(recipe.getOutput(), 132, 28);
            cookTime = recipe.getCookTime();
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return getCycledIngredients(cycleticks / 20, inputStacks);
        }

        @Override
        public PositionedStack getResult() {
            return outputStack;
        }

        public PositionedStack getFuel() {
            return fuelStack;
        }
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("method.arcanum_crucible");
    }

    @Override
    public String getGuiTexture() {
        return "mbo:textures/gui/gui_infusionworkbench.png";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {

        if ("item".equals(outputId) && results.length > 0 && results[0] instanceof ItemStack) {
            ItemStack output = (ItemStack) results[0];
            for (ArcanumRecipes recipe : ArcanumRecipesManager.getInstance().getRecipes()) {
                ItemStack recipeOutput = recipe.getOutput();
                if (output.getItem() == recipeOutput.getItem() &&
                        output.getItemDamage() == recipeOutput.getItemDamage() &&
                        ItemStack.areItemStackTagsEqual(output, recipeOutput)) {
                    arecipes.add(new CachedMagicFurnaceRecipe(recipe));
                }
            }
        } else if ("magicfurnace".equals(outputId)) {
            for (ArcanumRecipes recipe : ArcanumRecipesManager.getInstance().getRecipes()) {
                arecipes.add(new CachedMagicFurnaceRecipe(recipe));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadUsageRecipes(String inputId, Object... ingredients) {

        if ("item".equals(inputId) && ingredients.length > 0 && ingredients[0] instanceof ItemStack) {
            ItemStack input = (ItemStack) ingredients[0];

            for (ArcanumRecipes recipe : ArcanumRecipesManager.getInstance().getRecipes()) {
                for (ItemStack stack : recipe.shapedRecipe.recipeItems) {
                    if (stack != null &&
                            input.getItem() == stack.getItem() &&
                            input.getItemDamage() == stack.getItemDamage() &&
                            ItemStack.areItemStackTagsEqual(input, stack)) {

                        arecipes.add(new CachedMagicFurnaceRecipe(recipe));
                        break;
                    }
                }
            }
        } else {
            super.loadUsageRecipes(inputId, ingredients);
        }
    }

    @Override
    public int recipiesPerPage() {
        return 1;
    }

    @Override
    public void drawExtras(int recipeIndex) {
        CachedMagicFurnaceRecipe recipe = (CachedMagicFurnaceRecipe) arecipes.get(recipeIndex);

        int guiLeft = (Minecraft.getMinecraft().currentScreen.width - 200) / 2;
        int guiTop = (Minecraft.getMinecraft().currentScreen.height - 220) / 2;

        int x = 25;
        int y = 73;
        int width = (int) ((float) (cycleticks % recipe.cookTime) / recipe.cookTime * 80);

    }


    public List<Rectangle> getGuiExtraAreas(GuiContainer gui) {
        List<Rectangle> list = new ArrayList<>();
        list.add(new Rectangle(25, 73, 80, 16));
        return list;
    }
    private static final int GUI_OFFSET_X = 0;
    private static final int GUI_OFFSET_Y = 0;

    @Override
    public void drawBackground(int recipe) {
        GL11.glColor4f(1, 1, 1, 1);
        GuiDraw.changeTexture(getGuiTexture());
        GuiDraw.drawTexturedModalRect(GUI_OFFSET_X, GUI_OFFSET_Y, 0, 0, 176,  90);
    }

    @Override
    public List<PositionedStack> getIngredientStacks(int recipe) {
        return ((CachedMagicFurnaceRecipe) arecipes.get(recipe)).inputStacks;
    }

    @Override
    public PositionedStack getResultStack(int recipe) {
        return ((CachedMagicFurnaceRecipe) arecipes.get(recipe)).outputStack;
    }

    @Override
    public List<PositionedStack> getOtherStacks(int recipe) {
        List<PositionedStack> list = new ArrayList<>();
        list.add(((CachedMagicFurnaceRecipe) arecipes.get(recipe)).getFuel());
        return list;
    }

    @Override
    public String getOverlayIdentifier() {
        return "magicfurnace";
    }
}
