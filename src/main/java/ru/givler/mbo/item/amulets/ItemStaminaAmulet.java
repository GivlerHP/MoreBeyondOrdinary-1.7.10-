package ru.givler.mbo.item.amulets;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import minefantasy.mf2.api.stamina.StaminaBar;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.item.ItemAmuletBase;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemStaminaAmulet extends ItemAmuletBase {
    public ItemStaminaAmulet(String name, String texture) {
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxStackSize(1);
        this.setMaxDamage(10);
        GameRegistry.registerItem(this, name);
    }

    @Override
    public void activate(EntityPlayer player, ItemStack stack) {
        if (Loader.isModLoaded("minefantasy2")) {
            StaminaBar.modifyStaminaValue(player, 70.0F);
        }
        player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 240, 0));
    }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 40;
    }

    @Override
    public int getExperienceCost() {
        return 2;
    }
}
