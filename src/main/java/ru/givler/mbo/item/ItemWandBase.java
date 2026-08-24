package ru.givler.mbo.item;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemWandBase extends Item {

    public ItemWandBase(String name, String texture, int maxDurability) {
        this.setMaxDamage(maxDurability);
        this.setMaxStackSize(1);
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":wand/" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        GameRegistry.registerItem(this, name);
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
        if (!world.isRemote && !player.capabilities.isCreativeMode) {
            itemstack.damageItem(1, player);
        }
        return itemstack;
    }
}
