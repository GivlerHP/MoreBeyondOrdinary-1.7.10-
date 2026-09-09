package ru.givler.mbo.movingplatform;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockPlatformStation extends ItemBlock {
    public ItemBlockPlatformStation(Block block){super(block);}
    @Override public String getItemStackDisplayName(ItemStack stack){
        String base=super.getItemStackDisplayName(stack);return stack!=null&&stack.hasTagCompound()?base+" ("+(stack.getTagCompound().getBoolean("TargetB")?"B":"A")+")":base;
    }
}
