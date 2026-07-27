package ru.givler.mbo.registry;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.entity.boat.EntityMBOBoat;
import ru.givler.mbo.entity.boat.EntityMBOChestBoat;
import ru.givler.mbo.entity.boat.EntityMBOBoatSeat;
import ru.givler.mbo.item.ItemMBOBoat;

import java.util.Iterator;

public final class BoatRegistry {
    public static ItemMBOBoat vanillaBoats, vanillaChestBoats, bopBoats, bopChestBoats;

    private BoatRegistry() { }

    public static void init() {
        vanillaBoats = register("VanillaBoats", false, 0, 6, CreativeTabRegistry.tabMBOitems);
        vanillaChestBoats = register("VanillaChestBoats", true, 0, 6, CreativeTabRegistry.tabMBOitems);
        if (Loader.isModLoaded("BiomesOPlenty")) {
            CreativeTabs tab = biomesoplenty.BiomesOPlenty.tabBiomesOPlenty;
            bopBoats = register("BoPBoats", false, 6, 15, tab);
            bopChestBoats = register("BoPChestBoats", true, 6, 15, tab);
        }
        EntityRegistry.registerModEntity(EntityMBOBoat.class, "MBOBoat", ModEntityIds.next(),
                MoreBeyondOrdinary.instance, 80, 1, true);
        EntityRegistry.registerModEntity(EntityMBOChestBoat.class, "MBOChestBoat", ModEntityIds.next(),
                MoreBeyondOrdinary.instance, 80, 1, true);
        EntityRegistry.registerModEntity(EntityMBOBoatSeat.class, "MBOBoatSeat", ModEntityIds.next(),
                MoreBeyondOrdinary.instance, 80, 1, false);
    }

    private static ItemMBOBoat register(String name, boolean chest, int first, int count, CreativeTabs tab) {
        ItemMBOBoat item = new ItemMBOBoat(name, chest, first, count, tab);
        GameRegistry.registerItem(item, name);
        return item;
    }

    /**
     * Вызывается в init: к этому моменту ванильный список рецептов уже заполнен.
     */
    @SuppressWarnings("unchecked")
    public static void registerRecipes() {
        removeVanillaBoatRecipe();

        for (int meta = 0; meta < 6; ++meta) {
            addBoatRecipes(vanillaBoats, vanillaChestBoats, meta, Blocks.planks, meta);
        }

        if (bopBoats != null && bopChestBoats != null) {
            Block bopPlanks = biomesoplenty.api.content.BOPCBlocks.planks;
            for (int meta = 0; meta < 15; ++meta) {
                addBoatRecipes(bopBoats, bopChestBoats, meta, bopPlanks, meta);
            }
        }
    }

    private static void addBoatRecipes(ItemMBOBoat boats, ItemMBOBoat chestBoats,
                                       int outputMeta, Block planks, int plankMeta) {
        ItemStack plank = new ItemStack(planks, 1, plankMeta);
        GameRegistry.addRecipe(new ItemStack(boats, 1, outputMeta),
                "P P",
                "PPP",
                'P', plank);
        GameRegistry.addShapelessRecipe(new ItemStack(chestBoats, 1, outputMeta),
                new ItemStack(boats, 1, outputMeta), new ItemStack(Blocks.chest));
    }

    @SuppressWarnings("unchecked")
    private static void removeVanillaBoatRecipe() {
        Iterator<IRecipe> iterator = CraftingManager.getInstance().getRecipeList().iterator();
        while (iterator.hasNext()) {
            IRecipe recipe = iterator.next();
            ItemStack output = recipe.getRecipeOutput();
            if (output != null && output.getItem() == Items.boat) {
                iterator.remove();
            }
        }
    }
}
