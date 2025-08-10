package ru.givler.mbo.block.specialblocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.Random;

public class BlockTemporaryGlass extends BlockGlass {
    public BlockTemporaryGlass(String name) {
        super(Material.glass, false);
        this.setBlockName(name);
        this.setTickRandomly(false);
        this.setLightOpacity(0);
        this.setHardness(1.0F);
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.setHarvestLevel("pick_axe", 1);
        this.setStepSound(soundTypeGlass);
        this.setBlockTextureName("minecraft:glass");
        GameRegistry.registerBlock(this, name);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        world.setBlockToAir(x, y, z);
    }
}