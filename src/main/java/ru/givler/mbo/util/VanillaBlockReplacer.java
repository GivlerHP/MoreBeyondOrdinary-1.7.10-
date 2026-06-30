package ru.givler.mbo.util;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import ru.givler.mbo.block.specialblocks.MBOBlockTrapDoor;

public final class VanillaBlockReplacer {

    private VanillaBlockReplacer() {}

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
}
