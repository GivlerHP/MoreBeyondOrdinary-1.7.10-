package ru.givler.mbo.recipes;

import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.givler.mbo.item.weapon.ItemEffectArrow;
import ru.givler.mbo.registry.ItemRegistry;

/** 1.7.10 adaptation: eight arrows around an existing potion, without new potion effects. */
public final class RecipeEffectArrows implements IRecipe {
  @Override
  public boolean matches(InventoryCrafting grid, World world) {
    return getCraftingResult(grid) != null;
  }

  @Override
  public ItemStack getCraftingResult(InventoryCrafting grid) {
    if (grid.getSizeInventory() != 9) return null;
    ItemStack potion = grid.getStackInSlot(4);
    if (potion == null || !(potion.getItem() instanceof ItemPotion)) return null;
    for (int slot = 0; slot < 9; slot++) {
      if (slot == 4) continue;
      ItemStack arrow = grid.getStackInSlot(slot);
      if (arrow == null || arrow.getItem() != Items.arrow) return null;
    }
    List<PotionEffect> effects = ((ItemPotion) potion.getItem()).getEffects(potion);
    if (effects == null || effects.isEmpty()) return null;
    return ItemEffectArrow.custom(ItemRegistry.effectArrow, effects);
  }

  @Override
  public int getRecipeSize() {
    return 9;
  }

  @Override
  public ItemStack getRecipeOutput() {
    return new ItemStack(ItemRegistry.effectArrow, 8, 19);
  }
}
