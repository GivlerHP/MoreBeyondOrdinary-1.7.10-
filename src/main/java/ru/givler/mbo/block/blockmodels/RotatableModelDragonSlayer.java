package ru.givler.mbo.block.blockmodels;

import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import ru.givler.mbo.block.BlockModels;

public class RotatableModelDragonSlayer extends BlockModels {
    public RotatableModelDragonSlayer(Material material, String name, String texture, String type) {
        super(material, name, texture, type);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);

        if (meta == 0) this.setBlockBounds(0.0F, 0.0F, 0.4F, 0.7F, 2.0F, 0.6F);
        else if (meta == 1) this.setBlockBounds(0.4F, 0.0F, 0.0F, 0.6F, 2.0F, 0.7F);
        else if (meta == 2) this.setBlockBounds(0.3F, 0.0F, 0.4F, 1.0F, 2.0F, 0.6F);
        else if (meta == 3) this.setBlockBounds(0.4F, 0.0F, 0.3F, 0.6F, 2.0F, 1.0F);
    }


}