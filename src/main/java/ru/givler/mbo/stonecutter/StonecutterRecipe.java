package ru.givler.mbo.stonecutter;

import net.minecraft.item.ItemStack;

public final class StonecutterRecipe {
    private final ItemStack input;
    private final ItemStack output;

    StonecutterRecipe(ItemStack input, ItemStack output) {
        this.input = input.copy();
        this.output = output.copy();
    }

    public ItemStack getInput() { return input.copy(); }
    public ItemStack getOutput() { return output.copy(); }

    public boolean matches(ItemStack stack) {
        return stack != null && stack.getItem() == input.getItem()
                && (input.getItemDamage() == 32767 || input.getItemDamage() == stack.getItemDamage());
    }
}
