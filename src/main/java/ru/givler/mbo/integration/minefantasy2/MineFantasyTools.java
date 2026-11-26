package ru.givler.mbo.integration.minefantasy2;

import cpw.mods.fml.common.Loader;
import minefantasy.mf2.item.tool.crafting.ItemSaw;
import minefantasy.mf2.item.tool.crafting.ItemSpanner;
import net.minecraft.item.ItemStack;

public final class MineFantasyTools {
    private MineFantasyTools() {}

    public static boolean isSaw(ItemStack stack) {
        return stack != null && Loader.isModLoaded("minefantasy2") && Types.isSaw(stack);
    }

    public static boolean isSpanner(ItemStack stack) {
        return stack != null && Loader.isModLoaded("minefantasy2") && Types.isSpanner(stack);
    }

    private static final class Types {
        static boolean isSaw(ItemStack stack) {
            return stack.getItem() instanceof ItemSaw;
        }

        static boolean isSpanner(ItemStack stack) {
            return stack.getItem() instanceof ItemSpanner;
        }
    }
}
