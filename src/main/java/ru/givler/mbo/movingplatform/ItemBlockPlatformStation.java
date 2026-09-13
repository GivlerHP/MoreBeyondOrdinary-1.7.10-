package ru.givler.mbo.movingplatform;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockPlatformStation extends ItemBlock {
    public ItemBlockPlatformStation(Block block){super(block);}
    @Override public String getItemStackDisplayName(ItemStack stack){
        return super.getItemStackDisplayName(stack);
    }
}
