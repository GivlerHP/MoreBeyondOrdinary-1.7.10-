package ru.givler.mbo.item.belt;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.item.ItemBeltBase;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemFertilityBelt extends ItemBeltBase {
    public ItemFertilityBelt(String name, String texture) {
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxStackSize(1);
        GameRegistry.registerItem(this, name);
    }
    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {

    }
}

