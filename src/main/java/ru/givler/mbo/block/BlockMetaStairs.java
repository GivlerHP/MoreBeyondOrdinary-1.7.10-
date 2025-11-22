package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.item.Item;
import ru.givler.mbo.ItemBlockMetadata;
import ru.givler.mbo.registry.CreativeTabRegistry;

// Класс создающий ступеньки из метаблоков (BlockMeta)
public class BlockMetaStairs extends BlockStairs {
    private final BlockMeta baseBlock;
    private final int meta;


    public BlockMetaStairs(BlockMeta baseBlock, int meta) {
        super(baseBlock, meta); // Используем текстуру базового блока с указанным метаданным
        this.baseBlock = baseBlock;
        this.meta = meta;


        this.setBlockName(baseBlock.getUnlocalizedName() + "_stairs_" + meta ); // Уникальное имя
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
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
    public static Block[] registerStairs(BlockMeta baseBlock, int count) {
        Block[] stairsArray = new Block[count];
        for (int i = 0; i < count; i++) {
            stairsArray[i] = new BlockMetaStairs(baseBlock, i);
        }
        return stairsArray;
    }
}