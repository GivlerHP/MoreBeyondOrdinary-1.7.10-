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

public class BlockMetaSlab extends BlockSlab {

    public static class ItemMetaSlab extends ItemSlab {
        public ItemMetaSlab(Block block, BlockMetaSlab single, BlockMetaSlab dbl, Boolean isDouble) {
            super(block, single, dbl, isDouble);
        }
    }

    private BlockMetaSlab singleSlabForDrops;

    private BlockMetaSlab(boolean isDouble, BlockMeta baseBlock, String texture, int meta) {
        super(isDouble, baseBlock.getMaterial());

        this.setBlockName(baseBlock.getUnlocalizedName() + "_slab_" + meta);
        this.setHardness(baseBlock.getBlockHardness(null, 0, 0, 0));
        this.setResistance(baseBlock.getExplosionResistance(null));
        this.setStepSound(baseBlock.stepSound);
        if (baseBlock.getMaterial() == Material.rock || baseBlock.getMaterial() == Material.iron) {
            this.setHarvestLevel("pickaxe", 0);
        } else if (baseBlock.getMaterial() == Material.wood) {
            this.setHarvestLevel("axe", 0);
        }
        this.setLightOpacity(0);
        this.useNeighborBrightness = true;
        this.setBlockTextureName(MoreBeyondOrdinary.MODID + ":" + texture + "_" + meta);

        if (!isDouble) {
            this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        }
    }

    @Override
    public Item getItem(World world, int x, int y, int z) {
        return Item.getItemFromBlock(this);
    }


    @Override
    public Item getItemDropped(int meta, Random rand, int fortune) {
        if (field_150004_a && singleSlabForDrops != null) {
            return Item.getItemFromBlock(singleSlabForDrops);
        }
        return Item.getItemFromBlock(this);
    }

    @Override
    public int quantityDropped(Random rand) {
        if (field_150004_a && singleSlabForDrops != null) return 2;
        return 1;
    }

    @Override
    public String func_150002_b(int meta) {
        return this.getUnlocalizedName();
    }

    public static BlockMetaSlab[] registerSlabs(BlockMeta baseBlock, int count, String texture) {
        BlockMetaSlab[] result = new BlockMetaSlab[count];

        for (int i = 0; i < count; i++) {
            BlockMetaSlab single = new BlockMetaSlab(false, baseBlock, texture, i);
            BlockMetaSlab dbl    = new BlockMetaSlab(true,  baseBlock, texture, i);
            dbl.singleSlabForDrops = single;

            String singleName = baseBlock.getUnlocalizedName() + "_slab_" + i;
            String doubleName = baseBlock.getUnlocalizedName() + "_slab_double_" + i;

            GameRegistry.registerBlock(single, ItemMetaSlab.class, singleName, single, dbl, Boolean.FALSE);
            GameRegistry.registerBlock(dbl,    ItemMetaSlab.class, doubleName, single, dbl, Boolean.TRUE);

            result[i] = single;
        }

        return result;
    }

    public static void addStandardRecipes(BlockMetaSlab[] slabs, BlockMeta parent) {
        for (int i = 0; i < slabs.length; i++) {
            GameRegistry.addRecipe(new ItemStack(slabs[i], 6),
                    new Object[]{"XXX", 'X', new ItemStack(parent, 1, i)});
            GameRegistry.addRecipe(new ItemStack(parent, 1, i),
                    new Object[]{"X", "X", 'X', new ItemStack(slabs[i], 1)});
        }
    }
}