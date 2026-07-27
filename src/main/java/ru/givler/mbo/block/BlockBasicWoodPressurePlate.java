package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class BlockBasicWoodPressurePlate extends BlockPressurePlate {
    private final Block planks;
    private final int plankMeta;

    public BlockBasicWoodPressurePlate(String name, Block planks, int plankMeta) {
        super(name, Material.wood, Sensitivity.everything);
        this.planks = planks;
        this.plankMeta = plankMeta;
        setBlockName(name);
        setHardness(0.5F);
        setStepSound(Block.soundTypeWood);
        setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        GameRegistry.registerBlock(this, name);
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return planks.getIcon(side, plankMeta);
    }

    public void addStandardRecipe() {
        GameRegistry.addRecipe(new ItemStack(this), "PP",
                'P', new ItemStack(planks, 1, plankMeta));
    }
}
