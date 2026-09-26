package ru.givler.mbo.registry;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import ru.givler.mbo.block.special.MBOBlockTrapDoor;
import ru.givler.mbo.block.BlockBasicWoodButton;
import net.minecraft.init.Blocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import cpw.mods.fml.common.registry.GameRegistry;

import java.util.Iterator;

public final class VanillaBlockReplacer {
    public static BlockBasicWoodButton woodenButtonReplacement;

    private VanillaBlockReplacer() {}

    /** Gives wooden variants distinct translation keys from their stone counterparts. */
    public static void separateWoodenLocalizationKeys() {
        Blocks.wooden_button.setBlockName("woodenButton");
        Blocks.wooden_pressure_plate.setBlockName("woodenPressurePlate");
    }

    public static void replaceTrapdoor() {

        String key = "minecraft:trapdoor";

        Block old = (Block) GameData.getBlockRegistry().getObject(key);

        if (old instanceof MBOBlockTrapDoor) {
            return;
        }

        Block trapdoor = new MBOBlockTrapDoor(Material.wood)
                .setHardness(3.0F)
                .setStepSound(Block.soundTypeWood)
                .setBlockName("trapdoor")
                .setBlockTextureName("trapdoor");

        GameData.getBlockRegistry().putObject(key, trapdoor);

        ItemBlock itemBlock = new ItemBlock(trapdoor);
        ((Item) itemBlock).setUnlocalizedName("trapdoor");
        GameData.getItemRegistry().putObject(key, itemBlock);
    }

    /** Replaces the wildcard-plank vanilla recipe with an oak-only recipe. */
    @SuppressWarnings("unchecked")
    public static void restrictTrapdoorRecipeToOak() {
        Block trapdoor = (Block) GameData.getBlockRegistry().getObject("minecraft:trapdoor");
        Item replacementItem = Item.getItemFromBlock(trapdoor);
        Item vanillaItem = Item.getItemFromBlock(Blocks.trapdoor);

        Iterator<IRecipe> iterator = CraftingManager.getInstance().getRecipeList().iterator();
        while (iterator.hasNext()) {
            ItemStack output = iterator.next().getRecipeOutput();
            if (output != null
                    && (output.getItem() == replacementItem || output.getItem() == vanillaItem)) {
                iterator.remove();
            }
        }

        GameRegistry.addRecipe(new ItemStack(trapdoor, 2),
                "PPP", "PPP", 'P', new ItemStack(Blocks.planks, 1, 0));
    }

    public static void replaceWoodenButton() {
        String key = "minecraft:wooden_button";
        Block old = (Block) GameData.getBlockRegistry().getObject(key);
        if (woodenButtonReplacement != null) {
            return;
        }

        Item oldItem = Item.getItemFromBlock(old);
        BlockBasicWoodButton button = new BlockBasicWoodButton(
                "wooden_button", Blocks.planks, 0, false, CreativeTabs.tabRedstone);
        woodenButtonReplacement = button;
        GameData.getBlockRegistry().putObject(key, button);

        ItemBlock itemBlock = new ItemBlock(button);
        ((Item)itemBlock).setUnlocalizedName("wooden_button");
        ((Item)itemBlock).setCreativeTab(CreativeTabs.tabRedstone);
        GameData.getItemRegistry().putObject(key, itemBlock);

        removeRecipesProducing(oldItem);
        GameRegistry.addRecipe(new ItemStack(button), "P",
                'P', new ItemStack(Blocks.planks, 1, 0));
    }

    @SuppressWarnings("unchecked")
    private static void removeRecipesProducing(Item outputItem) {
        Iterator<IRecipe> iterator = CraftingManager.getInstance().getRecipeList().iterator();
        while (iterator.hasNext()) {
            ItemStack output = iterator.next().getRecipeOutput();
            if (output != null && output.getItem() == outputItem) {
                iterator.remove();
            }
        }
    }

}
