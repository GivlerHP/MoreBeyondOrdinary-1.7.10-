package ru.givler.mbo.item.amulets;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.item.ItemAmuletBase;
import ru.givler.mbo.main;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemHealingAmulet extends ItemAmuletBase {

    public ItemHealingAmulet(String name, String texture) {
        this.setUnlocalizedName(name);
        this.setTextureName(main.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxStackSize(1);
        this.setMaxDamage(10);
        GameRegistry.registerItem(this, name);
    }

    @Override
    public void activate(EntityPlayer player, ItemStack stack) {
        player.heal(3.0F);
    }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {

    }

    @Override
    public int getCooldownTicks() {
        return 20 * 30;
    }

    @Override
    public int getExperienceCost() {
        return 2;
    }
}
