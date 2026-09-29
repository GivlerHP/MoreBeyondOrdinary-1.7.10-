package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.item.ItemBlockMetadata;
import ru.givler.mbo.registry.CreativeTabRegistry;
import java.util.Random;

// Класс создающий ступеньки из метаблоков (BlockMeta)
public class BlockMetaStairs extends BlockStairs {
    private final BlockMeta baseBlock;
    private final int meta;
    private int copperState = -1;


    public BlockMetaStairs(BlockMeta baseBlock, int meta) {
        this(baseBlock, meta, baseBlock.getUnlocalizedName() + "_stairs_" + meta, true);
    }

    public BlockMetaStairs(BlockMeta baseBlock, int meta, String registrationName, boolean addRecipe) {
        super(baseBlock, meta); // Используем текстуру базового блока с указанным метаданным
        this.baseBlock = baseBlock;
        this.meta = meta;


        this.setBlockName(registrationName); // Уникальное имя
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.setHardness(baseBlock.getBlockHardness(null, 0, 0, 0)); // Твёрдость
        this.setResistance(baseBlock.getExplosionResistance(null)); // Сопротивление взрывам
        this.setStepSound(baseBlock.stepSound); // Звук шага
        this.setHarvestLevel("pick_axe", 0); // Инструмент для добычи
        this.setLightLevel(baseBlock.getLightValue()); // Уровень освещения
        this.setLightOpacity(baseBlock.getLightOpacity()); // Прозрачность
        this.useNeighborBrightness = true; // Улучшенная обработка освещения

        if (addRecipe) GameRegistry.registerBlock(this, ItemBlockMetadata.class, registrationName);
        else GameRegistry.registerBlock(this, registrationName);
        if (addRecipe) GameRegistry.addRecipe(new ItemStack(this, 4),
                new Object[]{"X  ", "XX ", "XXX", 'X', new ItemStack(baseBlock, 1, meta)});
    }

    public BlockMetaStairs withCopper(int state) {
        copperState = state;
        setHardness(3.0F);
        setResistance(6.0F);
        setHarvestLevel("pickaxe", 1);
        setTickRandomly((state & 8) == 0 && (state & 3) < 3);
        return this;
    }

    public int getCopperState() { return copperState; }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (copperState >= 0 && !world.isRemote) CopperOxidation.tick(world, x, y, z, random);
        else if (copperState < 0) super.updateTick(world, x, y, z, random);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        return copperState >= 0 && CopperOxidation.interact(world, x, y, z, player);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        super.setBlockBoundsBasedOnState(world, x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        if ((meta & 4) != 0) {
            this.minY = 0.0F;
            this.maxY = 1.0F;
        }
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
