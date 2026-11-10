package ru.givler.mbo.block.special;

import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class MBOBlockTrapDoor extends BlockTrapDoor {

    public MBOBlockTrapDoor(Material material) {
        super(material);
        this.disableValidation = true;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
        ru.givler.mbo.core.TrapdoorPlacementHooks.placedBy(world, x, y, z, player);
    }
}
