package ru.givler.mbo.integration.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.recipes.stonecutter.StonecutterRecipe;
import ru.givler.mbo.recipes.stonecutter.StonecutterRecipes;

public final class StonecutterRecipeHandler extends TemplateRecipeHandler {
  public final class CachedStonecutterRecipe extends CachedRecipe {
    private final PositionedStack input;
    private final PositionedStack selectedResult;
    private final List<PositionedStack> outputs = new ArrayList<PositionedStack>();

    CachedStonecutterRecipe(ItemStack ingredient, List<ItemStack> results) {
      input = new PositionedStack(ingredient, 14, 33);
      selectedResult = null;
      for (int i = 0; i < results.size(); i++)
        outputs.add(new PositionedStack(results.get(i), 46 + (i % 4) * 20, 16 + (i / 4) * 20));
    }

    CachedStonecutterRecipe(List<ItemStack> ingredients, ItemStack selected) {
      input = null;
      selectedResult = new PositionedStack(selected, 137, 33);
      for (int i = 0; i < ingredients.size(); i++)
        outputs.add(new PositionedStack(ingredients.get(i), 46 + (i % 4) * 20, 16 + (i / 4) * 20));
    }

    @Override
    public PositionedStack getIngredient() {
      return input;
    }

    @Override
    public PositionedStack getResult() {
      return selectedResult != null ? selectedResult : (outputs.isEmpty() ? null : outputs.get(0));
    }

    @Override
    public List<PositionedStack> getOtherStacks() {
      return selectedResult != null
          ? outputs
          : (outputs.size() < 2
              ? java.util.Collections.<PositionedStack>emptyList()
              : outputs.subList(1, outputs.size()));
    }
  }

  @Override
  public String getRecipeName() {
    return StatCollector.translateToLocal("container.mbo.stonecutter");
  }

  @Override
  public String getGuiTexture() {
    return "mbo:textures/gui/stonecutterNEI.png";
  }

  @Override
  public String getOverlayIdentifier() {
    return "mbo.stonecutter";
  }

  @Override
  public int recipiesPerPage() {
    return 1;
  }

  @Override
  public void loadCraftingRecipes(String outputId, Object... results) {
    if (getOverlayIdentifier().equals(outputId)) {
      addAllGroups();
      return;
    }
    if ("item".equals(outputId) && results.length > 0 && results[0] instanceof ItemStack) {
      ItemStack wanted = (ItemStack) results[0];
      List<ItemStack> ingredients = new ArrayList<ItemStack>();
      ItemStack result = null;
      for (Group group : groups())
        for (ItemStack output : group.outputs)
          if (same(output, wanted)) {
            if (!containsOutput(ingredients, group.input)) ingredients.add(group.input);
            if (result == null) result = output;
            break;
          }
      if (result != null) arecipes.add(new CachedStonecutterRecipe(ingredients, result));
      return;
    }
    super.loadCraftingRecipes(outputId, results);
  }

  @Override
  public void loadUsageRecipes(String inputId, Object... ingredients) {
    if ("item".equals(inputId) && ingredients.length > 0 && ingredients[0] instanceof ItemStack) {
      ItemStack wanted = (ItemStack) ingredients[0];
      for (Group group : groups())
        if (matchesInput(group.input, wanted))
          arecipes.add(new CachedStonecutterRecipe(group.input, group.outputs));
      return;
    }
    super.loadUsageRecipes(inputId, ingredients);
  }

  private void addAllGroups() {
    for (Group group : groups())
      arecipes.add(new CachedStonecutterRecipe(group.input, group.outputs));
  }

  private static List<Group> groups() {
    List<Group> groups = new ArrayList<Group>();
    for (StonecutterRecipe recipe : StonecutterRecipes.getAllRecipes()) {
      ItemStack input = recipe.getInput();
      Group found = null;
      for (Group group : groups)
        if (sameIngredient(group.input, input)) {
          found = group;
          break;
        }
      if (found == null) {
        found = new Group(input);
        groups.add(found);
      }
      ItemStack output = recipe.getOutput();
      if (!containsOutput(found.outputs, output)) found.outputs.add(output);
    }
    return groups;
  }

  @Override
  public void drawBackground(int recipe) {
    GL11.glColor4f(1F, 1F, 1F, 1F);
    GuiDraw.changeTexture(getGuiTexture());
    GuiDraw.drawTexturedModalRect(-6, 0, 0, 0, 176, 83);
  }

  private static boolean containsOutput(List<ItemStack> outputs, ItemStack wanted) {
    for (ItemStack output : outputs) if (same(output, wanted)) return true;
    return false;
  }

  private static boolean same(ItemStack a, ItemStack b) {
    return a != null
        && b != null
        && a.getItem() == b.getItem()
        && a.getItemDamage() == b.getItemDamage()
        && ItemStack.areItemStackTagsEqual(a, b);
  }

  private static boolean sameIngredient(ItemStack a, ItemStack b) {
    return a != null
        && b != null
        && a.getItem() == b.getItem()
        && a.getItemDamage() == b.getItemDamage()
        && ItemStack.areItemStackTagsEqual(a, b);
  }

  private static boolean matchesInput(ItemStack recipe, ItemStack wanted) {
    return recipe != null
        && wanted != null
        && recipe.getItem() == wanted.getItem()
        && (recipe.getItemDamage() == 32767 || recipe.getItemDamage() == wanted.getItemDamage())
        && ItemStack.areItemStackTagsEqual(recipe, wanted);
  }

  private static final class Group {
    final ItemStack input;
    final List<ItemStack> outputs = new ArrayList<ItemStack>();

    Group(ItemStack input) {
      this.input = input.copy();
    }
  }
}
