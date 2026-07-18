package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class BlockBasicFenceGate extends BlockFenceGate {

    private final Block planks;
    private final int plankMeta;

    public BlockBasicFenceGate(String name, Block planks, int plankMeta) {
        super();

        this.planks = planks;
        this.plankMeta = plankMeta;
        this.setBlockName(name);
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.setHardness(2.0F);
        this.setResistance(5.0F);
        this.setStepSound(Block.soundTypeWood);
        this.setHarvestLevel("axe", 0);

        GameRegistry.registerBlock(this, name);
    }

    public void addStandardRecipe() {
        GameRegistry.addRecipe(new ItemStack(this),
                new Object[]{"SXS", "SXS", 'X', new ItemStack(this.planks, 1, this.plankMeta),
                        'S', net.minecraft.init.Items.stick});
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return this.planks.getIcon(side, this.plankMeta);
    }
}
