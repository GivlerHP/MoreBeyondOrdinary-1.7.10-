package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.Random;

public class BlockBasicSlab extends BlockSlab {

    public static class ItemBasicSlab extends ItemSlab {
        public ItemBasicSlab(Block block,
                             BlockBasicSlab single,
                             BlockBasicSlab dbl,
                             Boolean isDouble) {
            super(block, single, dbl, isDouble);
        }
    }

    private BlockBasicSlab singleSlabForDrops;

    private BlockBasicSlab(boolean isDouble, String regName, String texturePath, Material material) {
        super(isDouble, material);
        this.setBlockName(regName);
        this.setHardness(1.5F);
        this.setResistance(10.0F);
        this.setStepSound(soundTypeStone);
        if (material == Material.rock || material == Material.iron) {
            this.setHarvestLevel("pickaxe", 0);
        } else if (material == Material.wood) {
            this.setHarvestLevel("axe", 0);
        }
        this.setLightOpacity(0);
        this.useNeighborBrightness = true;
        this.setBlockTextureName(MoreBeyondOrdinary.MODID + ":" + texturePath);

        if (!isDouble) {
            this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        }
    }

    @Override
    public Item getItemDropped(int meta, Random rand, int fortune) {
        if (field_150004_a && singleSlabForDrops != null) {
            return Item.getItemFromBlock(singleSlabForDrops);
        }
        return super.getItemDropped(meta, rand, fortune);
    }

    @Override
    public int quantityDropped(Random rand) {
        if (field_150004_a && singleSlabForDrops != null) {
            return 2;
        }
        return super.quantityDropped(rand);
    }

    @Override
    public Item getItem(World world, int x, int y, int z) {
        return Item.getItemFromBlock(this);
    }

    @Override
    public String func_150002_b(int meta) {
        return getUnlocalizedName();
    }


    public static BlockBasicSlab registerPair(String baseName, String texturePath) {
        return registerPair(baseName, texturePath, Material.rock);
    }

    public static BlockBasicSlab registerPair(String baseName, String texturePath, Material material) {
        BlockBasicSlab single = new BlockBasicSlab(false, baseName + "_slab",        texturePath, material);
        BlockBasicSlab dbl    = new BlockBasicSlab(true,  baseName + "_slab_double", texturePath, material);
        dbl.singleSlabForDrops = single;

        GameRegistry.registerBlock(single, ItemBasicSlab.class,
                baseName + "_slab", single, dbl, Boolean.FALSE);
        GameRegistry.registerBlock(dbl,    ItemBasicSlab.class,
                baseName + "_slab_double", single, dbl, Boolean.TRUE);

        return single;
    }

    public static void addStandardRecipes(BlockBasicSlab single, Block parent) {
        GameRegistry.addRecipe(new ItemStack(single, 6),
                new Object[]{"XXX", 'X', parent});
        GameRegistry.addRecipe(new ItemStack(parent, 1),
                new Object[]{"X", "X", 'X', single});
    }
}