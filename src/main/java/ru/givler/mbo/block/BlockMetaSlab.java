package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.item.Item;
import ru.givler.mbo.ItemBlockMetadata;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

// Класс создающий полублоки из метаблоков (BlockMeta)
public class BlockMetaSlab extends BlockSlab {
    private final BlockMeta baseBlock;
    private final int meta;

    public BlockMetaSlab(BlockMeta baseBlock, String texture, int meta) {
        super(false, baseBlock.getMaterial()); // Указываем материал блока
        this.baseBlock = baseBlock;
        this.meta = meta;

        this.setBlockName(baseBlock.getUnlocalizedName() + "_slab_" + meta); // Уникальное имя
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.setHardness(baseBlock.getBlockHardness(null, 0, 0, 0)); // Твёрдость
        this.setResistance(baseBlock.getExplosionResistance(null)); // Сопротивление взрывам
        this.setStepSound(baseBlock.stepSound); // Звук шага
        this.setHarvestLevel("pick_axe", 0); // Инструмент для добычи
        this.setLightLevel(baseBlock.getLightValue()); // Уровень освещения
        this.setLightOpacity(baseBlock.getLightOpacity()); // Прозрачность
        this.useNeighborBrightness = true; // Улучшенная обработка освещения

        this.setBlockTextureName(MoreBeyondOrdinary.MODID + ":" + texture + "_" + meta);

        GameRegistry.registerBlock(this, ItemBlockMetadata.class, baseBlock.getUnlocalizedName() + "_slab_" + meta);
    }

    // Указываем, какой ItemStack выпадает при разрушении
    @Override
    public Item getItemDropped(int metadata, java.util.Random random, int fortune) {
        return Item.getItemFromBlock(this);
    }

    // Регистрация всех вариаций полублоков (по метаданным)
    public static Block[] registerSlabs(BlockMeta baseBlock, int count, String texture) {
        Block[] slabsArray = new Block[count];
        for (int i = 0; i < count; i++) {
            slabsArray[i] = new BlockMetaSlab(baseBlock, texture, i);
        }
        return slabsArray;
    }

    @Override
    public String func_150002_b(int meta) {
        return this.getUnlocalizedName();
    }

}
