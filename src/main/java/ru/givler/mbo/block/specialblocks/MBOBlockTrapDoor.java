package ru.givler.mbo.block.specialblocks;

import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class MBOBlockTrapDoor extends BlockTrapDoor {

    public MBOBlockTrapDoor(Material material) {
        super(material);
        this.disableValidation = true;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
        //System.out.println("[MBO] onBlockPlacedBy called! Class: " + this.getClass().getName());
        int meta = world.getBlockMetadata(x, y, z);
        int openBit = meta & 4;
        int topBit  = meta & 8;

        int facing = MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

        switch (facing) {
            case 0: meta = 0; break;
            case 1: meta = 3; break;
            case 2: meta = 1; break;
            case 3: meta = 2; break;
        }

        world.setBlockMetadataWithNotify(x, y, z, meta | openBit | topBit, 2);
    }
}
