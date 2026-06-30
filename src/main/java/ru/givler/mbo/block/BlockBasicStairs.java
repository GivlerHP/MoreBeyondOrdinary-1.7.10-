package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockStairs;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class BlockBasicStairs extends BlockStairs {

    private final BlockBase baseBlock;

    public BlockBasicStairs(BlockBase baseBlock) {
        super(baseBlock, 0);

        this.baseBlock = baseBlock;
        this.setBlockName(baseBlock.getUnlocalizedName() + "_stairs");
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.setHardness(baseBlock.getBlockHardness(null, 0, 0, 0));
        this.setResistance(baseBlock.getExplosionResistance(null));
        this.setStepSound(baseBlock.stepSound);
        this.setHarvestLevel("pickaxe", 0);
        this.setLightLevel(baseBlock.getLightValue());
        this.setLightOpacity(baseBlock.getLightOpacity());
        this.useNeighborBrightness = true;

        GameRegistry.registerBlock(this, baseBlock.getUnlocalizedName() + "_stairs");
    }

    public void addStandardRecipes() {
        GameRegistry.addRecipe(new ItemStack(this, 4),
                new Object[]{"X  ", "XX ", "XXX", 'X', baseBlock});
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
}