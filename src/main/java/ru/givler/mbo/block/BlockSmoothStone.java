package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import ru.givler.mbo.registry.CreativeTabRegistry;

/**
 * Modern smooth stone. Every face uses the top texture of the vanilla stone slab.
 */
public class BlockSmoothStone extends Block {
    public BlockSmoothStone() {
        super(Material.rock);
        setBlockName("SmoothStone");
        setHardness(2.0F);
        setResistance(10.0F);
        setHarvestLevel("pickaxe", 0);
        setStepSound(soundTypeStone);
        setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        GameRegistry.registerBlock(this, "SmoothStone");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int metadata) {
        return Blocks.stone_slab.getIcon(1, 0);
    }
}
