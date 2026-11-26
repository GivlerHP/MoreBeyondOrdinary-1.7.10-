package ru.givler.mbo.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.IIcon;

public final class ItemBlockDoll extends ItemBlock {
    public ItemBlockDoll(Block block) {
        super(block);
        setMaxStackSize(1);
    }

    @Override @SideOnly(Side.CLIENT) public void registerIcons(IIconRegister icons) {
        itemIcon = icons.registerIcon("mbo:doll");
    }

    @Override @SideOnly(Side.CLIENT) public IIcon getIconFromDamage(int damage) { return itemIcon; }
    @Override @SideOnly(Side.CLIENT) public int getSpriteNumber() { return 1; }
}
