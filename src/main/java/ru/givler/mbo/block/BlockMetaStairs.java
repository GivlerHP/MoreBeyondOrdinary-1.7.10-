package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockStairs;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.ItemBlockMetadata;

import java.util.List;




// Класс создающий ступеньки из метаблоков (BlockMeta)
public class BlockMetaStairs extends BlockStairs {
    private final BlockMeta baseBlock;
    private final int meta;


    public BlockMetaStairs(BlockMeta baseBlock, int meta) {
        super(baseBlock, meta); // Используем текстуру базового блока с указанным метаданным
        this.baseBlock = baseBlock;
        this.meta = meta;


        this.setBlockName(baseBlock.getUnlocalizedName() + "_stairs_" + meta ); // Уникальное имя
        this.setCreativeTab(baseBlock.getCreativeTabToDisplayOn()); // Креативная вкладка
        this.setHardness(baseBlock.getBlockHardness(null, 0, 0, 0)); // Твёрдость
        this.setResistance(baseBlock.getExplosionResistance(null)); // Сопротивление взрывам
        this.setStepSound(baseBlock.stepSound); // Звук шага
        this.setHarvestLevel("pick_axe", 0); // Инструмент для добычи
        this.setLightLevel(baseBlock.getLightValue()); // Уровень освещения
        this.setLightOpacity(baseBlock.getLightOpacity()); // Прозрачность
        this.useNeighborBrightness = true; // Улучшенная обработка освещения

        GameRegistry.registerBlock(this, ItemBlockMetadata.class, baseBlock.getUnlocalizedName() + "_stairs_" + meta);
    }

    // Указываем, какой ItemStack выпадает при разрушении
    @Override
    public Item getItemDropped(int metadata, java.util.Random random, int fortune) {
        return Item.getItemFromBlock(this);
    }





    // Регистрация всех вариаций ступенек (по метаданным)
    public static void registerStairs(BlockMeta baseBlock, int count) {
        for (int i = 0; i < count; i++) {
            new BlockMetaStairs(baseBlock, i);
        }
    }
}