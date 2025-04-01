package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockStairs;
import ru.givler.mbo.block.BlockBasic;
import ru.givler.mbo.registry.CreativeTabRegistry;


//класс создающий ступеньки из блоков добавленных через класс BlockBasic
public class BlockBasicStairs extends BlockStairs {

    public BlockBasicStairs(BlockBasic baseBlock) {
        super(baseBlock, 0); // Используем текстуру базового блока

        this.setBlockName(baseBlock.getUnlocalizedName() + "_stairs"); // Название блока
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.setHardness(baseBlock.getBlockHardness(null, 0, 0, 0)); // Твёрдость
        this.setResistance(baseBlock.getExplosionResistance(null)); // Сопротивление взрывам
        this.setStepSound(baseBlock.stepSound); // Звук шага
        this.setHarvestLevel("pick_axe", 0); // Инструмент для добычи
        this.setLightLevel(baseBlock.getLightValue()); // Уровень освещения
        this.setLightOpacity(baseBlock.getLightOpacity()); // Прозрачность
        this.useNeighborBrightness = true; // Улучшенная обработка освещения

        GameRegistry.registerBlock(this, baseBlock.getUnlocalizedName() + "_stairs"); // Регистрация блока
    }


}