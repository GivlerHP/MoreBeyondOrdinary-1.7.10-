package ru.givler.mbo.integration.minefantasy2.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import minefantasy.mf2.api.crafting.anvil.IAnvilRecipe;
import minefantasy.mf2.api.crafting.carpenter.ICarpenterRecipe;
import minefantasy.mf2.api.crafting.kitchen.IKitchenRecipe;
import minefantasy.mf2.api.knowledge.client.EntryPageBlastFurnace;
import minefantasy.mf2.api.knowledge.client.EntryPageCraft;
import minefantasy.mf2.api.knowledge.client.EntryPageCrucible;
import minefantasy.mf2.api.knowledge.client.EntryPageGrind;
import minefantasy.mf2.api.knowledge.client.EntryPageImage;
import minefantasy.mf2.api.knowledge.client.EntryPageRecipeAnvil;
import minefantasy.mf2.api.knowledge.client.EntryPageRecipeBloom;
import minefantasy.mf2.api.knowledge.client.EntryPageRecipeCarpenter;
import minefantasy.mf2.api.knowledge.client.EntryPageRecipeKitchen;
import minefantasy.mf2.api.knowledge.client.EntryPageSmelting;
import minefantasy.mf2.api.knowledge.client.EntryPageText;
import minefantasy.mf2.api.refine.Alloy;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import ru.givler.mbo.integration.minefantasy2.knowledge.KnowledgeListMBO;

@SideOnly(Side.CLIENT)
public final class KnowledgePageRegistryMBO {
    private KnowledgePageRegistryMBO() {}

    public static void registerPages() {
        KnowledgeListMBO.buildingFittings.addPages(
                assembleSimpleImgPage("trapdoor_latch",
                        "knowledge.MBOBuildingFittings.trapdoor_latch"),
                assembleSimpleImgPage("fence_connections",
                        "knowledge.MBOBuildingFittings.fence_connections"),
                assembleSimpleImgPage("vertical_slab_connections",
                        "knowledge.MBOBuildingFittings.vertical_slab_connections"));
    }

    private static EntryPageImage assembleSimpleImgPage(String name, String text) {
        return new EntryPageImage("textures/gui/knowledge/mbo/" + name + ".png", 96, 96, text);
    }

    private static EntryPageText assembleTextPage(String text, Object... arguments) {
        return new EntryPageText(text, arguments);
    }

    private static EntryPageCraft assembleCraftingPage(IRecipe... recipes) {
        return new EntryPageCraft(recipes);
    }

    private static EntryPageRecipeAnvil assembleAnvilPage(IAnvilRecipe... recipes) {
        return new EntryPageRecipeAnvil(recipes);
    }

    private static EntryPageRecipeCarpenter assembleCarpenterPage(ICarpenterRecipe... recipes) {
        return new EntryPageRecipeCarpenter(recipes);
    }

    private static EntryPageRecipeKitchen assembleKitchenPage(IKitchenRecipe... recipes) {
        return new EntryPageRecipeKitchen(recipes);
    }

    private static EntryPageSmelting assembleSmeltingPage(ItemStack input, ItemStack output) {
        return new EntryPageSmelting(input, output);
    }

    private static EntryPageRecipeBloom assembleBloomeryPage(ItemStack input, ItemStack output) {
        return new EntryPageRecipeBloom(input, output);
    }

    private static EntryPageBlastFurnace assembleBlastFurnacePage(ItemStack input, ItemStack output) {
        return new EntryPageBlastFurnace(input, output);
    }

    private static EntryPageCrucible assembleCruciblePage(Alloy... recipes) {
        return new EntryPageCrucible(recipes);
    }

    private static EntryPageGrind assembleGrindingPage(ItemStack input, ItemStack output, int tier) {
        return new EntryPageGrind(input, output, tier);
    }
}
