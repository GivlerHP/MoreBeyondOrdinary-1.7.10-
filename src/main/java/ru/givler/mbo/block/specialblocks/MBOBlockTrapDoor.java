package ru.givler.mbo.block.specialblocks;

import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;

public class MBOBlockTrapDoor extends BlockTrapDoor {

    public MBOBlockTrapDoor(Material material) {
        super(material);
        this.disableValidation = true;
    }
}
