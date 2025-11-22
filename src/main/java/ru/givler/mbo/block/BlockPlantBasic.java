package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class BlockPlantBasic extends BlockBush {
    public BlockPlantBasic(String name, String texture) {
        super(Material.plants);

        this.setBlockName(name);
        this.setBlockTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.setHardness(0.2F);
        this.setResistance(1.0F);
        this.setLightLevel(0.0F);
        this.setLightOpacity(0);
        this.setStepSound(soundTypeGrass);
        this.useNeighborBrightness = true;
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