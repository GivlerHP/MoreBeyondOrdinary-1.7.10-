package ru.givler.mbo.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import java.util.ArrayList;

public class RecipeHelper {

    public static ItemStack[] buildInputArray(Object... input) {
        ArrayList<String> pattern = new ArrayList<>();
        int i = 0;

        // Сначала идёт 1-3 строки шаблона
        while (i < input.length && input[i] instanceof String) {
            pattern.add((String) input[i]);
            i++;
        }

        int width = pattern.get(0).length();
        int height = pattern.size();

        // Массив для хранения результата
        ItemStack[] result = new ItemStack[width * height];

        // Теперь пары char + Object (Item, Block, ItemStack)
        java.util.Map<Character, ItemStack> itemMap = new java.util.HashMap<>();

        while (i < input.length) {
            if (!(input[i] instanceof Character))
                throw new IllegalArgumentException("Expected character at position " + i);
            if (i + 1 >= input.length)
                throw new IllegalArgumentException("Missing object for character at position " + i);

            Character key = (Character) input[i];
            Object value = input[i + 1];

            ItemStack stack;
            if (value instanceof ItemStack) stack = ((ItemStack) value).copy();
            else if (value instanceof net.minecraft.item.Item) stack = new ItemStack((net.minecraft.item.Item) value);
            else if (value instanceof net.minecraft.block.Block) stack = new ItemStack((net.minecraft.block.Block) value);
            else if (value == null) stack = null; // Для пробела
            else throw new IllegalArgumentException("Invalid object for character " + key);

            itemMap.put(key, stack);
            i += 2;
        }

        // Заполняем массив result символами из шаблона
        for (int row = 0; row < height; row++) {
            String line = pattern.get(row);
            if (line.length() != width)
                throw new IllegalArgumentException("Pattern row " + row + " length mismatch");
            for (int col = 0; col < width; col++) {
                char c = line.charAt(col);
                result[row * width + col] = itemMap.get(c);
            }
        }

        return result;
    }
}
