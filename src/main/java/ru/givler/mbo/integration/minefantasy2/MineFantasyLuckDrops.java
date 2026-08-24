package ru.givler.mbo.integration.minefantasy2;

import minefantasy.mf2.block.list.BlockListMF;
import minefantasy.mf2.item.list.ComponentListMF;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Loaded reflectively only when MineFantasy 2 is installed. */
public final class MineFantasyLuckDrops {
    private MineFantasyLuckDrops() {}

    public static ItemStack getBonus(Block block, int amount) {
        if (block == BlockListMF.oreBorax) return new ItemStack(ComponentListMF.flux_strong, amount);
        if (block == BlockListMF.oreClay) return new ItemStack(Items.clay_ball, amount);
        if (block == BlockListMF.oreCoalRich) return new ItemStack(Items.coal, 1 + amount);
        if (block == BlockListMF.oreKaolinite) return new ItemStack(ComponentListMF.kaolinite, amount);
        if (block == BlockListMF.oreNitre) return new ItemStack(ComponentListMF.nitre, amount);
        if (block == BlockListMF.oreSulfur) return new ItemStack(ComponentListMF.sulfur, amount);
        if (block == BlockListMF.oreInferno) return new ItemStack(ComponentListMF.inferno_crystal, amount);
        if (block == BlockListMF.oreVoid) return new ItemStack(ComponentListMF.void_crystal, amount);
        if (block == BlockListMF.oreTear) return new ItemStack(ComponentListMF.tear_crystal, amount);
        return null;
    }
}
