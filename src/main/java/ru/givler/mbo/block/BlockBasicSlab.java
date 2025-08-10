package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemSlab;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;


//класс для создание полублоков
public class BlockBasicSlab extends BlockSlab {
    private final boolean isDouble;
    private static BlockBasicSlab singleSlab;
    private static BlockBasicSlab doubleSlab;

    public BlockBasicSlab(boolean isDouble, String name, String  texture) {
        super(isDouble, Material.rock);
        this.isDouble = isDouble;
        this.setBlockName(name + "_slab");
        this.setHardness(1.5F);
        this.setResistance(10.0F);
        this.setStepSound(soundTypeStone);
        this.setHarvestLevel("pick_axe", 0);
        this.setBlockTextureName(MoreBeyondOrdinary.MODID + ":" + texture); // Задаём текстуру блока
        this.useNeighborBrightness = true; // Улучшенная обработка освещения

        if (!isDouble) {
            this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);   //только одиночная плита добавляется в креативную панель
        }
        GameRegistry.registerBlock(this, name);
    }

    @Override
    public String func_150002_b(int meta) {
        return this.getUnlocalizedName();
    }

    public static void registerSlabs(String baseName, String texture) {
        singleSlab = new BlockBasicSlab(false, baseName + "_slab", texture);  //одиночная плита
        doubleSlab = new BlockBasicSlab(true, baseName + "_double_slab", texture);  //двойная плита (если поставить полублок на полублок)

        GameRegistry.registerItem(new ItemSlab(singleSlab, singleSlab, doubleSlab, false), baseName + "_slab");
    }
}