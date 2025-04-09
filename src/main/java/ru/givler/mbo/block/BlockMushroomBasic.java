package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;

import net.minecraft.block.BlockMushroom;
import ru.givler.mbo.main;
import ru.givler.mbo.registry.CreativeTabRegistry;


public class BlockMushroomBasic extends BlockMushroom {

    public BlockMushroomBasic(String name, String texture) {
        super();

        this.setBlockName(name);
        this.setLightLevel(0.5F);
        this.setLightOpacity(0);
        this.setHardness(0.2F);
        this.setResistance(1.0F);
        this.setStepSound(soundTypeGrass);
        this.setBlockTextureName(main.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.useNeighborBrightness = true; // тени
        GameRegistry.registerBlock(this, name);
    }

    @Override
    public int getRenderBlockPass() {
        return 1; 
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }
}